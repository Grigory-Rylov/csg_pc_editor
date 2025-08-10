
package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import java.util.ArrayList;
import java.util.List;

public class SplitPoltgonCompletedCmd implements DebugCmd {

    private final SplitStartedCmd splitStartedCmd;
    private final List<V3d> front;
    private final List<V3d> back;

    public SplitPoltgonCompletedCmd(
        List<V3d> front,
        List<V3d> back,

        SplitStartedCmd splitStartedCmd
    ) {

        this.splitStartedCmd = splitStartedCmd;
        this.front = new ArrayList<>(front);
        this.back = new ArrayList<>(back);
    }

    @Override
    public String getDescription() {
        return "Split completef - front: " + front.size() + ", back = " + back.size();
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {
        if (splitStartedCmd != null) {
            splitStartedCmd.render(debugVisualizer);
        }
        for (V3d p : back) {
            debugVisualizer.drawDebugPoint(p, 2, Color.BLUE);
        }

        for (V3d p : front) {
            debugVisualizer.drawDebugPoint(p, 2, Color.GREEN);
        }
    }
} 
