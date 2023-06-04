package app.revanced.reddit.patches;

import com.reddit.domain.model.ILink;

import java.util.ArrayList;
import java.util.List;

public final class FilterPromotedLinksPatch {
    /**
     * Filters away all ILinks which are labeled as promoted.
     **/
    public static List<?> filterChildren(final Iterable<?> links) {
        List<Object> filteredList = new ArrayList<>();
        for (Object item : links) {
            if (!(item instanceof ILink) || !((ILink) item).getPromoted()) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }
}
