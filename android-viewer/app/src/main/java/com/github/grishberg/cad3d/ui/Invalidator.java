package com.github.grishberg.cad3d.ui;

import java.util.List;

public interface Invalidator {
    public void run();
    public void invalidate(List<DebugPointUi> debugPoints);
}
