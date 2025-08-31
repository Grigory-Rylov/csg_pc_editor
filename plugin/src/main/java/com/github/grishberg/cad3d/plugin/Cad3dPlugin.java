package com.github.grishberg.cad3d.plugin;

public interface Cad3dPlugin {

    void requestModels(Config config, ResultListener listener);

    String getName();

    long getVersion();
}
