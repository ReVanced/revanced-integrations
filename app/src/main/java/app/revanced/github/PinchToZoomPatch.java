package app.revanced.github;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ViewGroup;

public final class PinchToZoomPatch {
    @SuppressLint("StaticFieldLeak")
    private static ViewGroup recyclerView;

    public static void addPinchToZoomGesture(final ViewGroup codeViewRecycleView) {
        if (recyclerView == codeViewRecycleView) return;
        recyclerView = codeViewRecycleView;

        recyclerView.setOnTouchListener(new PinchToZoomGesture(recyclerView.getContext()));

        Log.d("revanced", "Adding pinch to zoom event handler to " + codeViewRecycleView);
    }
}