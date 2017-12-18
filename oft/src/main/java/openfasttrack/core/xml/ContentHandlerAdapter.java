package openfasttrack.core.xml;

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

import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import openfasttrack.core.Location;
import openfasttrack.core.xml.event.EndElementEvent;
import openfasttrack.core.xml.event.StartElementEvent;

public class ContentHandlerAdapter extends DefaultHandler implements ContentHandlerAdapterController
{
    private final static Logger LOG = Logger.getLogger(ContentHandlerAdapter.class.getName());

    private final String fileName;
    private final XMLReader xmlReader;
    private final EventContentHandler delegate;
    private Locator locator;
    private ContentHandler originalContentHandler;

    public ContentHandlerAdapter(final String fileName, final XMLReader xmlReader,
            final EventContentHandler delegate)
    {
        this.fileName = fileName;
        this.xmlReader = xmlReader;
        this.delegate = delegate;
    }

    public void registerListener()
    {
        if (this.originalContentHandler != null)
        {
            throw new IllegalStateException("Already registered as listener");
        }
        this.originalContentHandler = this.xmlReader.getContentHandler();
        this.delegate.init(this);
        this.xmlReader.setContentHandler(this);
    }

    @Override
    public void setDocumentLocator(final Locator locator)
    {
        this.locator = locator;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException
    {
        LOG.finest(() -> "Start element: uri=" + uri + ", localName=" + localName + ", qName="
                + qName);
        final StartElementEvent event = StartElementEvent.create(uri, localName, qName, attributes,
                getCurrentLocation());
        this.delegate.startElement(event);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException
    {
        LOG.finest(
                () -> "End element: uri=" + uri + ", localName=" + localName + ", qName=" + qName);
        final EndElementEvent event = EndElementEvent.create(uri, localName, qName,
                getCurrentLocation());
        this.delegate.endElement(event);
    }

    private Location getCurrentLocation()
    {
        return Location.create(this.fileName, this.locator.getLineNumber(),
                this.locator.getColumnNumber());
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
    {
        this.delegate.characters(new String(ch, start, length));
    }

    @Override
    public void parsingFinished()
    {
        this.xmlReader.setContentHandler(this.originalContentHandler);
    }
}
