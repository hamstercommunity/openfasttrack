package org.itsallcode.openfasttrace.importer.rif.handler;

/*-
 * #%L
 * OpenFastTrace
 * %%
 * Copyright (C) 2016 - 2018 itsallcode.org
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
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.core.Location;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.core.xml.tree.CallbackContentHandler;
import org.itsallcode.openfasttrace.importer.ImportEventListener;

public class SingleRifObjectsHandlerBuilder
{
    private final CallbackContentHandler handler;
    private final ImportEventListener listener;
    private final SpecificationItemId.Builder idBuilder;
    private final Location.Builder locationBuilder;
    private String containedFileName = null;
    private int containedLine = -1;

    public SingleRifObjectsHandlerBuilder(final ImportEventListener listener,
            final SpecificationItemId.Builder idBuilder, final Location.Builder locationBuilder)
    {
        this.listener = listener;
        this.idBuilder = idBuilder;
        this.locationBuilder = locationBuilder;
        this.handler = new CallbackContentHandler();
    }

    public CallbackContentHandler build()
    {
        configureDataHandlers();
        configureSubTreeHanlders();
        ignoreCharacterData("creationdate", "source");
        return this.handler;
    }

    private void configureSubTreeHanlders()
    {

    }

    private void configureDataHandlers()
    {
        this.handler.addCharacterDataListener("id", this::removeArtifactTypeFromName)
                .addIntDataListener("version", this.idBuilder::revision)
                .addCharacterDataListener("description", this.listener::appendDescription)
                .addCharacterDataListener("rationale", this.listener::appendRationale)
                .addCharacterDataListener("comment", this.listener::appendComment)
                .addCharacterDataListener("status", this::setStatus)
                .addCharacterDataListener("shortdesc", this.listener::setTitle)
                .addCharacterDataListener("sourcefile", this::rememberSourceFile)
                .addIntDataListener("sourceline", this::rememberSourceLine);
    }

    private void setStatus(final String statusAsText)
    {
        this.listener.setStatus(ItemStatus.parseString(statusAsText));
    }

    private void removeArtifactTypeFromName(final String data)
    {
        this.idBuilder.name(data);
    }

    private void ignoreCharacterData(final String... elements)
    {
        for (final String element : elements)
        {
            this.handler.addCharacterDataListener(element, text -> {});
        }
    }

    private void rememberSourceFile(final String fileName)
    {
        this.containedFileName = fileName;
        setContainedLocationIfComplete();
    }

    private void setContainedLocationIfComplete()
    {
        if (this.containedFileName != null && this.containedLine >= 1)
        {
            this.locationBuilder.path(this.containedFileName).line(this.containedLine);
        }
    }

    private void rememberSourceLine(final int line)
    {
        this.containedLine = line;
        setContainedLocationIfComplete();
    }

}
