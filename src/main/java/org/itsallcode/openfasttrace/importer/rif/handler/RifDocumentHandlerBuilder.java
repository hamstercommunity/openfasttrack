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
import java.util.logging.Logger;

import org.itsallcode.openfasttrace.core.xml.tree.CallbackContentHandler;
import org.itsallcode.openfasttrace.core.xml.tree.TreeContentHandler;
import org.itsallcode.openfasttrace.importer.ImportEventListener;

public class RifDocumentHandlerBuilder
{

    private static final Logger LOG = Logger.getLogger(RifDocumentHandlerBuilder.class.getName());

    private static final String DOCTYPE_ATTRIBUTE_NAME = "doctype";
    private final CallbackContentHandler handler;
    private final String fileName;
    private final ImportEventListener listener;

    public RifDocumentHandlerBuilder(final String fileName, final ImportEventListener listener)
    {
        this.fileName = fileName;
        this.listener = listener;

        this.handler = new CallbackContentHandler();
    }

    public TreeContentHandler build()
    {
        return this.handler;

    }

}
