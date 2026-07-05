package com.github.grishberg.cad3d.common;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

public class DebugPoint {

    public final V3d point;
    public final String title;
    public final Color color;

    public DebugPoint(V3d point, String title, Color color) {
        this.point = point;
        this.title = title;
        this.color = color;
    }
}
