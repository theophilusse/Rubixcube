package com.rubix.rubix;

public class MengerSponge_face {

    //MengerSponge_middle face;
    MengerSponge_face sponge_a;
    MengerSponge_face sponge_b;
    MengerSponge_face   tile[];
    private int         depth;
    /*private int         dim[];*/
    private int         surface[];

    /*
    private float       corner_a[];
    private float       corner_b[];
    private float       corner_c[];
    private float       corner_d[];

    private float       axisx[];
    private float       axisy[];
    private float       axisz[];

    private int         base[]; ///
    */

    //public MengerSponge(int base[], int offset[], int depth, int surface[], int dim[])

    public MengerSponge_face(
            /*int normalAxis,*/
            /*boolean axis,*/
            /*int base[],*/
            /*int offset[],*/
            int depth,
            int surface[],
            int dim[] // screen dim
        )
    {
        this.depth = depth;
        /*this.dim = dim;*/
        this.surface = surface;
        int nextOffset[];

        nextOffset = new int[3];
        //nextOffset[0] = (int) ((base[0] - offset[0]) * 0.3f);
        sponge_a = null;
        sponge_b = null;
        tile = new MengerSponge_face[8];
        for (int i = 0; i < 9; i++)
            tile[i] = null;
        if (depth >= 0)
            for (int j = 0; j < 9; j++)
            {
                if (j == 4)
                {
                    //face = new MengerSponge_middle(normalAxis, axis, depth - 1, surface);
                    sponge_a = new MengerSponge_face(depth - 1, surface, dim);
                    sponge_b = new MengerSponge_face(depth - 1, surface, dim);
                }
                else
                    tile[j] = new MengerSponge_face(depth - 1, surface, dim); //
                    //tile[j] = new MengerSponge_face(normalAxis, !axis, depth - 1, surface/*, dim*/); //
            }

        //Math.sqrt((height * 0.75));
        /*
        if (dim != null)
        {
            tile = new MengerSponge[3];
            tile[0] = new MengerSponge(axis, offset, depth, surface, null);
            tile[1] = new MengerSponge(!axis, offset, depth, surface, null);
            tile[2] = new MengerSponge(axis, offset, depth, surface, null);
            tile[4] = null;
        }
        else
        {
            for (int j = 0; j < 9; j++)
            {
                tile[0] = new MengerSponge(axis, offset, depth, surface);
                if (j == 4)
                    face = new MengerSponge_face(axis, offset, depth, surface);
            }
        }*/
    }

    private void       draw_triangle(int rgb, int a[], int b[], int c[], int surface[])
    {
        int maxX = Math.max(a[0], Math.max(b[0], c[0]));
        int minX = Math.min(a[0], Math.min(b[0], c[0]));
        int maxY = Math.max(a[1], Math.max(b[1], c[1]));
        int minY = Math.min(a[1], Math.min(b[1], c[1]));
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

    private void render_sponge( // Can be optimized
            float upLeftCorner[],
            float upRightCorner[],
            float downLeftCorner[],
            float downRightCorner[],
            AxisVectors axis,
            int width,
            int side
        )
    {
        float face_a_upLeftCorner[] = new float[2];
        float face_a_upRightCorner[] = new float[2];
        float face_a_downLeftCorner[] = new float[2];
        float face_a_downRightCorner[] = new float[2];
        float face_b_upLeftCorner[] = new float[2];
        float face_b_upRightCorner[] = new float[2];
        float face_b_downLeftCorner[] = new float[2];
        float face_b_downRightCorner[] = new float[2];

        /*
        float vecLeft[] = new float[2];
        float vecRight[] = new float[2];
        */
        float vec[] = new float[2];
        int side_a;
        int side_b;
        if (side == 0/* < 2 */)
        {
            side_a = 1;
            side_b = 2;
            vec[0] = axis.left[0] * width; // vecLeft
            vec[1] = axis.left[1] * width; // vecLeft
            //
            face_a_upRightCorner[0] = upRightCorner[0];
            face_a_upRightCorner[1] = upRightCorner[1];
            face_a_downRightCorner[0] = downRightCorner[0];
            face_a_downRightCorner[1] = downRightCorner[1];
            face_a_upLeftCorner[0] = upRightCorner[0] + vec[0]; // vecLeft
            face_a_upLeftCorner[1] = upRightCorner[1] + vec[1]; // vecLeft
            face_a_downLeftCorner[0] = downRightCorner[0] + vec[0]; // vecLeft
            face_a_downLeftCorner[1] = downRightCorner[1] + vec[1]; // vecLeft

            face_b_upLeftCorner[0] = face_a_downLeftCorner[0];
            face_b_upLeftCorner[1] = face_a_downLeftCorner[1];
            face_b_downLeftCorner[0] = downLeftCorner[0] + vec[0]; // vecLeft
            face_b_downLeftCorner[1] = downLeftCorner[1] + vec[1]; // vecLeft
            face_b_upRightCorner[0] = downRightCorner[0];
            face_b_upRightCorner[1] = downRightCorner[1];
            face_b_downRightCorner[0] = downLeftCorner[0];
            face_b_downRightCorner[1] = downLeftCorner[1];
        }
        else if (side == 1) //
        {
            side_a = 0;
            side_b = 2;
            vec[0] = axis.right[0] * width; // vecRight
            vec[1] = axis.right[1] * width; // vecRight
            //
            face_a_upLeftCorner[0] = upLeftCorner[0];
            face_a_upLeftCorner[1] = upLeftCorner[1];
            face_a_downLeftCorner[0] = downLeftCorner[0];
            face_a_downLeftCorner[1] = downLeftCorner[1];
            face_a_upRightCorner[0] = upLeftCorner[0] + vec[0]; // vecRight
            face_a_upRightCorner[1] = upLeftCorner[1] + vec[1]; // vecRight
            face_a_downRightCorner[0] = downLeftCorner[0] + vec[0]; // vecRight
            face_a_downRightCorner[1] = downLeftCorner[1] + vec[1]; // vecRight

            face_b_upLeftCorner[0] = face_a_downLeftCorner[0];
            face_b_upLeftCorner[1] = face_a_downLeftCorner[1];
            face_b_downLeftCorner[0] = downLeftCorner[0];
            face_b_downLeftCorner[1] = downLeftCorner[1];
            face_b_upRightCorner[0] = downRightCorner[0] + vec[0]; // vecRight
            face_b_upRightCorner[1] = downRightCorner[1] + vec[1]; // vecRight
            face_b_downRightCorner[0] = downRightCorner[0];
            face_b_downRightCorner[1] = downRightCorner[1];
        }
        else // if (side == 2)
        {
            side_a = 0;
            side_b = 1;
            //
            if (axis.front[1] <= 0f) // upFace
            {
                vec[0] = axis.down[0] * width; // vecDown
                vec[1] = axis.down[1] * width; // vecDown
                //
                face_a_upLeftCorner[0] = downRightCorner[0];
                face_a_upLeftCorner[1] = downRightCorner[1];
                face_a_downLeftCorner[0] = downLeftCorner[0] + vec[0]; // vecDown
                face_a_downLeftCorner[1] = downLeftCorner[1] + vec[1]; // vecDown
                face_a_upRightCorner[0] = upLeftCorner[0];
                face_a_upRightCorner[1] = upLeftCorner[1];
                face_a_downRightCorner[0] = upLeftCorner[0] + vec[0]; // vecDown
                face_a_downRightCorner[1] = upLeftCorner[1] + vec[1]; // vecDown

                face_b_upLeftCorner[0] = upLeftCorner[0];
                face_b_upLeftCorner[1] = upLeftCorner[1];
                face_b_downLeftCorner[0] = upLeftCorner[0] + vec[0]; // vecDown
                face_b_downLeftCorner[1] = upLeftCorner[1] + vec[1]; // vecDown
                face_b_upRightCorner[0] = upRightCorner[0];
                face_b_upRightCorner[1] = upRightCorner[1];
                face_b_downRightCorner[0] = upRightCorner[0] + vec[0]; // vecDown
                face_b_downRightCorner[1] = upRightCorner[1] + vec[1]; // vecDown
            }
            else // downFace
            {
                vec[0] = axis.up[0] * width; // vecUp
                vec[1] = axis.up[1] * width; // vecUp
                //
                face_a_downLeftCorner[0] = downLeftCorner[0];
                face_a_downLeftCorner[1] = downLeftCorner[1];
                face_a_upLeftCorner[0] = downLeftCorner[0] + vec[0]; // vecUp
                face_a_upLeftCorner[1] = downLeftCorner[1] + vec[1]; // vecUp
                face_a_downRightCorner[0] = downRightCorner[0];
                face_a_downRightCorner[1] = downRightCorner[1];
                face_a_upRightCorner[0] = downRightCorner[0] + vec[0]; // vecUp
                face_a_upRightCorner[1] = downRightCorner[1] + vec[1]; // vecUp

                face_b_downLeftCorner[0] = downRightCorner[0];
                face_b_downLeftCorner[1] = downRightCorner[1];
                face_b_upLeftCorner[0] = downRightCorner[0] + vec[0]; // vecUp
                face_b_upLeftCorner[1] = downRightCorner[1] + vec[1]; // vecUp
                face_b_downRightCorner[0] = upLeftCorner[0];
                face_b_downRightCorner[1] = upLeftCorner[1];
                face_b_upRightCorner[0] = upRightCorner[0] + vec[0]; // vecUp
                face_b_upRightCorner[1] = upRightCorner[1] + vec[1]; // vecUp
            }
        }
        sponge_a.render(face_a_upLeftCorner,
                face_a_upRightCorner,
                face_a_downLeftCorner,
                face_a_downRightCorner,
                axis,
                width,
                side_a);
        sponge_b.render(face_b_upLeftCorner,
                face_b_upRightCorner,
                face_b_downLeftCorner,
                face_b_downRightCorner,
                axis,
                width,
                side_b);
    }

    public void render(
            float upLeftCorner[],
            float upRightCorner[],
            float downLeftCorner[],
            float downRightCorner[],
            AxisVectors axis,
            int width,
            int side)
    {
        /*
        float upLeftCorner[];
        float upRightCorner[];
        float downLeftCorner[];
        float downRightCorner[];
        */

        if (/*depth == 9 ||*/ depth == 0) {
            int tri_a[] = new int[2];
            int tri_b[] = new int[2];
            int tri_c[] = new int[2];
            int tri_d[] = new int[2];
            tri_a[0] = (int) upLeftCorner[0];
            tri_a[1] = (int) upLeftCorner[1];
            tri_b[0] = (int) upRightCorner[0];
            tri_b[1] = (int) upRightCorner[1];
            tri_c[0] = (int) downLeftCorner[0];
            tri_c[1] = (int) downLeftCorner[1];
            tri_d[0] = (int) downRightCorner[0];
            tri_d[1] = (int) downRightCorner[1];
            int color;
            color = (0xff000000 >> side) / (side + 1);
            draw_triangle(
                    color, //depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                    tri_a,
                    tri_b,
                    tri_d,
                    surface);
            draw_triangle(
                    color, //depth % 2 == 0 ? 0x00ff0000 : 0x0000ff00,
                    tri_a,
                    tri_c,
                    tri_d,
                    surface);
        }

        float offsetx = 0;
        float offsety = 0;
        float center[];

        center = new float[2];
        /*offsetx = ;
        offsety = ;*/
        center[0] = (downRightCorner[0] - upLeftCorner[0]) * 0.5f;
        center[1] = (downRightCorner[1] - upLeftCorner[1]) * 0.5f;
        /*
        if (depth == 9)
        {
            offsetx = dim[0] * 0.5f;
            offsety = dim[1] * 0.5f;
        }
        */
        if (side == 0)
        {
            center[0] = ((float) offsetx) + (axis.left[0] * width) * -1;
            center[1] = ((float) offsety) + (axis.left[1] * width);
        }
        else if (side == 1)
        {
            center[0] = ((float) offsetx) + (axis.left[0] * width);
            center[1] = ((float) offsety) + (axis.left[1] * width);
        }
        else if (side == 2)
        {
            // TODO ------------------------------------------------------

            //if (axis.up[1] > axis.down[1])
            if (axis.front[1] > 0f)
            {
                center[0] = ((float) offsetx) + (axis.up[0] * width);
                center[1] = ((float) offsety) + (axis.up[1] * width);
            }
            else
            {
                center[0] = ((float) offsetx) + (axis.down[0] * width);
                center[1] = ((float) offsety) + (axis.down[1] * width);
            }
        }
        //

        center = new float[2];
        center[0] = upLeftCorner[0] + ((downRightCorner[0] - upLeftCorner[0]) / 2);
        center[1] = upLeftCorner[1] + ((downRightCorner[1] - upLeftCorner[1]) / 2);
        //center[2] = upLeftCorner[2] + ((downRightCorner[2] - upLeftCorner[2]) / 2);

        // Lazy method
        /*
            a_ b  c  d_
            e  f  g  h
            i  j  k  l
            m_ b  c  p_
        */
        float vec_x[] = new float[3];
        float vec_y[] = new float[3];

        vec_x[0] = (upRightCorner[0] - upLeftCorner[0]) / 3;
        vec_x[1] = (upRightCorner[1] - upLeftCorner[1]) / 3;
        vec_x[2] = (upRightCorner[2] - upLeftCorner[2]) / 3;
        vec_y[0] = (downLeftCorner[0] - upLeftCorner[0]) / 3;
        vec_y[1] = (downLeftCorner[1] - upLeftCorner[1]) / 3;
        vec_y[2] = (downLeftCorner[2] - upLeftCorner[2]) / 3;

        //float grid_pt_a[] = new float grid_pt_a[];
        float grid_pt_b[] = new float[2];
        float grid_pt_c[] = new float[2];
        //float grid_pt_d[] = new float grid_pt_d[];
        float grid_pt_e[] = new float[2];
        float grid_pt_f[] = new float[2];
        float grid_pt_g[] = new float[2];
        float grid_pt_h[] = new float[2];
        float grid_pt_i[] = new float[2];
        float grid_pt_j[] = new float[2];
        float grid_pt_k[] = new float[2];
        float grid_pt_l[] = new float[2];
        //float grid_pt_m[] = new float grid_pt_m[];
        float grid_pt_n[] = new float[2];
        float grid_pt_o[] = new float[2];
        //float grid_pt_p[] = new float grid_pt_p[];

        /*
        face.render(
                grid_pt_f,
                grid_pt_g,
                grid_pt_j,
                grid_pt_k
                upLeftCorner + ((downRightCorner - upLeftCorner) / 2),
                upRightCorner + ((downLeftCorner - upRightCorner) / 2),

                downLeftCorner + ((downRightCorner - upLeftCorner) / 2),
                downRightCorner + ((downLeftCorner - upRightCorner) / 2),
                downLeftCorner,
                downRightCorner
        );
        */

        // Other method
        /*
        a = (ulc - center) * 0.7;
        b = (urc - center) * 0.7;
        c = (dlc - center) * 0.7;
        d = (drc - center) * 0.7;

        aa = (b - a) * 0.5
        bb = (c - b) * 0.5
        cc = (c - d) * 0.5
        dd = (d - a) * 0.5
        */

        Toolbox.addVector2dCopy(grid_pt_b, upLeftCorner, vec_x);

        Toolbox.addVector2dCopy(grid_pt_c, vec_y, vec_x);
        Toolbox.addVector2dCopy(grid_pt_c, upRightCorner, grid_pt_c);
        //Toolbox.addVector2dCopy(grid_pt_d, upLeftCorner, vec_x);

        // upLeftCorner + vec_x + vec_y
        Toolbox.addVector2dCopy(grid_pt_e, vec_x, vec_y);
        Toolbox.addVector2dCopy(grid_pt_e, upLeftCorner, grid_pt_e);

        // upLeftCorner + vec_y
        Toolbox.addVector2dCopy(grid_pt_f, upLeftCorner, vec_y);

        // upLeftCorner + vec_x + vec_y
        Toolbox.addVector2dCopy(grid_pt_g, vec_y, vec_x);
        Toolbox.addVector2dCopy(grid_pt_g, upLeftCorner, grid_pt_g);

        // upRightCorner + vec_y
        Toolbox.addVector2dCopy(grid_pt_h, upRightCorner, vec_y);

        // downLeftCorner - vec_y + vec_x
        Toolbox.subVector2dCopy(grid_pt_i, downLeftCorner, vec_y);
        Toolbox.addVector2dCopy(grid_pt_i, grid_pt_i, vec_x);

        // downLeftCorner + vec_x - vec_y
        Toolbox.addVector2dCopy(grid_pt_j, downLeftCorner, vec_x);
        Toolbox.subVector2dCopy(grid_pt_j, grid_pt_j, vec_y);

        // downRightCorner - vec_y - vec_x
        Toolbox.subVector2dCopy(grid_pt_k, downRightCorner, vec_y);
        Toolbox.subVector2dCopy(grid_pt_k, grid_pt_k, vec_x);

        // downRightCorner - vec_y
        Toolbox.subVector2dCopy(grid_pt_l, downRightCorner, vec_y);

        //Toolbox.addVector2dCopy(grid_pt_m, upLeftCorner, vec_x);
        // downRightCorner - vec_x
        Toolbox.addVector2dCopy(grid_pt_n, downLeftCorner, vec_x);

        //
        Toolbox.subVector2dCopy(grid_pt_o, downRightCorner, vec_x);

        render_sponge(
            grid_pt_f,
            grid_pt_g,
            grid_pt_j,
            grid_pt_k,
            axis,
            (int)(width * 0.5f),
            side
        );

        tile[0].render(
            upLeftCorner, // a
            grid_pt_b, // b
            grid_pt_e, // e
            grid_pt_f, // f
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[1].render(
            grid_pt_b, // b
            grid_pt_c, // c
            grid_pt_f, // f
            grid_pt_g, // g
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[2].render(
            grid_pt_c, // c
            upRightCorner, // d
            grid_pt_g, // g
            grid_pt_h, // h
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[3].render(
            grid_pt_e, // e
            grid_pt_f, // f
            grid_pt_i, // i
            grid_pt_j, // j
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[4].render(
            grid_pt_f, // f
            grid_pt_g, // g
            grid_pt_j, // j
            grid_pt_k, // k
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[5].render(
            grid_pt_g, // g
            grid_pt_h, // h
            grid_pt_k, // k
            grid_pt_f, // f
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[6].render(
            grid_pt_i, // i
            grid_pt_j, // j
            //downRightCorner - (vec_x + vec_y),
            downLeftCorner, // m
            grid_pt_n, // n
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[7].render(
            grid_pt_j, // j
            grid_pt_k, // k
            grid_pt_n, // n
            grid_pt_o, // o
            axis,
            (int)(width * 0.5f),
            side
        );
        tile[8].render(
            grid_pt_k, // k
            grid_pt_l, // l
            grid_pt_o, // o
            downRightCorner, // p
            axis,
            (int)(width * 0.5f),
            side
        );
    }
}