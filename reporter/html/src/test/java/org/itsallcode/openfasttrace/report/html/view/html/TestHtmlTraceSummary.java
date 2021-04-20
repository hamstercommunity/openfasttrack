package org.itsallcode.openfasttrace.report.html.view.html;

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

import static org.mockito.Mockito.when;

import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.report.html.view.Viewable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestHtmlTraceSummary extends AbstractTestHtmlRenderer
{
    @Mock
    private Trace traceMock;

    @Override
    @BeforeEach
    public void prepareEachTest()
    {
        super.prepareEachTest();
    }

    @Test
    void testRenderSummaryOk()
    {
        when(this.traceMock.hasNoDefects()).thenReturn(true);
        when(this.traceMock.count()).thenReturn(200);
        when(this.traceMock.countDefects()).thenReturn(0);
        renderTaceSummaryOnIndentationLevel(1);
        assertOutputLines("  " + CharacterConstants.CHECK_MARK
                + " 200 total <meter value=\"200\" max=\"200\">100%</meter>");
    }

    private void renderTaceSummaryOnIndentationLevel(final int indentationLevel)
    {
        final Viewable view = this.factory.createTraceSummary(this.traceMock);
        view.render(indentationLevel);
    }

    @ParameterizedTest
    @ValueSource(ints =
    { 0, 1, 50, 99 })
    void testRenderPercentagesNotOk(final int value)
    {
        final int maximum = 100;
        final int defects = maximum - value;
        when(this.traceMock.hasNoDefects()).thenReturn(false);
        when(this.traceMock.count()).thenReturn(maximum);
        when(this.traceMock.countDefects()).thenReturn(defects);
        renderTaceSummaryOnIndentationLevel(1);
        assertOutputLines("  " + CharacterConstants.CROSS_MARK + " " + maximum
                + " total <meter value=\"" + value + "\" low=\"99\" max=\"100\">" + value
                + "%</meter>" + " <span class=\".red\">" + defects + " defects</span>");
    }
}