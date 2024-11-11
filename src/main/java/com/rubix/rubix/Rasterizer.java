package com.rubix.rubix;

import android.graphics.Color;
import android.util.Log;

public class Rasterizer extends Thread {
    private int[]		coord; // int2
    private float[]     _a; // float2
    private float[]     _b; // float2
    private float[]     _c; // float2
    private float[]     vfa; // float2
    private float[]     vfb; // float2
    private float[]     vf_out; // float2
    private float[]     vs1; // float2
    private float[]     vs2; // float2
    private float[]     q; // float2
    private float[]     pointa; // float3
    private float       ambiant; // float3
    private float[]     origin; // float3

    // TODO --------------------
    private int[]       shared3;
    private int[]       shared4;
    private int[]       shared7;
    private int[]       shared8;
    // TODO --------------------
    private int         face;
    private int         faceOffset;
    private int         offset;
    private int[]       offa;
    private int[]       offb;
    private int[]       offc;
    private int[]       offd;
    private int[]       a;
    private int[]       b;
    private int[]       c;
    private int[]       d;
    private int[]       vertices;
    private int[]       surface;
    private int         width;
    private int         height;

    private boolean     lock;
    private boolean     running;
    private boolean     mixed;
    private int         odd;
    private int         renderType;
    private int         numCores;
    private int         clusterSize;

    private int         colorMask;
    private int[]       tileIndex;
    private float       diffuse;
    private float       specular;
    private float       marginFactor;
    private boolean     marginEffect;
    private int         margin;
    private int         id;
    private int         numInstructions_octo;
    private int         numInstructions_quad;
    private int         numInstructions_mono;
    private int         numInstructions;
    private int         maxInstructions;
    Rubixcube           rubix;

    public Rasterizer(Rubixcube rubix, int renderType, int id, int numCores)
    {
        this.id = id;
        if (id < 0 || id > 3)
            id = -1;
        this.rubix = rubix;
        this.renderType = renderType;
        this.margin = 0;
        this.marginFactor = 0.92f;
        this.offset = 0;
        this.numCores = numCores;
        clusterSize = numCores >= 8 ? 8 : (numCores >= 2 ? 4 : 1);
        maxInstructions = 3;
        numInstructions_octo = 2;
        numInstructions_quad = 3;
        numInstructions_mono = 9;
        switch (clusterSize)
        {
            case 8:
                numInstructions = numInstructions_octo - (id > 0 ? 1 : 0);
                break;
            case 4:
                numInstructions = numInstructions_quad - (id > 1 ? 1 : 0);
                break;
            default: // Mono
            {
                numInstructions = numInstructions_mono;
                maxInstructions = numInstructions;
            }
        }
        System.out.println("BATMAN :: Rasterizer :: Cluster Id ["+id+"]"); // Debug
        System.out.println("BATMAN :: Rasterizer :: Cluster Size ["+clusterSize+"]"); // Debug
        mixed = false;
        shared3 = new int[2];
        shared4 = new int[2];
        shared7 = new int[2];
        shared8 = new int[2];
        coord = new int[2];
        a = new int[2];
        b = new int[2];
        c = new int[2];
        d = new int[2];
        _a = new float[2];
        _b = new float[2];
        _c = new float[2];
        vfa = new float[2];
        vfb = new float[2];
        vf_out = new float[2];
        vs1 = new float[2];
        vs2 = new float[2];
        q = new float[2];
        pointa = new float[3];
        origin = new float[3];
        origin[0] = 0;
        origin[1] = 0;
        origin[2] = -3;
        marginEffect = true;
        initConstants();
        lock = true;
    }

    public void         switchMarginEffect()
    {
        marginEffect = !marginEffect;
    }

    public boolean      getMarginEffect()
    {
        return (marginEffect);
    }

    public void         setMarginEffect(boolean marginEffect)
    {
        this.marginEffect = marginEffect;
    }

    private void        initConstants()
    {
        offa = new int[numInstructions];
        offb = new int[numInstructions];
        offc = new int[numInstructions];
        offd = new int[numInstructions];
        face = -1;
        faceOffset = -1;
        for (int instruction = 0; instruction < numInstructions; instruction++)
        {
            offa[instruction] = -1;
            offb[instruction] = -1;
            offc[instruction] = -1;
            offd[instruction] = -1;
        }
        tileIndex = new int[maxInstructions * clusterSize];
        for (int i = 0; i < maxInstructions * clusterSize; i++)
            tileIndex[i] = -1;
        switch (clusterSize)
        {
            case 8:
                loadTriangles_octoCore();
                break;
            case 4:
                loadTriangles_quadCore();
                break;
            default:
                loadTriangles_monoCore();
        }
    }

    private void        loadTriangles_monoCore()
    {
        tileIndex[0] = 0;
        tileIndex[1] = 1;
        tileIndex[2] = 2;
        tileIndex[3] = 3;
        tileIndex[4] = 4;
        tileIndex[5] = 5;
        offa[0] = 2 * 0;
        offb[0] = 2 * 1;
        offc[0] = 2 * 4;
        offd[0] = 2 * 5;
        offa[1] = 2 * 1;
        offb[1] = 2 * 2;
        offc[1] = 2 * 5;
        offd[1] = 2 * 6;
        offa[2] = 2 * 2;
        offb[2] = 2 * 3;
        offc[2] = 2 * 6;
        offd[2] = 2 * 7;
        offa[3] = 2 * 4;
        offb[3] = 2 * 5;
        offc[3] = 2 * 8;
        offd[3] = 2 * 9;
        offa[4] = 2 * 5;
        offb[4] = 2 * 6;
        offc[4] = 2 * 9;
        offd[4] = 2 * 10;
        offa[5] = 2 * 6;
        offb[5] = 2 * 7;
        offc[5] = 2 * 10;
        offd[5] = 2 * 11;
        offa[6] = 2 * 8;
        offb[6] = 2 * 9;
        offc[6] = 2 * 12;
        offd[6] = 2 * 13;
        offa[7] = 2 * 9;
        offb[7] = 2 * 10;
        offc[7] = 2 * 13;
        offd[7] = 2 * 14;
        offa[8] = 2 * 10;
        offb[8] = 2 * 11;
        offc[8] = 2 * 14;
        offd[8] = 2 * 15;
    }

    private void        loadTriangles_octoCore()
    {
        tileIndex[0] = 0;
        tileIndex[1] = 8;
        tileIndex[2] = -1;
        tileIndex[3] = 1;
        tileIndex[4] = -1;
        tileIndex[5] = -1;
        tileIndex[6] = 2;
        tileIndex[7] = -1;
        tileIndex[8] = -1;
        tileIndex[9] = 3;
        tileIndex[10] = -1;
        tileIndex[11] = -1;
        tileIndex[12] = 4;
        tileIndex[13] = -1;
        tileIndex[14] = -1;
        tileIndex[15] = 5;
        tileIndex[16] = -1;
        tileIndex[17] = -1;
        tileIndex[18] = 6;
        tileIndex[19] = -1;
        tileIndex[20] = -1;
        tileIndex[21] = 7;
        tileIndex[22] = -1;
        tileIndex[23] = -1;
        switch (id)
        {
            case 0:
                offa[0] = 2 * 12;
                offb[0] = 2 * 0;
                offc[0] = 2 * 2;
                offd[0] = 2 * 3;
                offa[1] = 2 * 8;
                offb[1] = 2 * 9;
                offc[1] = 2 * 11;
                offd[1] = 2 * 15;
                break;
            case 1:
                offa[0] = 2 * 0;
                offb[0] = 2 * 1;
                offc[0] = 2 * 3;
                offd[0] = 2 * 4;
                break;
            case 2:
                offa[0] = 2 * 1;
                offb[0] = 2 * 13;
                offc[0] = 2 * 4;
                offd[0] = 2 * 5;
                break;
            case 3:
                offa[0] = 2 * 2;
                offb[0] = 2 * 3;
                offc[0] = 2 * 6;
                offd[0] = 2 * 7;
                break;
            case 4:
                offa[0] = 2 * 3;
                offb[0] = 2 * 4;
                offc[0] = 2 * 7;
                offd[0] = 2 * 8;
                break;
            case 5:
                offa[0] = 2 * 4;
                offb[0] = 2 * 5;
                offc[0] = 2 * 8;
                offd[0] = 2 * 9;
                break;
            case 6:
                offa[0] = 2 * 6;
                offb[0] = 2 * 7;
                offc[0] = 2 * 14;
                offd[0] = 2 * 10;
                break;
            case 7:
                offa[0] = 2 * 7;
                offb[0] = 2 * 8;
                offc[0] = 2 * 10;
                offd[0] = 2 * 11;
                break;
        }
    }

    private void        loadTriangles_quadCore() // Ok
    {
        tileIndex[0] = 1;
        tileIndex[1] = 0;
        tileIndex[2] = 5;
        tileIndex[3] = 2;

        tileIndex[4] = 4;
        tileIndex[5] = 3;
        tileIndex[6] = 7;
        tileIndex[7] = 8;

        tileIndex[8] = 6;
        tileIndex[9] = -1;
        tileIndex[10] = -1;
        tileIndex[11] = -1;
        switch (id)
        {
            case 0:
                offa[0] = 2 * 1;
                offb[0] = 2 * 2;
                offc[0] = 2 * 5;
                offd[0] = 2 * 6;
                offa[1] = 2 * 5;
                offb[1] = 2 * 6;
                offc[1] = 2 * 9;
                offd[1] = 2 * 10;
                offa[2] = 2 * 8;
                offb[2] = 2 * 9;
                offc[2] = 2 * 12;
                offd[2] = 2 * 13;
                break;
            case 1:
                offa[0] = 2 * 0;
                offb[0] = 2 * 1;
                offc[0] = 2 * 4;
                offd[0] = 2 * 5;
                offa[1] = 2 * 4;
                offb[1] = 2 * 5;
                offc[1] = 2 * 8;
                offd[1] = 2 * 9;
                break;
            case 2:
                offa[0] = 2 * 6;
                offb[0] = 2 * 7;
                offc[0] = 2 * 10;
                offd[0] = 2 * 11;
                offa[1] = 2 * 9;
                offb[1] = 2 * 10;
                offc[1] = 2 * 13;
                offd[1] = 2 * 14;
                break;
            case 3:
                offa[0] = 2 * 2;
                offb[0] = 2 * 3;
                offc[0] = 2 * 6;
                offd[0] = 2 * 7;
                offa[1] = 2 * 10;
                offb[1] = 2 * 11;
                offc[1] = 2 * 14;
                offd[1] = 2 * 15;
                break;
        }
    }

    public void         setRenderType(int type)
    {
        if (type < 0 || type > 1)
            renderType = 0;
        else
            renderType = type;
    }

    public void         render()
    {
        while (!lock);
        lock = false;
    }

    public void         setSurface(int[] surface, int width, int height)
    {
        while (!lock);
        this.surface = surface;
        this.width = width;
        this.height = height;
    }

    public void         setUnitId(int id)
    {
        while (!lock);
        if (id < 0 || id > 3)
            id = -1;
        this.id = id;
    }

    public int          getUnitId()
    {
        return (id);
    }
    public void         loadVertices(int[] vertices,
                                     int offset,
                                     float diffuse,
                                     float specular,
                                     int colorMask,
                                     boolean mixed,
                                     boolean odd,
                                     int margin) //!\ Non Thread-Safe /!\
    {
        while (!lock);
        this.vertices = vertices;
        this.offset = offset;
        face = offset / 32; // Blocksize
        faceOffset = face * 9;
        this.diffuse = diffuse;
        this.specular = Math.max(0f, specular);
        this.colorMask = colorMask;
        this.mixed = mixed;
        this.odd = odd ? (char)1 : (char)0;
        this.margin = marginEffect ? Math.max(margin, 0) : 0;
    }

    static private int  computeLight(int color, float ambiant, float diffuse, float specular)
    {
        // TODO ----------------------------------
        //int         exposition;

        //ambiant = Math.max(0f, Math.min(ambiant, 1f));
        diffuse = Math.max(0f, Math.min(diffuse, 1f));
        specular = (float)Math.max(0f, Math.min(Math.pow(Math.max(0f, specular), 2), 1f));
        //exposition = (int)(64 * specular);
        //Log.e("LIGHT", "AMBIANT:: " + ambiant); // Debug
        //Log.e("LIGHT", "DIFFUSE:: " + diffuse); // Debug
        //Log.e("LIGHT", "SPECULAR:: " + specular); // Debug
        color = Toolbox.mulColor(color, Math.min(Math.max(0f, diffuse), 1f));
        return (color); // TODO
        //return (Toolbox.addColor(color, exposition)); // TODO Specular
    }

    private void        computeUnit()
    {
        int     color;
        int     index;

        if (renderType == 0)
        {
            if (mixed)
                for (int i = 0; i < numInstructions; i++) // Anaglyph
                {
                    if (offa[i] == -1)
                        continue;
                    a[0] = vertices[offset + offa[i]];
                    a[1] = vertices[offset + offa[i] + 1];
                    b[0] = vertices[offset + offb[i]];
                    b[1] = vertices[offset + offb[i] + 1];
                    c[0] = vertices[offset + offc[i]];
                    c[1] = vertices[offset + offc[i] + 1];
                    //index = tileIndex[(i * maxInstructions) + id]; // Original
                    index = tileIndex[(i * numCores) + id]; // Testing
                    //color = Toolbox.mulColor(rubix.palette[tileIndex], diffuse); // TODO TEST MASK
                    color = computeLight(rubix.palette[rubix.status[faceOffset + index]], ambiant, diffuse, specular);
                    draw_triangle_mixed(color);
                    a[0] = vertices[offset + offd[i]];
                    a[1] = vertices[offset + offd[i] + 1];
                    draw_triangle_mixed(color);
                }
            else if (margin == 0)
                for (int i = 0; i < numInstructions; i++)
                {
                    if (offa[i] == -1)
                        continue;
                    a[0] = vertices[offset + offa[i]];
                    a[1] = vertices[offset + offa[i] + 1];
                    b[0] = vertices[offset + offb[i]];
                    b[1] = vertices[offset + offb[i] + 1];
                    c[0] = vertices[offset + offc[i]];
                    c[1] = vertices[offset + offc[i] + 1];
                    //index = tileIndex[(i * maxInstructions) + id]; // Original
                    index = tileIndex[(i * numCores) + id]; // Testing
                    color = computeLight(rubix.palette[rubix.status[faceOffset + index]], ambiant, diffuse, specular);
                    draw_triangle(color);
                    a[0] = vertices[offset + offd[i]];
                    a[1] = vertices[offset + offd[i] + 1]; // TODO Testing
                    draw_triangle(color);
                }
            else {
                for (int i = 0; i < numInstructions; i++) {
                    if (offa[i] == -1)
                        continue;
                    a[0] = vertices[offset + offa[i]];
                    a[1] = vertices[offset + offa[i] + 1];
                    b[0] = vertices[offset + offb[i]];
                    b[1] = vertices[offset + offb[i] + 1];
                    c[0] = vertices[offset + offc[i]];
                    c[1] = vertices[offset + offc[i] + 1];
                    d[0] = vertices[offset + offd[i]];
                    d[1] = vertices[offset + offd[i] + 1];

                    pointa[0] = (float)(c[0] - b[0]);
                    pointa[1] = (float)(c[1] - b[1]);
                    pointa[0] *= marginFactor;
                    pointa[1] *= marginFactor;
                    b[0] += pointa[0];
                    b[1] += pointa[1];
                    c[0] += pointa[0] * -1;
                    c[1] += pointa[1] * -1;
                    pointa[0] = (float)(d[0] - a[0]);
                    pointa[1] = (float)(d[1] - a[1]);
                    pointa[0] *= marginFactor;
                    pointa[1] *= marginFactor;
                    a[0] += pointa[0];
                    a[1] += pointa[1];
                    //index = tileIndex[(i * maxInstructions) + id]; // Original
                    index = tileIndex[(i * numCores) + id]; // Testing
                    color = computeLight(rubix.palette[rubix.status[faceOffset + index]],
                                ambiant,
                                rubix.culling[face],
                                //rubix.diffuseFaceDot[faceOffset + index], // TODO Diffuse tile
                                rubix.specularFaceDot[faceOffset + index]);
                    draw_triangle(color);
                    a[0] = (int) (d[0] + (pointa[0] * -1));
                    a[1] = (int) (d[1] + (pointa[1] * -1));
                    draw_triangle(color);
                }
            }
        }
        else // TODO Eponge de Menger
        {
            ;
        }
    }

    @Override
    public void run() {
        //try {
            ambiant = rubix.getAmbiantLight();
            if (id == -1)
                return;
            running = true;
            while (running)
                while (!lock) {
                    //Log.e("COMPUTE UNIT", "#" + id); // TODO Debug
                    computeUnit();
                    lock = true; // Finished
                }
        //} catch (Exception e) {
        //    Log.e("THREAD UNIT #" + this.getUnitId(), "Fatal error.");
        //}
    }

    public boolean  isRunning()
    {
        return (running);
    }

    public  boolean hasFinished()
    {
        return (lock);
    }

    public void     shutdown()
    {
        running = false;
    }

    private void draw_triangle_mixed(int rgb)//, float[] triangle, float[] sun_position)
    {
        int         maxX;
        int         minX;
        int         maxY;
        int         minY;
        int         odd;
        float       t;
        float       s;

        maxX = Math.max(a[0], Math.max(b[0], c[0]));
        minX = Math.min(a[0], Math.min(b[0], c[0]));
        maxY = Math.max(a[1], Math.max(b[1], c[1]));
        minY = Math.min(a[1], Math.min(b[1], c[1]));
        odd = this.odd;
        if (odd == 1 && minY % 2 == 1) {
            minY++;
            if (minY > maxY)
                maxY++;
        }
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
                vf_out[0] = vfa[0] / vfb[0];
                vf_out[1] = vfa[1] / vfb[1];
                s = vf_out[0];
                Toolbox.crossProd2(vfa, vs1, q);
                vf_out[0] = vfa[0] / vfb[0];
                vf_out[1] = vfa[1] / vfb[1];
                t = vf_out[0];
                if ( (s >= -0.000001) && (t >= -0.000001) && (s + t <= 1.000001) ) /// No gap between triangles ; Need to verify precise value
                    surface[coord[1] * width + coord[0]] = rgb;
                coord[1] += 2;
            }
            coord[0]++;
        }
    }

    private void draw_triangle(int rgb)
    {
        int maxX = Math.max(a[0], Math.max(b[0], c[0]));
        int minX = Math.min(a[0], Math.min(b[0], c[0]));
        int maxY = Math.max(a[1], Math.max(b[1], c[1]));
        int minY = Math.min(a[1], Math.min(b[1], c[1]));
        float       t;
        float       s;

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
                vf_out[0] = vfa[0] / vfb[0];
                vf_out[1] = vfa[1] / vfb[1];
                s = vf_out[0];
                Toolbox.crossProd2(vfa, vs1, q);
                vf_out[0] = vfa[0] / vfb[0];
                vf_out[1] = vfa[1] / vfb[1];
                t = vf_out[0];
                if ( (s >= -0.000001) && (t >= -0.000001) && (s + t <= 1.000001) ) /// No gap between triangles ; Need to verify precise value
                    surface[coord[1] * width + coord[0]] = rgb;
                coord[1]++;
            }
            coord[0]++;
        }
    }
}
