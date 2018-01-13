package openfasttrack.importer;

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



import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import openfasttrack.core.SpecificationItem;

public class TestMultiFileImporter
{
    private static final Path FOLDER = Paths.get("src/test/resources/markdown");
    private static final Path FILE1 = FOLDER.resolve("sample_design.md");
    private static final Path FILE2 = FOLDER.resolve("sample_system_requirements.md");
    private static final Path NON_EXISTING_FILE = FOLDER.resolve("does_not_exist");

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Mock
    private SpecificationListBuilder specItemBuilderMock;
    @Mock
    private ImporterFactoryLoader factoryLoaderMock;
    @Mock
    private Importer importerMock;
    @Mock
    private ImporterFactory importerFactoryMock;

    private MultiFileImporter multiFileImporter;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        this.multiFileImporter = new MultiFileImporter(this.specItemBuilderMock,
                this.factoryLoaderMock);
    }

    @Test
    public void testImportSingleFile()
    {
        expectFileImported(FILE1);
        this.multiFileImporter.importFile(FILE1);
        verify(this.importerMock).runImport();
    }

    @Test
    public void testImportAnySingleFile()
    {
        expectFileImported(FILE1);
        this.multiFileImporter.importAny(asList(FILE1));
        verify(this.importerMock).runImport();
    }

    @Test
    public void testImportAnyNonExistingFile()
    {
        this.multiFileImporter.importAny(asList(NON_EXISTING_FILE));
        verify(this.importerMock, times(0)).runImport();
    }

    @Test
    public void testImportAnyFolderFile()
    {
        expectFileImported(FILE1);
        expectFileImported(FILE2);
        this.multiFileImporter.importAny(asList(FOLDER));
        verify(this.importerMock, times(2)).runImport();
    }

    // [itest->dsn~input-directory-recursive-traversal~1]
    @Test
    public void testRecursiveDir()
    {
        expectFileImported(FILE1);
        expectFileImported(FILE2);
        this.multiFileImporter.importRecursiveDir(FOLDER, "**/*.md");
        verify(this.importerMock, times(2)).runImport();
    }

    @Test
    public void testGetImportedItems()
    {
        final List<SpecificationItem> expected = new ArrayList<>();
        when(this.specItemBuilderMock.build()).thenReturn(expected);

        assertThat(this.multiFileImporter.getImportedItems(), sameInstance(expected));
    }

    private void expectFileImported(final Path file)
    {
        when(this.factoryLoaderMock.supportsFile(file)).thenReturn(true);
        when(this.factoryLoaderMock.getImporterFactory(file)).thenReturn(this.importerFactoryMock);
        when(this.importerFactoryMock.createImporter(eq(file), eq(DEFAULT_CHARSET),
                same(this.specItemBuilderMock))).thenReturn(this.importerMock);
    }
}
