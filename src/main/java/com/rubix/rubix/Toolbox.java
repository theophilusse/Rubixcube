package com.rubix.rubix;

import com.rubix.rubix.AxisVectors;

public class Toolbox {

    public AxisVectors axis;
    public float[]      tmp;
    public float[]      colorVec;
    public int[]        point;
    public int[]        anaglyphPoint;
    static public float rad90 = (float)(Math.PI / 2);
    static public float rad180 = (float)Math.PI;
    static public float rad360 = (float)Math.PI * 2;
    static public float pi = (float)Math.PI;
    static public float pi2 = (float)(Math.PI * 2);

    public Toolbox(int numPoints)
    {
        axis = new AxisVectors();
        point = new int[numPoints * 2];
        anaglyphPoint = new int[numPoints * 2];
        tmp = new float[3];
        colorVec = new float[3];
    }

    /*
    public float[]      getDown()
    {
        return (down.clone());
    }

    public float[]      getUp()
    {
        return (up.clone());
    }

    public float[]      getFront()
    {
        return (front.clone());
    }

    public float[]      getBack()
    {
        return (back.clone());
    }

    public float[]      getLeft()
    {
        return (left.clone());
    }

    public float[]      getRight()
    {
        return (right.clone());
    }
    */

    static public float[]       vecAt(float[] out, float[] vertice, int offset)
    {
        offset *= 3; // Todo ::: TESTING
        out[0] = vertice[offset++];
        out[1] = vertice[offset++];
        out[2] = vertice[offset];
        return (out);
    }

    // -> RTv1
    static public float[] rotVec(float[] output, float[] axis, float[] lev, float a)
    {
        float				cosa;
        float				sina;

        cosa = (float)Math.cos(a);
        sina = (float)Math.sin(a);
        output[0] = (cosa + (1 - cosa) * axis[0] * axis[0]) * lev[0];
        output[0] += ((1 - cosa) * axis[0] * axis[1] - axis[2] * sina) * lev[1];
        output[0] += ((1 - cosa) * axis[0] * axis[2] + axis[1] * sina) * lev[2];
        output[1] = ((1 - cosa) * axis[0] * axis[1] + axis[2] * sina) * lev[0];
        output[1] += (cosa + (1 - cosa) * axis[1] * axis[1]) * lev[1];
        output[1] += ((1 - cosa) * axis[1] * axis[2] - axis[0] * sina) * lev[2];
        output[2] = ((1 - cosa) * axis[0] * axis[2] - axis[1] * sina) * lev[0];
        output[2] += ((1 - cosa) * axis[1] * axis[2] + axis[0] * sina) * lev[1];
        output[2] += (cosa + (1 - cosa) * axis[2] * axis[2]) * lev[2];
        return (output);
    }

    static public void      rot90clockwise2d(float[] out, float[] in)
    {
        out[0] = in[1] * 1;
        out[1] = in[0] * -1;
    }

    static public void      rot90counterClockwise2d(float[] out, float[] in)
    {
        out[0] = in[1] * -1;
        out[1] = in[0] * 1;
    }

    static public void      rotDotClockwise2d(float[] out, float[] in, float dot)
    {
        out[0] = in[1] * dot;
        out[1] = in[0] * (dot * -1);
    }

    static public void      rotDotCounterClockwise2d(float[] out, float[] in, float dot)
    {
        out[0] = in[1] * (dot * -1);
        out[1] = in[0] * dot;
    }

    static public int getColor(int r, int g, int b)
    {
        int color;

        color = r << 16;
        color += g << 8;
        color += b;
        return (0xff000000 | color);
    }

    static public int getColorClamp(int r, int g, int b)
    {
        int color;

        r = Math.min(r, 255);
        g = Math.min(g, 255);
        b = Math.min(b, 255);
        color = r << 16;
        color += g << 8;
        color += b;
        return (0xff000000 | color);
    }

    static public int mixColor(int cA, int cB)
    {
        int rA;
        int gA;
        int bA;
        int rB;
        int gB;
        int bB;

        rA = (int)((cA & 0x00ff0000) >> 16);
        gA = (int)((cA & 0x0000ff00) >> 8);
        bA = (int)(cA & 0x000000ff);
        rB = (int)((cB & 0x00ff0000) >> 16);
        gB = (int)((cB & 0x0000ff00) >> 8);
        bB = (int)(cB & 0x000000ff);
        return (mixColor(rA, gA, bA, rB, gB, bB));
    }

    static public int getRed(int c)
    {
        return (c & 0x00ff0000);
    }

    static public int getGreen(int c)
    {
        return (c & 0x0000ff00);
    }

    static public int getBlue(int c)
    {
        return (c & 0x000000ff);
    }

    static public int mixColor(int rA, int gA, int bA, int rB, int gB, int bB)
    {
        return (getColor((rA + rB) / 2, (gA + gB) / 2, (bA + bB) / 2));
    }

    static public int addColor(int color, int value)
    {
        return (getColorClamp(getRed(color) + value, getGreen(color) + value, getBlue(color) + value));
    }

    static public int addColorColor(int color, int value)
    {
        int r;
        int g;
        int b;
        int ro;
        int go;
        int bo;
        int origin;

        r = (getRed(color) + getRed(value));
        g = (getGreen(color) + getGreen(value));
        b = (getBlue(color) + getBlue(value));
        ro = 255 - r;
        go = 255 - g;
        bo = 255 - b;
        origin = 0;
        if (ro < 0)
            origin += ro * -1;
        if (go < 0)
            origin += go * -1;
        if (bo < 0)
            origin += bo * -1;
        if (origin != 0)
        {
            if (r < 255)
                r += origin;
            if (g < 255)
                g += origin;
            if (b < 255)
                b += origin;
        }
        return (getColor(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)));
    }

    static public int mulColor(int color, float factor)
    {
        int r;
        int g;
        int b;

        r = Math.min(255, Math.max(0, (int)((float)((color & 0x00ff0000) >> 16) * factor)));
        g = Math.min(255, Math.max(0, (int)((float)((color & 0x0000ff00) >> 8) * factor)));
        b = Math.min(255, Math.max(0, (int)((float) (color & 0x000000ff) * factor)));
        return (getColor(r, g, b));
    }

    static public int mulColorReal(int color, float factor)
    {
        int r;
        int g;
        int b;
        int ro;
        int go;
        int bo;
        int origin;

        r = (int)((float)((color & 0x00ff0000) >> 16) * factor);
        g = (int)((float)((color & 0x0000ff00) >> 8) * factor);
        b = (int)((float) (color & 0x000000ff) * factor);
        ro = 255 - r;
        go = 255 - g;
        bo = 255 - b;
        origin = 0;
        if (ro < 0)
            origin += ro * -1;
        if (go < 0)
            origin += go * -1;
        if (bo < 0)
            origin += bo * -1;
        if (origin != 0)
        {
            if (r < 255)
                r += origin;
            if (g < 255)
                g += origin;
            if (b < 255)
                b += origin;
        }
        return (getColor(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)));
    }

    static public float[] subColor(float[] out, int colorA, int colorB)
    {
        out[0] = (int)(colorA & 0x00ff0000) - (int)(colorB & 0x00ff0000);
        out[1] = (int)(colorA & 0x0000ff00) - (int)(colorB & 0x0000ff00);
        out[2] = (int)(colorA & 0x000000ff) - (int)(colorB & 0x000000ff);
        return (out);
    }

    public int colorSpace(int colorFrom, int colorTo, float factor)
    {
        factor = Math.abs(factor);
        colorVec = subColor(tmp, colorTo, colorFrom);
        colorVec[0] *= factor;
        colorVec[0] += (float)(colorFrom & 0x00ff0000);
        colorVec[1] *= factor;
        colorVec[1] += (float)(colorFrom & 0x0000ff00);
        colorVec[2] *= factor;
        colorVec[2] += (float)(colorFrom & 0x000000ff);
        return (getColor((int)colorVec[0], (int)colorVec[1], (int)colorVec[2]));
    }

    static public double       magVector(float[] vector)
    {
        float x;
        float y;
        float z;

        x = vector[0];
        y = vector[1];
        z = vector[2];
        x *= x;
        y *= y;
        z *= z;
        z = x + y + z;
        if (z == 0f)
            return (0);
        return (Math.sqrt(z));
    }

    static public double       magVector(float x, float y, float z)
    {
        double     w;

        x *= x;
        y *= y;
        z *= z;
        w = x + y + z;
        if (w == 0)
            return (0);
        return (Math.sqrt(w));
    }

    static public double       magVector2d(float x, float y)
    {
        double w;

        w = (x * x) + (y * y);
        if (w == 0)
            return (0);
        return (Math.sqrt(w));
    }

    static public double       magVector2d(float[] vector)
    {
        float x;
        float y;
        float z;

        x = vector[0];
        y = vector[1];
        x *= x;
        y *= y;
        z = x + y;
        if (z == 0f)
            return (0);
        return (Math.sqrt(z));
    }

    static public float         dotProd(float[] a, float[] b)
    {
        return (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
    }

    static public float         dotProd2d(float[] a, float[] b)
    {
        return (a[0] * b[0] + a[1] * b[1]);
    }

    static public void          crossProd2(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] * b[1] - a[1] * b[0];
        out[1] = a[1] * b[0] - a[0] * b[1];
    }

    static public void          crossProd(float[] out, float[] a, float[] b)
    {
        out[0] = a[1] * b[2] - a[2] * b[1];
        out[1] = a[2] * b[0] - a[0] * b[2];
        out[2] = a[0] * b[1] - a[1] * b[0];
    }

    static public float[]          revVector(float[] a)
    {
        a[0] *= -1;
        a[1] *= -1;
        a[2] *= -1;
        return (a);
    }

    static public void             revVectorCopy(float[] out, float[] a)
    {
        out[0] = a[0] * -1;
        out[1] = a[1] * -1;
        out[2] = a[2] * -1;
    }

    static public void  copyVec(float[] dst, float[] src)
    {
        dst[0] = src[0];
        dst[1] = src[1];
        dst[2] = src[2];
    }

    static public float[]          mulVector(float[] a, float v)
    {
        a[0] *= v;
        a[1] *= v;
        a[2] *= v;
        return (a);
    }

    static public int[]          mulVector2di(int[] a, float v)
    {
        a[0] = (int)((float)a[0] * v);
        a[1] = (int)((float)a[1] * v);
        return (a);
    }

    static public float[]          mulVector2d(float[] a, float v)
    {
        a[0] *= v;
        a[1] *= v;
        return (a);
    }

    static public float[]          mulVector2difCopy(float[] out, float[] a, float v)
    {
        out[0] = a[0] * v;
        out[1] = a[1] * v;
        return (out);
    }

    static public int[]          mulVector2difCopy(int[] out, int[] a, float v)
    {
        out[0] = (int)((float)a[0] * v);
        out[1] = (int)((float)a[1] * v);
        return (out);
    }

    static public void             mulVectorVector(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] * b[0];
        out[1] = a[1] * b[1];
        out[2] = a[2] * b[2];
    }

    static public float[] scaleModel(float[] vertice, float scale)
    {
        int sz;

        sz = vertice.length;
        for (int i = 0; i < sz; i++)
            vertice[i] *= scale;
        return (vertice);
    }

    static public float[]          subVector(float[] a, float[] b)
    {
        a[0] -= b[0];
        a[1] -= b[1];
        a[2] -= b[2];
        return (a);
    }

    static public void             subVectorCopy(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
        out[2] = a[2] - b[2];
    }

    static public void             subVector2dCopy(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
    }

    static public void             subVector2diCopy(int[] out, int[] a, int[] b)
    {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
    }

    static public void             subVector2difCopy(float[] out, int[] a, int[] b)
    {
        out[0] = (float)(a[0] - b[0]);
        out[1] = (float)(a[1] - b[1]);
    }

    static public float[]          addVector(float[] a, float[] b)
    {
        a[0] += b[0];
        a[1] += b[1];
        a[2] += b[2];
        return (a);
    }

    static public void             addVectorCopy(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] + b[0];
        out[1] = a[1] + b[1];
        out[2] = a[2] + b[2];
    }

    static public void             addVector2dCopy(float[] out, float[] a, float[] b)
    {
        out[0] = a[0] + b[0];
        out[1] = a[1] + b[1];
    }

    static public void             addVector2diCopy(int[] out, int[] a, int[] b)
    {
        out[0] = a[0] + b[0];
        out[1] = a[1] + b[1];
    }

    static public void             addVector2diCopy(int[] out, float[] a, float[] b)
    {
        out[0] = (int)(a[0] + b[0]);
        out[1] = (int)(a[1] + b[1]);
    }

    static public void             addVector2difCopy(float[] out, int[] a, int[] b)
    {
        out[0] = (float)(a[0] + b[0]);
        out[1] = (float)(a[1] + b[1]);
    }

    static public float[]          normalize(float[] out, float[] vector)
    {
        float       len;

        len = (float)magVector(vector);
        if (len == 0)
            return (out);
        out[0] = vector[0] / len;
        out[1] = vector[1] / len;
        out[2] = vector[2] / len;
        return (out);
    }

    static public float[]          normalize2d(float[] out, float[] vector)
    {
        float       len;

        len = (float)magVector2d(vector);
        if (len == 0)
            return (out);
        out[0] = vector[0] / len;
        out[1] = vector[1] / len;
        return (out);
    }

    /* Bresenham */
    //static private void plotLineLow(Bitmap surface, int color, int width, int height, int x0, int y0, int x1, int y1) {
    static private void plotLineLow(int[] surface, int color, int width, int height, int x0, int y0, int x1, int y1)
    {
        int dx;
        int dy;
        int yi;
        int D;
        int y;

        dx = x1 - x0;
        dy = y1 - y0;
        yi = 1;
        if (dy < 0)
        {
            yi = -1;
            dy = -dy;
        }
        D = (2 * dy) - dx;
        y = y0;
        for (int x = x0; x < x1; x++)
        {
            putPixel(surface, x, y, width, height, color);
            if (D > 0)
            {
                y = y + yi;
                D = D + (2 * (dy - dx));
            }
            else
                D = D + 2 * dy;
        }
    }

    //static private void plotLineHigh(Bitmap surface, int color, int width, int height, int x0, int y0, int x1, int y1)
    static private void plotLineHigh(int[] surface, int color, int width, int height, int x0, int y0, int x1, int y1)
    {
        int dx;
        int dy;
        int xi;
        int D;
        int x;

        dx = x1 - x0;
        dy = y1 - y0;
        xi = 1;
        if (dx< 0)
        {
            xi = -1;
            dx = -dx;
        }
        D = (2 * dx) - dy;
        x = x0;
        for (int y = y0; y < y1; y++)
        {
            putPixel(surface, x, y, width, height, color);
            if (D > 0)
            {
                x = x + xi;
                D = D + (2 * (dx - dy));
            }
            else
                D = D + 2 * dx;
        }
    }

    //static public void drawLine(Bitmap surface, int width, int height, int color, int x0, int y0, int x1, int y1)
    static public void drawLine(int[] surface, int width, int height, int color, int x0, int y0, int x1, int y1)
    {
        if (Math.abs(y1 - y0) < Math.abs(x1 - x0))
        {
            if (x0 > x1)
                plotLineLow(surface, color, width, height, x1, y1, x0, y0);
            else
                plotLineLow(surface, color, width, height, x0, y0, x1, y1);
        }
        else
        {
            if (y0 > y1)
                plotLineHigh(surface, color, width, height, x1, y1, x0, y0);
            else
                plotLineHigh(surface, color, width, height, x0, y0, x1, y1);
        }
    }

    //static public void putPixel(Bitmap surface, int x, int y, int width, int height, int color)
    static public void putPixel(int[] surface, int x, int y, int width, int height, int color)
    {
        /*
            y = x;
            x = width - x + 1;
        */
        if (x < 0 || y < 0 || x >= width || y >= height)
            return ;
        //surface.setPixel(x, y, color);
        surface[y * width + x] = color;
    }
}
