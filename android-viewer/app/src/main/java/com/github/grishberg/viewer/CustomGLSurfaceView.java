package com.github.grishberg.viewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.github.grishberg.cad3d.ui.ControlledRenderer;

public class CustomGLSurfaceView extends GLSurfaceView {

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final float TOUCH_MOVE_FACTOR = 5;
    private float previousX;
    private float previousY;
    private float scaleFactor = 1.0f;
    private float translateX = 0.0f;
    private float translateY = 0.0f;
    private ScaleGestureDetector scaleDetector;
    private ControlledRenderer renderer;
    private float mDensity;
    private boolean skipRender;

    public CustomGLSurfaceView(Context context) {
        this(context, null);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Let the ScaleGestureDetector inspect all events.
        scaleDetector.onTouchEvent(e);

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                previousX = x;
                previousY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                if (e.getPointerCount() == 1 && !scaleDetector.isInProgress()) {
                    // Rotation
                    float dx = x - previousX;
                    float dy = y - previousY;

                    if (skipRender) {
                        skipRender = false;
                    } else {
                        renderer.setAngleX(renderer.getAngleX() + dy * TOUCH_SCALE_FACTOR);
                        renderer.setAngleY(renderer.getAngleY() + dx * TOUCH_SCALE_FACTOR);
                        requestRender();
                        skipRender = false;
                    }
                } else if (e.getPointerCount() == 2 && !scaleDetector.isInProgress()) {
                    // Translation
                    float dx = (x - previousX) * renderer.getScale() * TOUCH_MOVE_FACTOR;
                    float dy = (y - previousY) * renderer.getScale() * TOUCH_MOVE_FACTOR;

                    translateX += dx * 0.01f;
                    translateY -= dy * 0.01f;

                    renderer.setTranslateX(translateX);
                    renderer.setTranslateY(translateY);
                    requestRender();
                    skipRender = true;
                }
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            renderer.setScale(scaleFactor);
            requestRender();
            return true;
        }
    }


    // Hides superclass method.
    public void setRenderer(ControlledRenderer renderer, float density) {
        this.renderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
    }

}
	
