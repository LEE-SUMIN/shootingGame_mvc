package com.example.shootinggame2;

import android.graphics.Point;
import android.view.Display;

public class MyDisplay {
    public static int width;
    public static int height;

    public static void setup(Display display) {
        Point size = new Point();
        display.getRealSize(size);
        width = size.x;
        height = (int) (size.y * 0.8);
    }
}