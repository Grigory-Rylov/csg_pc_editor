package eu.printingin3d.javascad.utils.optimizator;

public interface ProgressObserver {

    void onProgress(int progress);

    ProgressObserver STUB = new ProgressObserver() {
        @Override
        public void onProgress(int progress) {
            // no op
        }
    };
}
