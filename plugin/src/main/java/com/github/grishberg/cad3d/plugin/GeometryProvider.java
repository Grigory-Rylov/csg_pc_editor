package com.github.grishberg.cad3d.plugin;

import java.util.List;

interface GeometryProvider {

    List<VertexHolder> getVertexHolders();

    String getName();
}
