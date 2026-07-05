package com.github.grishberg.cad3d.ui;

import com.github.grishberg.viewer.LogListView;
import com.github.grishberg.cad3d.common.Logger;

public class LogListViewLogger implements Logger {
    private final LogListView logListView;

    public LogListViewLogger(LogListView logListView) {
        this.logListView = logListView;
    }

    @Override
    public void d(String msg) {
        logListView.addLog(msg);
    }

    public void clear() {
        logListView.clearLogs();
    }
} 