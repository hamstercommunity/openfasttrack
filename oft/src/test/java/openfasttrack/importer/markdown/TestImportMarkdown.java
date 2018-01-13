package openfasttrack.importer.markdown;

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


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import openfasttrack.core.SpecificationItemId;
import openfasttrack.importer.ImportEventListener;
import openfasttrack.importer.Importer;

@RunWith(MockitoJUnitRunner.class)
public class TestImportMarkdown
{
    final private static SpecificationItemId ID1 = SpecificationItemId.parseId("type~id~1");
    final private static SpecificationItemId ID2 = SpecificationItemId.parseId("type~id~2");
    final private static String TITLE = "Requirement Title";
    private static final String DESCRIPTION_LINE1 = "Description";
    private static final String DESCRIPTION_LINE2 = "";
    private static final String DESCRIPTION_LINE3 = "More description";
    private static final String RATIONALE_LINE1 = "Rationale";
    private static final String RATIONALE_LINE2 = "More rationale";
    private static final String COMMENT_LINE1 = "Comment";
    private static final String COMMENT_LINE2 = "More comment";
    private static final String COVERED_ID1 = "impl~foo1~1";
    private static final String COVERED_ID2 = "impl~baz2~2";
    private static final String NEEDS_ARTIFACT_TYPE1 = "artA";
    private static final String NEEDS_ARTIFACT_TYPE2 = "artB";
    private static final String DEPENDS_ON_ID1 = "configuration~blubb.blah.blah~4711";
    private static final String DEPENDS_ON_ID2 = "db~blah.blubb~42";
    private static final String FILENAME = "file name";

    @Mock
    ImportEventListener listenerMock;

    @Mock
    Reader readerMock;

    // [utest~md.specification_item_id_format~1]
    @Test
    public void testIdentifyId()
    {
        assertMatch(MdPattern.ID, "req~foo~1<a id=\"req~foo~1\"></a>", "a~b~0", "req~test~1",
                "req~test~999", "req~test.requirement~1", "req~test_underscore~1",
                "`req~test1~1`arbitrary text");
        assertMismatch(MdPattern.ID, "test~1", "req-test~1", "req~4test~1");
    }

    // [utest~md.specification_item_title~1]
    @Test
    public void testIdentifyTitle()
    {
        assertMatch(MdPattern.TITLE, "#Title", "# Title", "###### Title", "#   Title");
        assertMismatch(MdPattern.TITLE, "Title", "Title #", " # Title");
    }

    private void assertMatch(final MdPattern mdPattern, final String... samples)
    {
        assertMatching(samples, mdPattern, true);
    }

    private void assertMismatch(final MdPattern mdPattern, final String... samples)
    {
        assertMatching(samples, mdPattern, false);
    }

    private void assertMatching(final String[] samples, final MdPattern mdPattern,
            final boolean mustMatch)
    {
        for (final String text : samples)
        {
            final Matcher matcher = mdPattern.getPattern().matcher(text);
            assertThat(mdPattern.toString() + " must " + (mustMatch ? "" : "not ") + "match " + "\""
                    + text + "\"", matcher.matches(), equalTo(mustMatch));
        }
    }

    @Test
    public void testFindRequirement()
    {
        final String completeItem = createCompleteSpecificationItemInMarkdownFormat();
        runImporterOnText(completeItem);
        assertAllImporterEventsCalled();
    }

    private String createCompleteSpecificationItemInMarkdownFormat()
    {
        return "# " + TestImportMarkdown.TITLE //
                + "\n" //
                + "`" + ID1 + "` <a id=\"" + ID1 + "\"></a>" //
                + "\n" //
                + DESCRIPTION_LINE1 + "\n" //
                + DESCRIPTION_LINE2 + "\n" //
                + DESCRIPTION_LINE3 + "\n" //
                + "\nRationale:\n" //
                + RATIONALE_LINE1 + "\n" //
                + RATIONALE_LINE2 + "\n" //
                + "\nCovers:\n\n" //
                + "  * " + COVERED_ID1 + "\n" //
                + " + " + "[Link to baz2](#" + COVERED_ID2 + ")\n" //
                + "\nDepends:\n\n" //
                + "  + " + DEPENDS_ON_ID1 + "\n" //
                + "  - " + DEPENDS_ON_ID2 + "\n" //
                + "\nComment:\n\n" //
                + COMMENT_LINE1 + "\n" //
                + COMMENT_LINE2 + "\n" //
                + "\nNeeds: " + NEEDS_ARTIFACT_TYPE1 //
                + ", " + NEEDS_ARTIFACT_TYPE2;
    }

    private void runImporterOnText(final String text)
    {
        final StringReader reader = new StringReader(text);
        final Importer importer = new MarkdownImporterFactory().createImporter(FILENAME, reader,
                this.listenerMock);
        importer.runImport();
    }

    // [utest->dsn~md.covers_list~1]
    // [utest->dsn~md.depends_list~1]
    // [utest->dsn~md.requirement-references~1]
    private void assertAllImporterEventsCalled()
    {
        final InOrder inOrder = inOrder(this.listenerMock);
        inOrder.verify(this.listenerMock).beginSpecificationItem();
        inOrder.verify(this.listenerMock).setId(ID1);
        inOrder.verify(this.listenerMock).setLocation(FILENAME, 2);
        inOrder.verify(this.listenerMock).setTitle(TITLE);
        inOrder.verify(this.listenerMock)
                .appendDescription(DESCRIPTION_LINE1 + System.lineSeparator() + DESCRIPTION_LINE2
                        + System.lineSeparator() + DESCRIPTION_LINE3);
        inOrder.verify(this.listenerMock)
                .appendRationale(RATIONALE_LINE1 + System.lineSeparator() + RATIONALE_LINE2);
        inOrder.verify(this.listenerMock).addCoveredId(SpecificationItemId.parseId(COVERED_ID1));
        inOrder.verify(this.listenerMock).addCoveredId(SpecificationItemId.parseId(COVERED_ID2));
        inOrder.verify(this.listenerMock)
                .addDependsOnId(SpecificationItemId.parseId(DEPENDS_ON_ID1));
        inOrder.verify(this.listenerMock)
                .addDependsOnId(SpecificationItemId.parseId(DEPENDS_ON_ID2));
        inOrder.verify(this.listenerMock)
                .appendComment(COMMENT_LINE1 + System.lineSeparator() + COMMENT_LINE2);
        inOrder.verify(this.listenerMock).addNeededArtifactType(NEEDS_ARTIFACT_TYPE1);
        inOrder.verify(this.listenerMock).addNeededArtifactType(NEEDS_ARTIFACT_TYPE2);
        inOrder.verify(this.listenerMock).endSpecificationItem();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testTwoConsecutiveSpecificationItems()
    {
        runImporterOnText(createTwoConsecutiveItemsInMarkdownFormat());
        assertImporterEventsForTwoConsecutiveItemsCalled();
    }

    private String createTwoConsecutiveItemsInMarkdownFormat()
    {
        return "# " + TestImportMarkdown.TITLE //
                + "\n" //
                + ID1 + "\n" //
                + "\n" + ID2 + "\n" //
                + "# Irrelevant Title";
    }

    private void assertImporterEventsForTwoConsecutiveItemsCalled()
    {
        final InOrder inOrder = inOrder(this.listenerMock);
        inOrder.verify(this.listenerMock).beginSpecificationItem();
        inOrder.verify(this.listenerMock).setId(ID1);
        inOrder.verify(this.listenerMock).setLocation(FILENAME, 2);
        inOrder.verify(this.listenerMock).setTitle(TITLE);
        inOrder.verify(this.listenerMock).endSpecificationItem();
        inOrder.verify(this.listenerMock).beginSpecificationItem();
        inOrder.verify(this.listenerMock).setId(ID2);
        inOrder.verify(this.listenerMock).setLocation(FILENAME, 4);
        inOrder.verify(this.listenerMock).endSpecificationItem();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSingleNeeds()
    {
        final String singleNeedsItem = "`foo~bar~1`\n\nNeeds: " + NEEDS_ARTIFACT_TYPE1;
        runImporterOnText(singleNeedsItem);
        verify(this.listenerMock, times(1)).addNeededArtifactType(NEEDS_ARTIFACT_TYPE1);
    }
}
