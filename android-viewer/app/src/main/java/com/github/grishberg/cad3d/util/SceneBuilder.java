package com.github.grishberg.cad3d.util;

import com.github.grishberg.cad3d.common.DebugPoint;
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import eu.printingin3d.javascad.vrl.VertexHolder;

public interface SceneBuilder {

   void requestBuffers();

   void setListener(ReadyListener listener);

   void setConfig(@NotNull KeyboardConfig cfg);

   interface ReadyListener {
      void onReady(List<VertexHolder> buffers);

      void onReady(List<VertexHolder> buffers, List<DebugPoint> newDebugPoints);
   }
}
