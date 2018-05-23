package org.itsallcode.openfasttrace.importer.rif.handler;

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
