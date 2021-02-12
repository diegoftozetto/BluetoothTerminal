package com.example.chronopassbluetoothterminal.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class MatrixLedAdapter extends BaseAdapter {

    private final Context context;
    private final String[] strings;
    private final List<Integer> selectedPositions;
    private final int color;

    public MatrixLedAdapter(Context context, String[] strings, List<Integer> selectedPositions, int color) {
        this.context = context;
        this.strings = strings;
        this.selectedPositions = selectedPositions;
        this.color = color;
    }

    public void addSelectedPositions(int position) {
        this.selectedPositions.add(position);
    }

    public void removeSelectedPositions(int position) {
        this.selectedPositions.remove(position);
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    @Override
    public int getCount() {
        return strings.length;
    }

    @Override
    public Object getItem(int position) {
        return strings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MatrixItemView customView = (convertView == null) ? new MatrixItemView(context) : (MatrixItemView) convertView;
        customView.display(selectedPositions.contains(position), color);

        return customView;
    }

}