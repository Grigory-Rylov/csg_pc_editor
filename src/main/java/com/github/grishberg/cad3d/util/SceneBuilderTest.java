package com.github.grishberg.cad3d.util;

import com.github.grishberg.cad3d.debug.DebugRecorder;
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig;
import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.Triangulator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.models.IModel;
import eu.printingin3d.javascad.models.Sphere;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator;
import eu.printingin3d.javascad.utils.StlValidator;
import eu.printingin3d.javascad.vrl.CSG;
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.FacetGenerationContext;
import eu.printingin3d.javascad.vrl.Polygon;
import eu.printingin3d.javascad.vrl.VertexHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SceneBuilderTest implements SceneBuilder {

    private static final boolean SHOW_GROUPED_EDGES = false;
    private static final boolean SHOW_TRIANGULATION = false;
    private static final boolean SHOW_NAKED_EDGES = false;
    public final List<VertexHolder> buffers;
    private final DebugRecorder debugRecorder;

    public SceneBuilderTest(DebugRecorder debugRecorder) {
        this.debugRecorder = debugRecorder;
        buffers = new ArrayList<>();
    }

    private ReadyListener listener;

    @Override
    public void setListener(ReadyListener listener) {
        this.listener = listener;
    }

    @Override
    public void rebuildModels(@NotNull KeyboardConfig cfg) {

        create3dModels();
    }

    private void create3dModels() {
        Abstract3dModel cube = new Cube(50.0);
        Abstract3dModel sphere = new Sphere(25.0).move(0, 0, 40.0);
        Abstract3dModel model = cube.addModel(sphere);
        CSG result = model.toCSG();

        debugRecorder.clear();

        List<Polygon> polygons = result.getPolygons();
        List<Polygon> fixedPolygons = PolygonValidator.fixPolygons(polygons);
        createAndAdd(fixedPolygons);

        List<Polygon> topPolygons = filterTopPolygons(fixedPolygons, 25);
        debugRecorder.onEnd(fixedPolygons);

        List<Facet> facetsFromPolygons = Triangulator.triangulate(fixedPolygons);
        debugRecorder.onEndFacets(facetsFromPolygons);


//        List<Polygon> referencePolygons = new STLParser().parseBinarySTL("reference.stl");
//        debugRecorder.onEnd(referencePolygons);

        // validation

        List<PolygonValidator.PolygonNakedEdgeInfo> info =
            new PolygonValidator().analyzeNakedEdges(topPolygons);

        for (PolygonValidator.PolygonNakedEdgeInfo infoItem : info) {
            /*
            debugRecorder.onDebugNakedEdgeWithFacets(
                infoItem.getPolygon(), infoItem.getNakedEdgeA(),
                infoItem.getNakedEdgeB(), infoItem.getFacet(),
                facetsFromPolygons
            );
             */
        }

        // debug polygon grouping
        Map<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> nearbyPolygons =
            PolygonValidator.getCommonPolygons(topPolygons);

        List<StlValidator.NakedEdgeInfo> nakedEdges =
            StlValidator.analyzeNakedEdges(facetsFromPolygons);
        System.out.println("Neked edges: " + nakedEdges.size());
        if (SHOW_NAKED_EDGES) {
            for (StlValidator.NakedEdgeInfo nakedEdge : nakedEdges) {
                PolygonValidator.LineKey lineKey = PolygonValidator.LineKey.fromSegment(
                    nakedEdge.getPointA(),
                    nakedEdge.getPointB()
                );
                List<PolygonValidator.PolygonEdge> nearby = nearbyPolygons.get(lineKey);

                debugRecorder.onDebugNakedEdgeWithNearbyFacets(
                    null,
                    nakedEdge.getPointA(),
                    nakedEdge.getPointB(),
                    nakedEdge.getFacet(),
                    nearby,
                    facetsFromPolygons
                );
            }
        }

        if (SHOW_GROUPED_EDGES) {
            for (Map.Entry<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> entry :
                nearbyPolygons.entrySet()) {
                debugRecorder.onGroupedEdge(entry.getKey(), entry.getValue());
            }
        }

        if (SHOW_TRIANGULATION) {
            for (Polygon p : fixedPolygons) {
                List<Facet> facets = Triangulator.triangulate(p);
                debugRecorder.onDebugPolygonTriangulation(p, facets);
            }
        }

        if (listener != null) {
            listener.onReady(buffers);
        }
    }

    private List<Polygon> filterTopPolygons(List<Polygon> src, double z) {
        ArrayList<Polygon> result = new ArrayList<>();

        for (Polygon p : src) {
            int count = 0;
            for (V3d v : p.getVertices()) {
                if (v.z == z) {
                    count++;
                }
            }
            if (count == p.getVertices().size()) {
                result.add(p);
            }
        }
        return result;
    }

    private List<Facet> triangulate(List<Polygon> polygons) {
        List<Facet> facetsFromPolygons = new ArrayList<>();
        for (Polygon p : polygons) {
            facetsFromPolygons.addAll(triangulatePolygon(p));
            //debugRecorder.onDebugPolygonTriangulation(p, localFacet);
        }
        return facetsFromPolygons;
    }

    private static List<Facet> triangulatePolygon(Polygon p) {
        List<Facet> facetsFromPolygons = new ArrayList<>();
        List<Triangle3d> triangles = Triangulator.triangulate(p.getVertices(), p.getNormal());
        List<Facet> localFacet = new ArrayList<>();
        for (Triangle3d t : triangles) {
            facetsFromPolygons.add(new Facet(t, p.getNormal(), p.getColor()));
            localFacet.add(new Facet(t, p.getNormal(), p.getColor()));
        }
        return facetsFromPolygons;
    }

    private void createAndAdd(IModel model) {
        createAndAdd(model, Color.GRAY);
    }

    private VertexHolder createAndAdd(IModel model, Color color) {
        return createAndAdd(model, color, 6);
    }

    private VertexHolder createAndAdd(IModel model, Color color, int fn) {
        FacetGenerationContext context = new ColorFacetGenerationContext(color);
        context.setFn(fn);
        CSG csg = model.toCSG(context);
        VertexHolder vertex = csg.getVerticesAndColorsAsFloatArray();
        buffers.add(vertex);
        return vertex;
    }

    private VertexHolder createAndAdd(List<Polygon> polygons) {
        return createAndAdd(polygons, Color.GRAY);
    }

    private VertexHolder createAndAdd(List<Polygon> polygons, Color color) {
        VertexHolder vertex = VertexHolder.fromPolygons(polygons, color);
        buffers.add(vertex);
        return vertex;
    }
}
