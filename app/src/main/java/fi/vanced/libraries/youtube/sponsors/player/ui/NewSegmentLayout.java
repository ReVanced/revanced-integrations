package fi.vanced.libraries.youtube.sponsors.player.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import pl.jakubweg.NewSegmentHelperLayout;
import pl.jakubweg.PlayerController;
import pl.jakubweg.SponsorBlockSettings;
import pl.jakubweg.SponsorBlockUtils;

import static fi.razerman.youtube.XGlobals.debug;

public class NewSegmentLayout extends FrameLayout {
    static String TAG = "NewSegmentLayout";

    private LinearLayout newSegmentContainer;
    public int defaultBottomMargin;
    public int ctaBottomMargin;
    public ImageButton rewindButton;
    public ImageButton forwardButton;
    public ImageButton adjustButton;
    public ImageButton compareButton;
    public ImageButton editButton;
    public ImageButton publishButton;
    private int rippleEffectId;

    public NewSegmentLayout(Context context) {
        super(context);
        this.initialize(context);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.initialize(context);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.initialize(context);
    }

    public NewSegmentLayout(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.initialize(context);
    }

    private final void initialize(Context context) {
        LayoutInflater.from(context).inflate(getIdentifier(context, "new_segment", "layout"), this, true);
        Resources resources = context.getResources();

        TypedValue rippleEffect = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleEffect, true);
        rippleEffectId = rippleEffect.resourceId;

        this.newSegmentContainer = (LinearLayout)this.findViewById(getIdentifier(context, "new_segment_container", "id"));

        this.rewindButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_rewind", "id"));
        if (this.rewindButton != null) {
            setClickEffect(this.rewindButton);
            this.rewindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Rewind button clicked"); }
                    PlayerController.skipRelativeMilliseconds(-SponsorBlockSettings.adjustNewSegmentMillis);
                }
            });
        }
        this.forwardButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_forward", "id"));
        if (this.forwardButton != null) {
            setClickEffect(this.forwardButton);
            this.forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Forward button clicked"); }
                    PlayerController.skipRelativeMilliseconds(SponsorBlockSettings.adjustNewSegmentMillis);
                }
            });
        }
        this.adjustButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_adjust", "id"));
        if (this.adjustButton != null) {
            setClickEffect(this.adjustButton);
            this.adjustButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Adjust button clicked"); }
                    SponsorBlockUtils.onMarkLocationClicked(NewSegmentHelperLayout.context);
                }
            });
        }
        this.compareButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_compare", "id"));
        if (this.compareButton != null) {
            setClickEffect(this.compareButton);
            this.compareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Compare button clicked"); }
                    SponsorBlockUtils.onPreviewClicked(NewSegmentHelperLayout.context);
                }
            });
        }
        this.editButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_edit", "id"));
        if (this.editButton != null) {
            setClickEffect(this.editButton);
            this.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Edit button clicked"); }
                    SponsorBlockUtils.onEditByHandClicked(NewSegmentHelperLayout.context);
                }
            });
        }
        this.publishButton = (ImageButton)this.findViewById(getIdentifier(context, "new_segment_publish", "id"));
        if (this.publishButton != null) {
            setClickEffect(this.publishButton);
            this.publishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (debug) { Log.d(TAG, "Publish button clicked"); }
                    SponsorBlockUtils.onPublishClicked(NewSegmentHelperLayout.context);
                }
            });
        }

        this.defaultBottomMargin = resources.getDimensionPixelSize(getIdentifier(context, "brand_interaction_default_bottom_margin", "dimen"));
        this.ctaBottomMargin = resources.getDimensionPixelSize(getIdentifier(context, "brand_interaction_cta_bottom_margin", "dimen"));
    }

    private void setClickEffect(ImageButton btn) {
        btn.setBackgroundResource(rippleEffectId);

        RippleDrawable rippleDrawable = (RippleDrawable)btn.getBackground();

        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled } };
        int[] colors = new int[] { 0x33ffffff }; // sets the ripple color to white

        ColorStateList colorStateList = new ColorStateList(states, colors);
        rippleDrawable.setColor(colorStateList);
    }

    private int getIdentifier(Context context, String name, String defType) {
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }
}
