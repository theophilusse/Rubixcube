package com.rubix.rubix;

public class MengerSponge_middle {

    private MengerSponge_face sponge_a;
    private MengerSponge_face sponge_b;
    private boolean              axis;
    private int                  depth;

    private int                  surface[]; //
    private int                  offset[]; //

    //
    // MengerSpongeFace[] sponge = new MengerSpongeFace[3];
    // sponge[0] = new sponge()
    //

    private int     normalAxis;
    //public void     onCreate(boolean axis, int depth)
    public MengerSponge_middle(/*int normalAxis,*/ /*/boolean side, int offset[],/**/ int depth, int surface[])
    {
        //this.offset = offset;
        this.surface = surface; //
        sponge_a = null;
        sponge_b = null;
        this.depth = depth - 1;
        if (this.depth == 0)
            return ;
        this.axis = axis;
        this.normalAxis = normalAxis;
        /*
        sponge_a = new MengerSponge_face(axis, offset, depth - 1, surface);
        sponge_b = new MengerSponge_face(!axis, offset, depth - 1, surface);
        */
        /*
        //if (axis) // normal up
        if (depth % 2 == 0)
        {
            //sponge_a = new MengerSponge_face(up, side, depth);
            //sponge_b = new MengerSponge_face(axis, side, depth);
            sponge_a = new MengerSponge_face(axis, offset, depth - 1, surface);
            sponge_b = new MengerSponge_face(!axis, offset, depth - 1, surface);
        }
        else
        {
            sponge_a = new MengerSponge_face(!axis, offset, depth - 1, surface);
            sponge_b = new MengerSponge_face(axis, offset, depth - 1, surface);
            //sponge_a = new MengerSponge_face(depth % 2 == 0 ? axis : !axis, depth); // Horizontal
            //sponge_b = new MengerSponge_face(depth % 2 == 0 ? !axis : axis, depth);
        }*/
        //sponge_a = new MengerSponge_face(depth % 2 == 0 ? axis : !axis, depth); // Horizontal
        //sponge_b = new MengerSponge_face(depth % 2 == 0 ? !axis : axis, depth);
    }

    //public void     render(int basex, int basey, int width)
    public void     render(
            float[] firstCorner,
            float[] secondCorner,
            float[] thirdCorner,
            float[] fourthCorner
        )
    {
        if (depth == 0 /* || sponge_a == null */)
        {
            /** Draw crown **/
            //renderCrown(basex, basey, width);
            //renderCrown(firstCorner, secondCorner, thirdCorner, fourthCorner);
            return;
        }
        /*
        width *= 0.3;
        basex = basex + width;
        basey = basey + width;
        sponge_a.render(basex, basey, width);
        sponge_b.render(basex, basey, width);*/

        /*
        sponge_a.render(
                firstCorner,
                secondCorner,
                thirdCorner,
                fourthCorner);
        sponge_b.render(
                firstCorner,
                secondCorner,
                thirdCorner,
                fourthCorner);
         */
    }

    //private void       draw_triangle(int rgb, int a[], int b[], int c[])
    private void       draw_triangle(int rgb, float a[], float b[], float c[])
    {
        int maxX = (int) Math.max(a[0], Math.max(b[0], c[0]));
        int minX = (int) Math.min(a[0], Math.min(b[0], c[0]));
        int maxY = (int) Math.max(a[1], Math.max(b[1], c[1]));
        int minY = (int) Math.min(a[1], Math.min(b[1], c[1]));
        float       t;
        float       s;

        float _a[] = new float[2];
        float _b[] = new float[2];
        float _c[] = new float[2];
        float vs1[] = new float[2];
        float vs2[] = new float[2];
        int coord[] = new int[2];
        float q[] = new float[2];
        int vf_out[] = new int[2];
        float vfa[] = new float[2];
        float vfb[] = new float[2];
        int width = 0;
        int height = 0;
        _a[0] = (float)a[0];
        _a[1] = (float)a[1];
        _b[0] = (float)b[0];
        _b[1] = (float)b[1];
        _c[0] = (float)c[0];
        _c[1] = (float)c[1];
        vs1[0] = _b[0] - _a[0];
        vs1[1] = _b[1] - _a[1];
        vs2[0] = _c[0] - _a[0];
        vs2[1] = _c[1] - _a[1];
        coord[0] = minX;
        while (coord[0] <= maxX && coord[0] < width && coord[0] >= 0)
        {
            coord[1] = minY;
            while (coord[1] <= maxY && coord[1] < height && coord[1] >= 0)
            {
                q[0] = (float)coord[0] - _a[0];
                q[1] = (float)coord[1] - _a[1];
                Toolbox.crossProd2(vfa, q, vs2);
                Toolbox.crossProd2(vfb, vs1, vs2);
                vf_out[0] = (int) (vfa[0] / vfb[0]);
                vf_out[1] = (int) (vfa[1] / vfb[1]);
                s = vf_out[0];
                Toolbox.crossProd2(vfa, vs1, q);
                vf_out[0] = (int) (vfa[0] / vfb[0]);
                vf_out[1] = (int) (vfa[1] / vfb[1]);
                t = vf_out[0];
                if ((s >= -0.000001) && (t >= -0.000001) && (s + t <= 1.000001)) /// No gap between triangles ; Need to verify precise value
                    surface[coord[1] * width + coord[0]] = rgb;
                coord[1]++;
            }
            coord[0]++;
        }
    }

    private void    renderCrown(int firstCorner[], int secondCorner[], int thirdCorner[], int fourthCorner[]) // Aka COVID19
    {
        // On vas pas se faire chier
        // No rotation
        /*
        int innerFirstCorner[] = new int[2];
        int innerSecondCorner[] = new int[2];
        int innerThirdCorner[] = new int[2];
        int innerFourthCorner[] = new int[2];
        innerFirstCorner = (fourthCorner - firstCorner) / 3;
        innerSecondCorner = (thirdCorner - secondCorner) / 3;
        innerThirdCorner = (secondCorner - thirdCorner) / 3;
        innerFourthCorner = (fourthCorner - firstCorner) / 3;*/
        /*
        firstCorner[0] /= 2;
        firstCorner[1] /= 2;
        secondCorner[0] /= 2;
        secondCorner[1] /= 2;
        thirdCorner[0] /= 2;
        thirdCorner[1] /= 2;
        fourthCorner[0] /= 2;
        fourthCorner[1] /= 2;*/

        // Memory
        /*
        Toolbox.mulVector2di(firstCorner, 0.5f) // Swap
        Toolbox.mulVector2di(secondCorner, 0.5f)
        Toolbox.mulVector2di(thirdCorner, 0.5f)
        Toolbox.mulVector2di(fourthCorner, 0.5f)

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, firstCorner, secondCorner, Toolbox.mulVector2di(firstCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, secondCorner, Toolbox.mulVector2di(firstCorner, 0.5f), Toolbox.mulVector2di(secondCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, secondCorner, thirdCorner, Toolbox.mulVector2di(secondCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, thirdCorner, Toolbox.mulVector2di(secondCorner, 0.5f), Toolbox.mulVector2di(thirdCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, thirdCorner, fourthCorner, Toolbox.mulVector2di(thirdCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, fourthCorner, Toolbox.mulVector2di(secondCorner, 0.5f), Toolbox.mulVector2di(fourthCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, fourthCorner, firstCorner, Toolbox.mulVector2di(fourthCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00, firstCorner, Toolbox.mulVector2di(fourthCorner, 0.5f), Toolbox.mulVector2di(firstCorner, 0.5f));
        */

        // No memory
        /*
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                firstCorner, secondCorner, Toolbox.mulVector2di(firstCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                secondCorner, Toolbox.mulVector2di(firstCorner, 0.5f), Toolbox.mulVector2di(secondCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                secondCorner, thirdCorner, Toolbox.mulVector2di(secondCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                thirdCorner, Toolbox.mulVector2di(secondCorner, 0.5f), Toolbox.mulVector2di(thirdCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                thirdCorner, fourthCorner, Toolbox.mulVector2di(thirdCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                fourthCorner, Toolbox.mulVector2di(thirdCorner, 0.5f), Toolbox.mulVector2di(fourthCorner, 0.5f));

        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                fourthCorner, firstCorner, Toolbox.mulVector2di(fourthCorner, 0.5f));
        draw_triangle(depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                firstCorner, Toolbox.mulVector2di(fourthCorner, 0.5f), Toolbox.mulVector2di(firstCorner, 0.5f));
        */
    }

    /*
    private void    renderCrown(InterplanetaryCompanion comp, int base_x, int base_y, int width) // Aka COVID19
    {
        int     corner_ax;
        int     corner_ay;
        int     corner_bx;
        int     corner_by;
        int     corner_cx;
        int     corner_cy;
        int     corner_dx;
        int     corner_dy;

        //width *= sqrt(0.3);
        rightDirection = comp.getRight();
        downDirection = comp.getDown();

        corner_ax = base_x + width;
        corner_ay = base_y + width;

        corner_bx = base_y;
        corner_by = base_y;
    }*/
}