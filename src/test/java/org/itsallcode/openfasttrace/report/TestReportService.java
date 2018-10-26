package org.itsallcode.openfasttrace.report;

import static org.hamcrest.MatcherAssert.assertThat;

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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemOutGuard;
import org.itsallcode.openfasttrace.ReportSettings;
import org.itsallcode.openfasttrace.core.Trace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith(TempDirectory.class)
@ExtendWith(SystemOutGuard.class)
class TestReportService
{
    @Mock
    Trace traceMock;

    private ReportService service;

    @BeforeEach
    public void prepareTest()
    {
        MockitoAnnotations.initMocks(this);
        this.service = new ReportService();
    }

    @Test
    void testReportPlainText(final Capturable stream)
    {
        final ReportSettings settings = ReportSettings.builder().verbosity(ReportVerbosity.MINIMAL)
                .build();
        stream.capture();
        this.service.reportTraceToStdOut(this.traceMock, settings);
        assertThat(stream.getCapturedData(), equalTo("not ok\n"));
    }

    @Test
    void testReportHtml(final Capturable stream)
    {
        final ReportSettings settings = ReportSettings.builder().outputFormat("html")
                .verbosity(ReportVerbosity.MINIMAL).build();
        stream.capture();
        this.service.reportTraceToStdOut(this.traceMock, settings);
        assertThat(stream.getCapturedData(), startsWith("<!DOCTYPE html>"));
    }

    @Test
    void testInvalidReportFormatThrowsIllegalArgumentException()
    {
        final ReportSettings settings = ReportSettings.builder().outputFormat("invalid")
                .verbosity(ReportVerbosity.QUIET).build();
        assertThrows(IllegalArgumentException.class,
                () -> this.service.reportTraceToStdOut(this.traceMock, settings));
    }

    @Test
    void testReportToIllegalPathThrowsReportExpection(@TempDir final Path tempDir)
            throws IOException
    {
        final File readOnlyFile = tempDir.resolve("readonly.txt").toFile();
        readOnlyFile.setReadOnly();
        final ReportSettings settings = ReportSettings.builder().verbosity(ReportVerbosity.QUIET)
                .build();
        assertThrows(ReportException.class, () -> this.service.reportTraceToPath(this.traceMock,
                readOnlyFile.toPath(), settings));
    }
}