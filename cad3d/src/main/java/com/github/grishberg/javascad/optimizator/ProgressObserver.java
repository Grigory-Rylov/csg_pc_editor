package com.github.grishberg.javascad.optimizator;

public interface ProgressObserver {

    void onProgress(int progress);

    ProgressObserver STUB = new ProgressObserver() {
        @Override
        public void onProgress(int progress) {
            // no op
        }
    };
}
