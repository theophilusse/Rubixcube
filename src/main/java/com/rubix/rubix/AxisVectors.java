package com.rubix.rubix;

import static com.rubix.rubix.Toolbox.*;

public class AxisVectors {
    public float[]     down;
    public float[]     up;
    public float[]     front;
    public float[]     back;
    public float[]     left;
    public float[]     right;
    public float[]     tmp;
    public float[]     tmp_;

    public AxisVectors()
    {
        tmp = new float[3];
        tmp_ = new float[3];
        left = new float[3];
        left[0] = 0f;
        left[1] = 0f;
        left[2] = 1f;
        right = revVector(left);
        up = new float[3];
        up[0] = 0f;
        up[1] = 1f;
        up[2] = 0f;
        down = revVector(up);
        front = new float[3];
        front[0] = 1f;
        front[1] = 0f;
        front[2] = 0f;
        back = revVector(front);
    }
}
