package app.revanced.integrations.patches.playback.quality;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import app.revanced.integrations.patches.components.VideoQualityMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import com.facebook.litho.ComponentHost;
import kotlin.Deprecated;

// This patch contains the logic to show the old video quality menu.
// Two methods are required, because the quality menu is a RecyclerView in the new YouTube version
// and a ListView in the old one.
public final class OldVideoQualityMenuPatch {
    public static void showOldVideoQualityMenu(final LinearLayout linearLayout) {
        if (!SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU.getBoolean()) return;

        // The quality menu is a RecyclerView with 3 children. The third child is the "Advanced" quality menu.

        if (linearLayout.getChildCount() != 3) return;

        var advancedQualityMenu = linearLayout.getChildAt(2);
        if (!(advancedQualityMenu instanceof RecyclerView)) return;
        final var recyclerView = (RecyclerView) advancedQualityMenu;

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        LogHelper.printDebug(() -> "Added view tree observer");

                        // Check if the current view is the quality menu.
                        if (VideoQualityMenuFilterPatch.isVideoQualityMenuVisible) {// Hide the video quality menu.
                            linearLayout.setVisibility(View.GONE);

                            // Click the "Advanced" quality menu to show the "old" quality menu.
                            ((ComponentHost) recyclerView.getChildAt(0)).getChildAt(3).performClick();
                            LogHelper.printDebug(() -> "Advanced quality menu in new type of quality menu clicked");
                        }

                        // Remove the listener because it will be added again.
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    @Deprecated(message = "This patch is deprecated because the quality menu is not a ListView anymore")
    public static void showOldVideoQualityMenu(final ListView listView) {
        if (!SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU.getBoolean()) return;

        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                LogHelper.printDebug(() -> "Added listener to old type of quality menu");

                parent.setVisibility(View.GONE);

                final var indexOfAdvancedQualityMenuItem = 4;
                if (listView.indexOfChild(child) != indexOfAdvancedQualityMenuItem) return;

                LogHelper.printDebug(() -> "Found advanced menu item in old type of quality menu");

                final var qualityItemMenuPosition = 4;
                listView.performItemClick(null, qualityItemMenuPosition, 0);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }
}
