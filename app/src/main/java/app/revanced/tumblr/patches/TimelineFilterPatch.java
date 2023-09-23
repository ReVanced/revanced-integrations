package app.revanced.tumblr.patches;

import android.util.Log;

import com.tumblr.rumblr.model.TimelineObject;
import com.tumblr.rumblr.model.TimelineObjectType;
import com.tumblr.rumblr.model.Timelineable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TimelineFilterPatch {
    public static void filterTimeline(List<TimelineObject<? extends Timelineable>> timelineObjects) {
        Iterator<TimelineObject<? extends Timelineable>> iterator = timelineObjects.iterator();
        while (iterator.hasNext()) {
            TimelineObject<? extends Timelineable> timelineElement = iterator.next();
            if (timelineElement == null) continue;
            String elementType = timelineElement.getData().getTimelineObjectType().toString();

            // This dummy gets removed by the TimelineFilterPatch and in its place,
            // equivalent instructions with a different constant string
            // will be inserted for each Timeline object type filter.
            if ("BLOCKED_OBJECT_DUMMY".equals(elementType)) iterator.remove();
        }
    }
}
