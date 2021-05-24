package org.itsallcode.openfasttrace.importer.zip;

/*-
 * #%L
 * OpenFastTrace Zip Importer
 * %%
 * Copyright (C) 2016 - 2019 itsallcode.org
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.itsallcode.openfasttrace.api.importer.*;
import org.itsallcode.openfasttrace.api.importer.input.InputFile;
import org.itsallcode.openfasttrace.importer.zip.input.ZipEntryInput;

/**
 * This {@link Importer} supports reading {@link ZipFile} and delegates import
 * of {@link ZipEntry}s to a {@link MultiFileImporter}.
 */
public class ZipFileImporter implements Importer
{
    private final InputFile file;
    private final MultiFileImporter delegateImporter;

    ZipFileImporter(final ImporterService importerService, final InputFile file,
            final ImportEventListener listener)
    {
        this(file, importerService.createImporter(listener));
    }

    ZipFileImporter(final InputFile file, final MultiFileImporter delegateImporter)
    {
        this.file = file;
        this.delegateImporter = delegateImporter;
    }

    @Override
    public void runImport()
    {
        if (!this.file.isRealFile())
        {
            throw new UnsupportedOperationException(
                    "Importing a zip file from a stream is not supported");
        }
        try (ZipFile zip = new ZipFile(this.file.toPath().toFile(), StandardCharsets.UTF_8))
        {
            zip.stream() //
                    .filter(entry -> !entry.isDirectory()) //
                    .map(entry -> createInput(zip, entry)) //
                    .forEach(this.delegateImporter::importFile);
        }
        catch (final IOException e)
        {
            throw new ImporterException("Error reading \"" + this.file + "\"", e);
        }
    }

    private InputFile createInput(final ZipFile zip, final ZipEntry entry)
    {
        return ZipEntryInput.forZipEntry(zip, entry);
    }
}
