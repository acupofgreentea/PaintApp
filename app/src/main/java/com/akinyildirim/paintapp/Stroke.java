package com.akinyildirim.paintapp;

import android.graphics.Path;

public class Stroke  {
    private int color;
    private int strokeWidth;
    private Path path;
    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Path getPath() {
        return path;
    }
}
