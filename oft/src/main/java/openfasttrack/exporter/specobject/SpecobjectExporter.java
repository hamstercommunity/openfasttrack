package openfasttrack.exporter.specobject;

/*-
 * #%L
 * OpenFastTrack
 * %%
 * Copyright (C) 2016 - 2017 hamstercommunity
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import openfasttrack.core.Newline;
import openfasttrack.core.SpecificationItem;
import openfasttrack.core.SpecificationItemId;
import openfasttrack.exporter.Exporter;
import openfasttrack.exporter.ExporterException;

public class SpecobjectExporter implements Exporter
{
    private static final Logger LOG = Logger.getLogger(SpecobjectExporter.class.getName());

    private final XMLStreamWriter writer;
    private final Map<String, List<SpecificationItem>> items;
    private final Newline newline;

    public SpecobjectExporter(final Stream<SpecificationItem> itemStream,
            final XMLStreamWriter xmlWriter, final Newline newline)
    {
        this.newline = newline;
        this.items = groupByDoctype(itemStream);
        this.writer = xmlWriter;
    }

    private Map<String, List<SpecificationItem>> groupByDoctype(
            final Stream<SpecificationItem> itemStream)
    {
        return itemStream.collect(
                groupingBy(item -> item.getId().getArtifactType(), LinkedHashMap::new, toList()));
    }

    @Override
    // [impl->dsn~conversion.reqm2-export~1]
    public void runExport()
    {
        try
        {
            writeOutput();
        }
        catch (final XMLStreamException e)
        {
            throw new ExporterException("Error exporting to specobject format", e);
        }
        finally
        {
            closeXmlWriter();
        }
    }

    private void closeXmlWriter()
    {
        try
        {
            LOG.finest(() -> "Closing xml writer");
            this.writer.close();
        }
        catch (final XMLStreamException e)
        {
            throw new ExporterException("Error closing xml writer", e);
        }
    }

    private void writeOutput() throws XMLStreamException
    {
        this.writer.writeStartDocument("UTF-8", "1.0");
        this.writer.writeStartElement("specdocument");

        for (final Entry<String, List<SpecificationItem>> entry : this.items.entrySet())
        {
            final String doctype = entry.getKey();
            final List<SpecificationItem> specItems = entry.getValue();
            writeItems(doctype, specItems);
        }

        this.writer.writeEndElement();
        this.writer.writeEndDocument();
    }

    private void writeItems(final String doctype, final List<SpecificationItem> specItems)
            throws XMLStreamException
    {
        LOG.finest(() -> "Writing " + specItems.size() + " items with doctype " + doctype);
        this.writer.writeStartElement("specobjects");
        this.writer.writeAttribute("doctype", doctype);
        for (final SpecificationItem item : specItems)
        {
            writeItem(item);
        }
        this.writer.writeEndElement();
    }

    private void writeItem(final SpecificationItem item) throws XMLStreamException
    {
        final String description = processMultilineText(item.getDescription());
        final String rationale = processMultilineText(item.getRationale());
        final String comment = processMultilineText(item.getComment());
        this.writer.writeStartElement("specobject");
        writeElement("id", item.getId().getName());
        writeElement("version", item.getId().getRevision());
        writeElement("description", description);
        writeElement("rationale", rationale);
        writeElement("comment", comment);

        writeNeedsArtifactTypes(item.getNeedsArtifactTypes());
        writeCoveredIds(item.getCoveredIds());
        writeDependsOnIds(item.getDependOnIds());

        this.writer.writeEndElement();
    }

    private String processMultilineText(final String text)
    {
        return unifyNewlines(text);
    }

    private String unifyNewlines(final String text)
    {
        final Matcher matcher = Newline.anyNewlinePattern().matcher(text);
        return matcher.replaceAll(this.newline.toString());
    }

    private void writeDependsOnIds(final List<SpecificationItemId> dependOnIds)
            throws XMLStreamException
    {
        if (dependOnIds.isEmpty())
        {
            return;
        }
        this.writer.writeStartElement("dependencies");
        for (final SpecificationItemId dependsOnId : dependOnIds)
        {
            writeElement("dependson", dependsOnId.toString());
        }
        this.writer.writeEndElement();
    }

    private void writeCoveredIds(final List<SpecificationItemId> coveredIds)
            throws XMLStreamException
    {
        if (coveredIds.isEmpty())
        {
            return;
        }
        this.writer.writeStartElement("providescoverage");
        for (final SpecificationItemId coveredId : coveredIds)
        {
            this.writer.writeStartElement("provcov");
            writeElement("linksto", coveredId.getName());
            writeElement("dstversion", coveredId.getRevision());
            this.writer.writeEndElement();
        }
        this.writer.writeEndElement();
    }

    private void writeNeedsArtifactTypes(final List<String> needsArtifactTypes)
            throws XMLStreamException
    {
        if (needsArtifactTypes.isEmpty())
        {
            return;
        }
        this.writer.writeStartElement("needscoverage");
        for (final String neededArtifactType : needsArtifactTypes)
        {
            writeElement("needsobj", neededArtifactType);
        }
        this.writer.writeEndElement();
    }

    private void writeElement(final String elementName, final int content) throws XMLStreamException
    {
        writeElement(elementName, String.valueOf(content));
    }

    private void writeElement(final String elementName, final String content)
            throws XMLStreamException
    {
        this.writer.writeStartElement(elementName);
        this.writer.writeCharacters(content);
        this.writer.writeEndElement();
    }
}
