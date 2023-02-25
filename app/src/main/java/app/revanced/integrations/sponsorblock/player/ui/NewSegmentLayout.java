package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdByName;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.utils.LogHelper;

public class NewSegmentLayout extends FrameLayout {
    private final int rippleEffectId;
    final int defaultBottomMargin;
    final int ctaBottomMargin;

    public NewSegmentLayout(Context context) {
        this(context, null);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(getResourceIdByName(context, "new_segment", "layout"), this, true);

        TypedValue rippleEffect = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleEffect, true);
        rippleEffectId = rippleEffect.resourceId;

        // LinearLayout newSegmentContainer = this.findViewById(getIdentifier(context, "sb_new_segment_container", "id"));

        ImageButton rewindButton = findViewById(getResourceIdByName(context, "sb_new_segment_rewind", "id"));
        if (rewindButton == null) {
            LogHelper.printException(() -> "Could not find rewindButton");
        } else {
            setClickEffect(rewindButton);
            rewindButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Rewind button clicked");
                VideoInformation.seekToRelative(-SettingsEnum.SB_ADJUST_NEW_SEGMENT_STEP.getInt());
            });
        }
        ImageButton forwardButton = findViewById(getResourceIdByName(context, "sb_new_segment_forward", "id"));
        if (forwardButton == null) {
            LogHelper.printException(() -> "Could not find forwardButton");
        } else {
            setClickEffect(forwardButton);
            forwardButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Forward button clicked");
                VideoInformation.seekToRelative(SettingsEnum.SB_ADJUST_NEW_SEGMENT_STEP.getInt());
            });
        }
        ImageButton adjustButton = findViewById(getResourceIdByName(context, "sb_new_segment_adjust", "id"));
        if (adjustButton == null) {
            LogHelper.printException(() -> "Could not find adjustButton");
        } else {
            setClickEffect(adjustButton);
            adjustButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Adjust button clicked");
                SponsorBlockUtils.onMarkLocationClicked();
            });
        }
        ImageButton compareButton = this.findViewById(getResourceIdByName(context, "sb_new_segment_compare", "id"));
        if (compareButton == null) {
            LogHelper.printException(() -> "Could not find compareButton");
        } else {
            setClickEffect(compareButton);
            compareButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Compare button clicked");
                SponsorBlockUtils.onPreviewClicked();
            });
        }
        ImageButton editButton = findViewById(getResourceIdByName(context, "sb_new_segment_edit", "id"));
        if (editButton == null) {
            LogHelper.printException(() -> "Could not find editButton");
        } else {
            setClickEffect(editButton);
            editButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Edit button clicked");
                SponsorBlockUtils.onEditByHandClicked();
            });
        }
        ImageButton publishButton = this.findViewById(getResourceIdByName(context, "sb_new_segment_publish", "id"));
        if (publishButton == null) {
            LogHelper.printException(() -> "Could not find publishButton");
        } else {
            setClickEffect(publishButton);
            publishButton.setOnClickListener(v -> {
                LogHelper.printDebug(() -> "Publish button clicked");
                SponsorBlockUtils.onPublishClicked();
            });
        }

        Resources resources = context.getResources();
        defaultBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "brand_interaction_default_bottom_margin", "dimen"));
        ctaBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "brand_interaction_cta_bottom_margin", "dimen"));
    }

    private void setClickEffect(ImageButton btn) {
        btn.setBackgroundResource(rippleEffectId);

        RippleDrawable rippleDrawable = (RippleDrawable) btn.getBackground();

        int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}};
        int[] colors = new int[]{0x33ffffff}; // sets the ripple color to white

        ColorStateList colorStateList = new ColorStateList(states, colors);
        rippleDrawable.setColor(colorStateList);
    }
}
