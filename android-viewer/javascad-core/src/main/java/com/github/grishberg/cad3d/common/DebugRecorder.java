package com.github.grishberg.cad3d.common;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.PolygonImproved;
import eu.printingin3d.javascad.vrl.VertexPosition;
import java.util.List;

public interface DebugRecorder {
    default void onLogEvent(String msg) {}
    default void onSplitPolygonStarted(PolygonImproved p1, PolygonImproved p2, String type) {}
    default void onSplitPolygonCompleted(List<V3d> f, List<V3d> b) {}
    default void onClassifyAndSplitVertex(V3d a, V3d b, VertexPosition pos) {}
    default void onEdgeSpanning(V3d a, V3d b, V3d c) {}
    default void onAddVertexNextPoint(List<V3d> list, V3d a, V3d b, V3d c, VertexPosition d, V3d e) {}
    default void onEdgeCross(List<V3d> list, V3d a, V3d b, V3d c, V3d d) {}
}
