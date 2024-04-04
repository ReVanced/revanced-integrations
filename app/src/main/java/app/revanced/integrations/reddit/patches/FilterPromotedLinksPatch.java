package app.revanced.integrations.reddit.patches;

import java.util.ArrayList;
import java.util.List;

import com.reddit.domain.model.ILink;

public final class FilterPromotedLinksPatch {
    /**
     * Filters list from promoted links.
     **/
    public static List<?> filterChildren(final Iterable<?> links) {
        final List<Object> filteredList = new ArrayList<>();

        for (Object item : links) {
            if (item instanceof ILink && ((ILink) item).getPromoted()) continue;

            filteredList.add(item);
        }

        return filteredList;
    }
}
