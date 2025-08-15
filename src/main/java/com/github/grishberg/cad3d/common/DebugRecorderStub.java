package com.github.grishberg.cad3d.common;

import eu.printingin3d.javascad.utils.PolygonValidator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;
import eu.printingin3d.javascad.vrl.VertexPosition;
import java.util.List;

public class DebugRecorderStub implements DebugRecorder {

    @Override
    public List<DebugCmd> getCommands() {
        return List.of();
    }

    @Override
    public void clear() {

    }

    @Override
    public int getCommandCount() {
        return 0;
    }

    @Override
    public DebugCmd getCommand(int index) {
        return null;
    }

    @Override
    public void onSplitPolygonStarted(
        Polygon polygon,
        Polygon polygon1,
        VertexPosition polygonType
    ) {

    }

    @Override
    public void onClassifyAndSplitVertex(
        V3d currentVertex,
        V3d nextVertex,
        VertexPosition position
    ) {

    }

    @Override
    public void onEdgeSpanning(V3d currentVertex, V3d nextVertex, V3d v) {

    }

    @Override
    public void onEdgeCross(
        List<V3d> list,
        V3d currentVertex,
        V3d nextVertex,
        V3d cross,
        VertexPosition vp
    ) {

    }

    @Override
    public void onAddVertexNextPoint(
        List<V3d> list,
        V3d lastVertex,
        V3d nextVertex,
        V3d prev,
        V3d curr,
        VertexPosition vp
    ) {

    }

    @Override
    public void onSplitPolygonCompleted(List<V3d> front, List<V3d> back) {

    }

    @Override
    public void onEnd(List<Polygon> polygons) {

    }

    @Override
    public void onEnd(Polygon polygon) {

    }

    @Override
    public void onEndFacets(List<Facet> facets) {

    }

    @Override
    public void onLogEvent(String message) {

    }

    @Override
    public void onLogEvent(String message, List<V3d> points) {

    }

    @Override
    public void onDebugNakedEdge(Polygon polygon, V3d nakedEdgeA, V3d nakedEdgeB, Facet facet) {

    }

    @Override
    public void onDebugPolygonTriangulation(Polygon polygon, List<Facet> facets) {

    }

    @Override
    public void onGroupedEdge(
        PolygonValidator.LineKey key,
        List<PolygonValidator.PolygonEdge> value
    ) {

    }
}
