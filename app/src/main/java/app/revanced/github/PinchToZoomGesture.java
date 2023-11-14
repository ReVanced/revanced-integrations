package app.revanced.github;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PinchToZoomGesture implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private View view;
    private final ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;

    public PinchToZoomGesture(Context c){ gestureScale = new ScaleGestureDetector(c, this); }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        view.onTouchEvent(event);
        return gestureScale.onTouchEvent(event);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor); // prevent our view from becoming too small //
        scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }
}