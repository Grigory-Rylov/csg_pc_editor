package com.github.grishberg.viewer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.grishberg.cad3d.R;

import java.util.ArrayList;
import java.util.List;

public class LogListView extends ListView {
    private final List<String> logItems = new ArrayList<>();
    private final ArrayAdapter<String> adapter;

    public LogListView(Context context) {
        this(context, null);
    }

    public LogListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        adapter = new ArrayAdapter<>(context, R.layout.log_item, logItems);
        setAdapter(adapter);
    }

    public void addLog(String log) {
        logItems.add(log);
        adapter.notifyDataSetChanged();
        setSelection(logItems.size() - 1);
    }

    public void clearLogs() {
        logItems.clear();
        adapter.notifyDataSetChanged();
    }
} 