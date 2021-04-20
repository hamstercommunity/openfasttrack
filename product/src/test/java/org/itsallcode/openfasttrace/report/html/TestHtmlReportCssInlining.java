package org.itsallcode.openfasttrace.report.html;

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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.itsallcode.openfasttrace.api.report.ReportException;
import org.itsallcode.openfasttrace.report.html.view.ViewableContainer;
import org.itsallcode.openfasttrace.report.html.view.html.HtmlViewFactory;
import org.itsallcode.openfasttrace.testutil.AbstractFileBasedTest;
import org.itsallcode.openfasttrace.testutil.OsDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestHtmlReportCssInlining extends AbstractFileBasedTest
{
    private static final String CSS = "* { font-family: helvetica; }";

    // [itest->dsn~reporting.html.inline_css~1]
    @Test
    void testHtmlReportContainsInlineCSS(@TempDir final Path tempDir) throws IOException
    {
        final File cssFile = tempDir.resolve("test.css").toFile();
        writeTextFile(cssFile, CSS);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final HtmlViewFactory factory = HtmlViewFactory.create(stream, cssFile.toURI().toURL());
        final ViewableContainer view = factory.createView("foo", "bar");
        view.render();
        assertThat(stream.toString(), containsString(CSS));
    }

    @Test
    void testInliningNonExistentCssThrowsException() throws MalformedURLException
    {
        final HtmlViewFactory factory = HtmlViewFactory.create(System.out,
                new URL("file:///this_file_does_not_exist.css"));
        final ViewableContainer view = factory.createView("foo", "bar");
        assertThrows(ReportException.class, () -> view.render());
    }

    @Test
    void testInliningUnreadableCssThrowsException(@TempDir final Path tempDir) throws IOException
    {
        OsDetector.assumeRunningOnUnix();
        final File cssFile = tempDir.resolve("test.css").toFile();
        cssFile.setReadable(false);
        final HtmlViewFactory factory = HtmlViewFactory.create(System.out, cssFile.toURI().toURL());
        final ViewableContainer view = factory.createView("foo", "bar");
        assertThrows(ReportException.class, () -> view.render());
    }
}