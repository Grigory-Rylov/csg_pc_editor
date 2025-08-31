package com.github.grishberg.cad3d.plugin;

import java.util.List;

public interface ResultListener {

    void onReady(List<VertexHolder> vertexHolders);
}
