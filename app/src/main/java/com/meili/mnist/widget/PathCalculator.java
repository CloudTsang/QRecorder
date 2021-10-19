package com.meili.mnist.widget;

import android.graphics.Point;
import android.util.Log;

import static java.lang.Float.NaN;

public class PathCalculator {

    public static float[] getBottomLine(float x1, float y1, float x2, float y2, float len){
        if(x1==x2){
            float[] a={x1-len, y2, x1+len, y2};
            return a;
        }
        if(y1==y2){
            Log.i("mnist", "y1==y2");
            float[] a={x2, y2-len, x2, y2+len};
            return a;
        }
        float[] kb = getkbVert(x1,y1,x2,y2);
        float k = kb[0];
        float b = kb[1];
            //k = y/x
            //x^2+(kx)^2 = len^2
        float ofsX = (float) Math.sqrt(Math.pow(len,2) / (1+Math.pow(k,2)));
        float x3 = x2-ofsX;
        float x4 = x2+ofsX;
        float y3 = k * x3 + b;
        float y4 = k * x4 + b;
        float[] a={x3,y3,x4,y4};
        Log.i("mnist", k+"  "+b+"   "+x1+"   "+y1+"   "+x2+"   "+y2+"   "+x3+"   "+y3+"   "+x4+"   "+y4);
        return a;

    }

    //第二条线xy 上底xy 下底xy
    public static float[] getParellLine(float x, float y, float x1, float y1, float x2, float y2){
        if(x1==x2){
            float[] a={x,y1,x,y2};
            return a;
        }
        if(y1==y2){
            float[] a={x1,y,x2,y};
            return a;
        }
        float[] kb = getkb(x1,y1,x2,y2);
        float k = kb[0];
        float b = y - k*x;

        float[] kbvert1 = getkbVert(x1,y1,x2,y2);
        float k1 = kbvert1[0];
        float b1 = kbvert1[1];
        //kx+b = k1x+b1
        //(k-k1)x = (b1-b)
        float x11 = (b1-b)/(k-k1);
        float y11 = k*x11+b;

        float[] kbvert2 = getkbVert(x2,y2,x1,y1);
        float k2 = kbvert2[0];
        float b2 = kbvert2[1];
        float x22 = (b2-b)/(k-k2);
        float y22 = k*x22+b;

        float[] a = {x11, y11, x22, y22};
        return a;
    }

    public static float[] getkb(float x1, float y1, float x2, float y2){
        float k = 1;
        float b = 0;
        if(x2 == x1){
            k = NaN;
            b = 0;
        }else if(y1 == y2){
            k = 0;
            b = y2;
        } else{
            k = (y2-y1)/(x2-x1);
            b = y2-k*x2;
        }
        float[] a = {k, b};
        return a;
    }

    public static float[] getkbVert(float x1, float y1, float x2, float y2){
        float k = 1;
        float b = 0;
        if(x2 == x1){
            k = 0;
            b = y2;
        }else if(y1 == y2){
            k = NaN;
            b = 0;
        }else{
            k = (y2-y1)/(x2-x1);
            k = -1/k;
            b = y2-k*x2;
        }
        float[] a = {k, b};
        return a;
    }

    public static float[] kbToABC(float[] kb){
        float k = kb[0];
        float b = kb[1];
        //Ax+By+C = 0
        float A = k;
        float B = -1;
        float C = b;
        if(k == NaN){
            A = 1;
            B = 0;
        }
        float[] a = {A,B,C};
        return a;
    }

    public static Point warpPointVert(Point oript1, Point oript2, Point npt1, Point npt2, float scaleX, float scaleY){
        float[] kb0=  getkb(oript1.x, oript1.y, oript2.x, oript2.y);
        float k0 = 0;
        if(kb0[0]<0){
            k0 = kb0[0] * scaleX / scaleY;
        }else{
            k0 = kb0[0] / scaleX * scaleY;
        }

        float b0 = npt1.y - k0*npt1.x;
        float k1 = -1/k0;
        float b1 = npt2.y - k1*npt2.x;
        float nx = (b1-b0)/(k0-k1);
        float ny = k1*nx+b1;
        return new Point((int)nx, (int)ny);
    }

}
