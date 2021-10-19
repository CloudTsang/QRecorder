package com.meili.mnist.widget;

import android.graphics.Point;
import android.graphics.Path;

public class LineObject {
    public Point startPoint;
    public Point endPoint;
    public Boolean isDash;
    //直线
    public Path path;
    //延长线、位置校准线
    public Path antPath;

    public LineObject(Point sp, Point ep, boolean isd){
        startPoint = sp;
        endPoint = ep;
        isDash = isd;
    }

    public void setPaths(Path p1, Path p2){
        path = p1;
        antPath = p2;
    }
}
