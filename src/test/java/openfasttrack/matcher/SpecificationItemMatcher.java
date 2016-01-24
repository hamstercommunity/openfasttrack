package openfasttrack.matcher;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.StreamSupport;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import openfasttrack.core.SpecificationItem;
import openfasttrack.matcher.config.ConfigurableMatcher;
import openfasttrack.matcher.config.MatcherConfig;

/**
 * {@link Matcher} for {@link SpecificationItem}
 */
public class SpecificationItemMatcher extends ConfigurableMatcher<SpecificationItem>
{
    private SpecificationItemMatcher(final SpecificationItem expected)
    {
        super(MatcherConfig.builder(expected) //
                .addProperty("id", SpecificationItem::getId, SpecificationItemIdMatcher::equalTo) //
                .addEqualsProperty("comment", SpecificationItem::getComment) //
                .addEqualsProperty("description", SpecificationItem::getDescription) //
                .addEqualsProperty("rationale", SpecificationItem::getRationale) //
                .addIterableProperty("coveredIds", SpecificationItem::getCoveredIds,
                        SpecificationItemIdMatcher::equalTo) //
                .addIterableProperty("dependsOnIds", SpecificationItem::getDependOnIds,
                        SpecificationItemIdMatcher::equalTo) //
                .addIterableProperty("neededArtifactTypes",
                        SpecificationItem::getNeededArtifactTypes, Matchers::equalTo) //
                .build());
    }

    @Factory
    public static Matcher<SpecificationItem> equalTo(final SpecificationItem item)
    {
        return new SpecificationItemMatcher(item);
    }

    @Factory
    public static Collection<Matcher<SpecificationItem>> equalTo(
            final Iterable<SpecificationItem> items)
    {
        return StreamSupport.stream(items.spliterator(), false) //
                .map(SpecificationItemMatcher::equalTo) //
                .collect(toList());
    }
}
