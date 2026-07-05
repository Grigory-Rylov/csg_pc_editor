package com.github.grishberg.cad3d.ui;

import android.graphics.Color;

public class DebugPointUi {
    public final int x;
    public final int y;
    public final Color color;
    public final String title;


    public DebugPointUi(int x, int y, Color color, String title) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.title = title;
    }
}
