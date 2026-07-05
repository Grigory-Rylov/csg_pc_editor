package com.github.grishberg.cad3d.ui;

import android.opengl.GLSurfaceView;

public interface ControlledRenderer extends GLSurfaceView.Renderer {
    void setScale(float scaleFactor);
    float getScale();

    void setAngleX(float angle);
    void setAngleY(float angle);

    float getAngleX();
    float getAngleY();

    void setTranslateX(float x);
    void setTranslateY(float y);
}
