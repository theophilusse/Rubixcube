package com.rubix.rubix;

import static com.rubix.rubix.Toolbox.*;

import android.graphics.Color;
import android.util.Log;

import com.rubix.rubix.Voxel.Cube;

public class Rubixcube {
    public int[]        palette;
    public int          delimiterColor;
    private float[]     vertice;
    private float[]     verticeOutput;
    private float[]     tilePosition;

    private float[]     tmp;
    private float[]     tmp_;
    private float[]     tmp__;
    private float[]     tmp___;
    private float[]     tmp____;
    private float[]     sunDirection;
    private float[]     sunDirection_;
    public float[]      sunPosition;
    private float[]     sunPosition_global;
    private float[]     cameraDirection;
    private float[]     cameraPosition;
    private boolean     cullingMode;
    public float[]      culling;
    public float[]      specularFaceDot;
    public float[]      diffuseFaceDot;
    public float        ambiantLight;
    public int[]        status;

    public int          grabbed;
    public int[]        grab;
    public int[]        cube;
    public int          easterEgg;
    public int          easterValue;
    public Tileset16[]  tile;
    private Cube[]      voxel;
    private Cube[] voxelRingBuffer;

    private final int         totalFace = 6;
    private final int         numTile = 9;
    private final int         numVerticeFace = 16;
    private final int         totalTile = totalFace * numTile;
    private final int         numVerticeFaceLength = numVerticeFace * 3;


    public Rubixcube(Toolbox tool, int[] palette, float ambiantLight, boolean cullingMode)
    {
        int bufSize;
        int dstPtr;
        int srcPtr;

        /* Working buffers */
        this.cullingMode = cullingMode;
        delimiterColor = 0xFF888888;
        status = new int[totalTile];
        tmp = new float[3];
        tmp_ = new float[3];
        tmp__ = new float[3];
        tmp___ = new float[3];
        tmp____ = new float[3];
        easterEgg = 0;
        easterValue = 10 + (int)(Math.random() * 10);

        /* Debug view */
        tile = new Tileset16[totalFace];
        for (int i = 0; i < tile.length; i++)
            tile[i] = new Tileset16(complementaryColor(i), palette[i]);

        /* Lighting + Culling View */
        setAmbiantLight(ambiantLight);
        initPalette(palette);
        culling = new float[totalFace + 1];
        specularFaceDot = new float[totalTile];
        for (int i = 0; i < specularFaceDot.length; i++)
            specularFaceDot[i] = 0.5f;
        diffuseFaceDot = new float[totalTile];
        for (int i = 0; i < diffuseFaceDot.length; i++)
            diffuseFaceDot[i] = 0.5f;
        cameraPosition = new float[3];
        cameraPosition[0] = 0f;
        cameraPosition[1] = 0f;
        cameraPosition[2] = 3f;
        cameraDirection = new float[3];
        cameraDirection[0] = 0f;
        cameraDirection[1] = 0f;
        cameraDirection[2] = 1f;
        float span = 10f;
        float span2 = span * -0.5f;
        sunPosition = new float[3];
        sunDirection = new float[3];
        sunDirection_ = new float[3];
        sunPosition_global = new float[3];
        sunPosition_global[0] = 0f;
        sunPosition_global[1] = -2.5f; // Up
        sunPosition_global[2] = 0f;
        while (Toolbox.magVector(sunPosition_global) <= 15f)
        {
            sunPosition_global[0] = (span2 + (int) (Math.random() * span)) * 4;
            sunPosition_global[1] = (span2 + (int) (Math.random() * span)) * 4;
            sunPosition_global[2] = (span2 + (int) (Math.random() * span)) * 4;
        }
        normalize(sunDirection, sunPosition);
        revVectorCopy(sunDirection_, sunDirection);

        /* Vertices */
        Face face;
        bufSize = numVerticeFace * totalFace * 3;
        vertice = new float[bufSize];
        verticeOutput = new float[bufSize];
        dstPtr = 0;
        for (int fIndex = 0; fIndex < totalFace; fIndex++)
        {
            srcPtr = 0;
            face = new Face(tmp, tmp_, tool, fIndex);
            for (int j = 0; j < face.point.length; j++) {
                vertice[dstPtr] = face.point[srcPtr];
                dstPtr++;
                srcPtr++;
            }
        }
        tilePosition = new float[totalTile * 3];

        /* Initialize */
        grabbed = 0;
        initBlocks();
        while (isSolved()) {
            rotate((int) (Math.random() * 6), (int) (Math.random() * 3)); // Enlever
            rotate((int) (Math.random() * 6), (int) (Math.random() * 3)); // Enlever
        }
        //rotate((int)(Math.random() * 6), (int)(Math.random() * 3)); // Enlever
        //updateStatus();
        // TODO initFaces();
        // TODO shuffle(25 + (int)(Math.random() * 64)); // TODO REMETTRE
    }

    private void            initPalette(int[] palette)
    {
        this.palette = palette;
        if (1 == 1)
            return ;
        this.palette = new int[6];

        for (int face = 6; face < 6; face++)
            this.palette[face] = palette[face];
    }

    public boolean          easterEgg()
    {
        if (easterEgg++ < easterValue)
            return (false);
        resetBlocks();
        easterEgg = 0;
        // TODO Debug : Finir les rotations
        for (int j = 0; j < 6; j++)
            for (int i = 0; i < 9; i++)
                debugTile(i, -1);
        return (true);
    }

    public void             setAmbiantLight(float factor)
    {
        ambiantLight = Math.max(0.05f, Math.min(factor, 1f));
    }

    public float            getAmbiantLight()
    {
        return (ambiantLight);
    }

    public void             setDelimiterColor(int color)
    {
        delimiterColor = color;
    }

    public void                debugTile(int tile, int index)
    {
        tile %= 9;
        index %= 6;
        for (int i = 0; i < 6; i++)
            status[i * 9 + tile] = index < 0 ? i : index;
    }

    public void                debugTop(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 3; j++)
                status[i * 9 + j] = index < 0 ? i : index;
    }

    public void                debugBottom(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
            for (int j = 5; j < 9; j++)
                status[i * 9 + j] = index < 0 ? i : index;
    }

    public void                debugLeft(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 9; j += 3)
                status[i * 9 + j] = index < 0 ? i : index;
    }

    public void                debugRight(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
            for (int j = 2; j < 9; j += 3)
                status[i * 9 + j] = index < 0 ? i : index;
    }

    public void                debugVertical(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
        {
            status[i * 9 + 1] = index < 0 ? i : index;
            status[i * 9 + 4] = index < 0 ? i : index;
            status[i * 9 + 7] = index < 0 ? i : index;
        }
    }

    public void                debugHorizontal(int index)
    {
        index %= 6;
        for (int i = 0; i < 6; i++)
        {
            status[i * 9 + 3] = index < 0 ? i : index;
            status[i * 9 + 4] = index < 0 ? i : index;
            status[i * 9 + 5] = index < 0 ? i : index;
        }
    }

    public void                initFaces()
    {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 9; j++)
                status[i * 9 + j] = i;
    }

    public void                shuffle(int rot)
    {
        for (int i = 0; i < rot; i++)
            rotate((int)(Math.random() * 6), (int)(Math.random() * 3));
    }

    private Cube              cubeType(int type)
    {
        int a;
        int b;
        int c;

        a = -1;
        b = -1;
        c = -1;
        switch (type)
        {
            case 0:
                a = 1;
                b = 2;
                c = 4;
                break;
            case 1:
                a = 1;
                b = 2;
                c = -1;
                break;
            case 2:
                a = 1;
                b = 5;
                c = 2;
                break;
            case 3:
                a = 1;
                b = 4;
                c = -1;
                break;
            case 4:
                a = 1;
                b = -1;
                c = -1;
                break;
            case 5:
                a = 1;
                b = 5;
                c = -1;
                break;
            case 6:
                a = 1;
                b = 4;
                c = 0;
                break;
            case 7:
                a = 1;
                b = 0;
                c = -1;
                break;
            case 8:
                a = 1;
                b = 0;
                c = 5;
                break;
            case 9:
                a = 2;
                b = 4;
                c = -1;
                break;
            case 10:
                a = 2;
                b = -1;
                c = -1;
                break;
            case 11:
                a = 5;
                b = 2;
                c = -1;
                break;
            case 12:
                a = 4;
                b = -1;
                c = -1;
                break;
            case 13:
                a = -1;
                b = -1;
                c = -1;
                break;
            case 14:
                a = 5;
                b = -1;
                c = -1;
                break;
            case 15:
                a = 4;
                b = 0;
                c = -1;
                break;
            case 16:
                a = 0;
                b = -1;
                c = -1;
                break;
            case 17:
                a = 0;
                b = 5;
                c = -1;
                break;
            case 18:
                a = 3;
                b = 4;
                c = 2;
                break;
            case 19:
                a = 3;
                b = 2;
                c = -1;
                break;
            case 20:
                a = 3;
                b = 2;
                c = 5;
                break;
            case 21:
                a = 3;
                b = 4;
                c = -1;
                break;
            case 22:
                a = 3;
                b = -1;
                c = -1;
                break;
            case 23:
                a = 3;
                b = 5;
                c = -1;
                break;
            case 24:
                a = 3;
                b = 0;
                c = 4;
                break;
            case 25:
                a = 3;
                b = 0;
                c = -1;
                break;
            case 26:
                a = 3;
                b = 5;
                c = 0;
                break;
        }
        return (new Cube(a, b, c));
    }

    private void              initBlocks()
    {
        voxel = new Cube[27];
        voxelRingBuffer = new Cube[9];
        cube = new int[20 * 3];
        resetBlocks();
    }

    public boolean          isSolved()
    {
        for (int face = 0; face < 6; face++)
            for (int tile = 0; tile < 9; tile++)
                if (status[face * 9 + tile] != face)
                    return (false);
        return (true);
    }

    public void             resetBlocks()
    {
        for (int c = 0; c < 27; c++)
            voxel[c] = cubeType(c);
        for (int c = 0; c < 8; c++)
            voxelRingBuffer[c] = cubeType(0);

        // TODO Crown subset
        // face #0
        grab = new int[6 * 8];
        grab[0] = 6;
        grab[1] = 7;
        grab[2] = 8;
        grab[3] = 17;
        grab[4] = 26;
        grab[5] = 25;
        grab[6] = 24;
        grab[7] = 15;
        /** Ok **/

        // face #1
        grab[8] = 0;
        grab[9] = 1;
        grab[10] = 2;
        grab[11] = 5;
        grab[12] = 8;
        grab[13] = 7;
        grab[14] = 6;
        grab[15] = 3;
        /** Ok **/

        // face #2
        grab[16] = 2;
        grab[17] = 1;
        grab[18] = 0;
        grab[19] = 9;
        grab[20] = 18;
        grab[21] = 19;
        grab[22] = 20;
        grab[23] = 11;
        /** Ok **/

        // face #3
        grab[24] = 20;
        grab[25] = 19;
        grab[26] = 18;
        grab[27] = 21;
        grab[28] = 24;
        grab[29] = 25;
        grab[30] = 26;
        grab[31] = 23;
        /** Ok **/

        // face #4
        grab[32] = 0;
        grab[33] = 3;
        grab[34] = 6;
        grab[35] = 15;
        grab[36] = 24;
        grab[37] = 21;
        grab[38] = 18;
        grab[39] = 9;
        /** Ok **/

        // face #5
        grab[40] = 8;
        grab[41] = 5;
        grab[42] = 2;
        grab[43] = 11;
        grab[44] = 20;
        grab[45] = 23;
        grab[46] = 26;
        grab[47] = 17;
        /** Ok **/

        updateStatus();

        // TODO Table rotation: referencing faces on cube
        // Joins
        /*
        cube[0] = 1;
        cube[1] = 2;
        cube[2] = -1;

        cube[3] = 1;
        cube[4] = 5;
        cube[5] = -1;
        cube[6] = 1;
        cube[7] = 0;
        cube[8] = -1;
        cube[9] = 1;
        cube[10] = 4;
        cube[11] = -1;
        cube[12] = 2;
        cube[13] = 4;
        cube[14] = -1;
        cube[15] = 5;
        cube[16] = 2;
        cube[17] = -1;
        cube[18] = 4;
        cube[19] = 0;
        cube[20] = -1;
        cube[21] = 0;
        cube[22] = 5;
        cube[23] = -1;
        cube[24] = 3;
        cube[25] = 2;
        cube[26] = -1;
        cube[27] = 3;
        cube[28] = 5;
        cube[29] = -1;
        cube[30] = 3;
        cube[31] = 0;
        cube[32] = -1;
        cube[33] = 3;
        cube[34] = 4;
        cube[35] = -1;

        // Corners
        cube[36] = 1;
        cube[37] = 2;
        cube[38] = 4;
        cube[39] = 1;
        cube[40] = 5;
        cube[41] = 2;
        cube[42] = 1;
        cube[43] = 0;
        cube[44] = 5;
        cube[45] = 1;
        cube[46] = 4;
        cube[47] = 0;
        cube[48] = 3;
        cube[49] = 4;
        cube[50] = 2;
        cube[51] = 3;
        cube[52] = 2;
        cube[53] = 5;
        cube[54] = 3;
        cube[55] = 5;
        cube[56] = 0;
        cube[57] = 3;
        cube[58] = 0;
        cube[59] = 4;
        */

        /*
        // face #0
        grab[0] = 12;
        grab[1] = 0;
        grab[2] = 13;
        grab[3] = 3;
        grab[4] = 1;
        grab[5] = 15;
        grab[6] = 2;
        grab[7] = 14;

        // face #1
        grab[8] = 16;
        grab[9] = 8;
        grab[10] = 17;
        grab[11] = 11;
        grab[12] = 9;
        grab[13] = 19;
        grab[14] = 10;
        grab[15] = 18;

        // face #2
        grab[16] = 16;
        grab[17] = 4;
        grab[18] = 12;
        grab[19] = 11;
        grab[20] = 3;
        grab[21] = 19;
        grab[22] = 6;
        grab[23] = 15;

        // face #3
        grab[24] = 13;
        grab[25] = 5;
        grab[26] = 17;
        grab[27] = 1;
        grab[28] = 9;
        grab[29] = 14;
        grab[30] = 7;
        grab[31] = 18;

        // face #4
        grab[32] = 15;
        grab[33] = 2;
        grab[34] = 14;
        grab[35] = 6;
        grab[36] = 7;
        grab[37] = 19;
        grab[38] = 10;
        grab[39] = 18;

        // face #5
        grab[40] = 16;
        grab[41] = 8;
        grab[42] = 17;
        grab[43] = 4;
        grab[44] = 5;
        grab[45] = 12;
        grab[46] = 0;
        grab[47] = 13;
        */
    }

    private void            copyCube(int dstCube, int srcCube)
    {
        dstCube *= 3;
        srcCube *= 3;
        for (int i = 0; i < 3; i++)
        {
            cube[dstCube] = cube[srcCube];
            dstCube++;
            srcCube++;
        }
    }

    private void debugIterFaceColor(int faceIndex)
    {
        for (int t = 0; t < 9; t++)
            setStatus(faceIndex, t, (getStatus(faceIndex, t) + 1) % 6);
    }

    private void debugSetFaceColor(int faceIndex, int colorIndex)
    {
        for (int t = 0; t < 9; t++)
            setStatus(faceIndex, t, colorIndex);//voxel[6].colorIndex[2]);
    }

    private void debugSetFaceTileIndexColor(int faceIndex, int tileIndex, int colorIndex)
    {
        setStatus(faceIndex, tileIndex, colorIndex);//voxel[6].colorIndex[2]);
    }

    public void rotate_face0(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 2;
        offset[1] = 1;
        offset[2] = 0;

        offset[3] = 5;
        offset[4] = 4;
        offset[5] = 3;

        offset[6] = 8;
        offset[7] = 7;
        offset[8] = 6;

        // ClockWise
        // Up
        offset[9] = 8 + 9;
        offset[10] = 7 + 9;
        offset[11] = 6 + 9;

        // Right
        offset[12] = 2 + (5 * 9);
        offset[13] = 5 + (5 * 9);
        offset[14] = 8 + (5 * 9);

        // Down
        offset[15] = 0 + (3 * 9);
        offset[16] = 1 + (3 * 9);
        offset[17] = 2 + (3 * 9);

        // Left
        offset[18] = 6 + (4 * 9);
        offset[19] = 3 + (4 * 9);
        offset[20] = 0 + (4 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face0(numRotation - 1);
    }

    public void rotate_face1(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 2 + 9;
        offset[1] = 1 + 9;
        offset[2] = 0 + 9;

        offset[3] = 5 + 9;
        offset[4] = 4 + 9;
        offset[5] = 3 + 9;

        offset[6] = 8 + 9;
        offset[7] = 7 + 9;
        offset[8] = 6 + 9;

        // ClockWise
        // Up
        offset[9] = 8 + (2 * 9);
        offset[10] = 7 + (2 * 9);
        offset[11] = 6 + (2 * 9);

        // Right
        offset[12] = 0 + (5 * 9);
        offset[13] = 1 + (5 * 9);
        offset[14] = 2 + (5 * 9);

        // Down
        offset[15] = 0 + (0 * 9);
        offset[16] = 1 + (0 * 9);
        offset[17] = 2 + (0 * 9);

        // Left
        offset[18] = 0 + (4 * 9);
        offset[19] = 1 + (4 * 9);
        offset[20] = 2 + (4 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face1(numRotation - 1);
    }

    public void rotate_face2(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 6 + (2 * 9);
        offset[1] = 7 + (2 * 9);
        offset[2] = 8 + (2 * 9);

        offset[3] = 3 + (2 * 9);
        offset[4] = 4 + (2 * 9);
        offset[5] = 5 + (2 * 9);

        offset[6] = 0 + (2 * 9);
        offset[7] = 1 + (2 * 9);
        offset[8] = 2 + (2 * 9);

        // ClockWise
        // Up
        offset[9] = 0 + (1 * 9);
        offset[10] = 1 + (1 * 9);
        offset[11] = 2 + (1 * 9);

        // Right
        offset[12] = 2 + (4 * 9);
        offset[13] = 5 + (4 * 9);
        offset[14] = 8 + (4 * 9);

        // Down
        offset[15] = 8 + (3 * 9);
        offset[16] = 7 + (3 * 9);
        offset[17] = 6 + (3 * 9);

        // Left
        offset[18] = 6 + (5 * 9);
        offset[19] = 3 + (5 * 9);
        offset[20] = 0 + (5 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face2(numRotation - 1);
    }

    public void rotate_face3(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 2 + (3 * 9);
        offset[1] = 1 + (3 * 9);
        offset[2] = 0 + (3 * 9);

        offset[3] = 5 + (3 * 9);
        offset[4] = 4 + (3 * 9);
        offset[5] = 3 + (3 * 9);

        offset[6] = 8 + (3 * 9);
        offset[7] = 7 + (3 * 9);
        offset[8] = 6 + (3 * 9);

        // ClockWise
        // Up
        offset[9] = 8 + (0 * 9);
        offset[10] = 7 + (0 * 9);
        offset[11] = 6 + (0 * 9);

        // Right
        offset[12] = 8 + (5 * 9);
        offset[13] = 7 + (5 * 9);
        offset[14] = 6 + (5 * 9);

        // Down
        offset[15] = 0 + (2 * 9);
        offset[16] = 1 + (2 * 9);
        offset[17] = 2 + (2 * 9);

        // Left
        offset[18] = 8 + (4 * 9);
        offset[19] = 7 + (4 * 9);
        offset[20] = 6 + (4 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face3(numRotation - 1);
    }

    public void rotate_face4(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 2 + (4 * 9);
        offset[1] = 1 + (4 * 9);
        offset[2] = 0 + (4 * 9);

        offset[3] = 5 + (4 * 9);
        offset[4] = 4 + (4 * 9);
        offset[5] = 3 + (4 * 9);

        offset[6] = 8 + (4 * 9);
        offset[7] = 7 + (4 * 9);
        offset[8] = 6 + (4 * 9);

        // ClockWise
        // Up
        offset[9] = 2 + (1 * 9);
        offset[10] = 5 + (1 * 9);
        offset[11] = 8 + (1 * 9);

        // Right
        offset[12] = 2 + (0 * 9);
        offset[13] = 5 + (0 * 9);
        offset[14] = 8 + (0 * 9);

        // Down
        offset[15] = 2 + (3 * 9);
        offset[16] = 5 + (3 * 9);
        offset[17] = 8 + (3 * 9);

        // Left
        offset[18] = 2 + (2 * 9);
        offset[19] = 5 + (2 * 9);
        offset[20] = 8 + (2 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face4(numRotation - 1);
    }

    public void rotate_face5(int numRotation)
    {
        if (numRotation < 1)
            return ;
        //int offset[] = new int[9 + 4 * 3];
        int offset[] = new int[9 + 4 * 3];
        int buffer[] = new int[9 + 4 * 3];
        offset[0] = 2 + (5 * 9);
        offset[1] = 1 + (5 * 9);
        offset[2] = 0 + (5 * 9);

        offset[3] = 5 + (5 * 9);
        offset[4] = 4 + (5 * 9);
        offset[5] = 3 + (5 * 9);

        offset[6] = 8 + (5 * 9);
        offset[7] = 7 + (5 * 9);
        offset[8] = 6 + (5 * 9);

        // ClockWise
        // Up
        offset[9] = 6 + (1 * 9);
        offset[10] = 3 + (1 * 9);
        offset[11] = 0 + (1 * 9);

        // Right
        offset[12] = 6 + (2 * 9);
        offset[13] = 3 + (2 * 9);
        offset[14] = 0 + (2 * 9);

        // Down
        offset[15] = 6 + (3 * 9);
        offset[16] = 3 + (3 * 9);
        offset[17] = 0 + (3 * 9);

        // Left
        offset[18] = 6 + (0 * 9);
        offset[19] = 3 + (0 * 9);
        offset[20] = 0 + (0 * 9);

        for (int i = 0; i < 21; i++)
            buffer[i] = status[offset[i]];
        // Face
        status[offset[0]] = buffer[6];
        status[offset[1]] = buffer[3];
        status[offset[2]] = buffer[0];
        status[offset[3]] = buffer[7];
        status[offset[5]] = buffer[1];
        status[offset[6]] = buffer[8];
        status[offset[7]] = buffer[5];
        status[offset[8]] = buffer[2];

        // Top
        status[offset[9]] = buffer[18];
        status[offset[10]] = buffer[19];
        status[offset[11]] = buffer[20];

        // Right
        status[offset[12]] = buffer[9];
        status[offset[13]] = buffer[10];
        status[offset[14]] = buffer[11];

        // Down
        status[offset[15]] = buffer[12];
        status[offset[16]] = buffer[13];
        status[offset[17]] = buffer[14];

        // Down
        status[offset[18]] = buffer[15];
        status[offset[19]] = buffer[16];
        status[offset[20]] = buffer[17];
        rotate_face5(numRotation - 1);
    }

    public void debugRotate(int faceIndex, int numRotation)
    {
        faceIndex %= 8;

        //debugSetFaceColor(faceIndex, 0);
        debugIterFaceColor(faceIndex);
        if (1 == 1)
            return ;
        //debugSetFaceTileIndexColor(faceIndex, 0, 0);
        //debugSetFaceTileIndexColor(faceIndex, 2, getStatus(faceIndex, 4));
        //debugSetFaceTileIndexColor(faceIndex, 2, 0); // TILE TWO DOUBLE ??????????? NO TILE TWO ON FACE FOUR
        //debugSetFaceTileIndexColor(faceIndex, 5, 0); // TILE FIVE BUGGIN WITH TILE TWO ???????????
        //debugSetFaceTileIndexColor(faceIndex, 7, 0); // TILE SEVEN DOUBLE ?????
        //debugSetFaceTileIndexColor(faceIndex, 8, 0); // TILE HEIGH UNUSED ???????
        int tileToHighlight;

        tileToHighlight = 0;
        for (int i = 0; i < 6; i++)
        {
            // 8
            // 7
            // 6
            // 5
            // 4
            // 3
            // 2 // Buggy artefacts
            // 1
            // 0
            /*
            debugSetFaceTileIndexColor(i, 6, 0);
            debugSetFaceTileIndexColor(i, 8, 0);
            */
            //debugSetFaceTileIndexColor(i, 2, 0);
            //debugSetFaceTileIndexColor(i, 7, 0);
            debugSetFaceTileIndexColor(i, tileToHighlight, 0);
            /*if (i == 4)
                debugSetFaceTileIndexColor(i, faceToHighlight, 0);*/
        }
        /*
        Cube debug;
        debug = new Cube(4, 4, 4);
        faceIndex %= 8;
        System.out.println("BATMAN :: Rubixcube :: Rotate :: Start");
        for (int t = 0; t < 1; t++)
        {
            voxelRingBuffer[t].copy(voxel[grab[(faceIndex * 8) + t]]);
            //voxelRingBuffer[t].copy(debug);
            System.out.println("BATMAN :: Rubixcube :: Rotate :: voxelBuffer !"+faceIndex+" v("+t+") :: " + voxelRingBuffer[t].toString());
        }
        for (int t = 0; t < 1; t++)
        {
            voxel[grab[(faceIndex * 8) + ((t + 2) % 8)]].copy(voxelRingBuffer[t]);
            System.out.println("BATMAN :: Rubixcube :: Rotate :: voxel !"+faceIndex+" v("+t+") :: " + voxel[t].toString());
        }
        */
        /*
        System.out.println("BATMAN :: Rubixcube :: Rotate :: Start");
        for (int t = 0; t < 8; t++)
        {
            voxelRingBuffer[t].copy(voxel[grab[(faceIndex * 8) + t]]);
            System.out.println("BATMAN :: Rubixcube :: Rotate :: voxelBuffer !"+faceIndex+" v("+t+") :: " + voxelRingBuffer[t].toString());
        }
        for (int t = 0; t < 8; t++)
        {
            voxel[grab[(faceIndex * 8) + ((t + 2) % 8)]].copy(voxelRingBuffer[t]);
            System.out.println("BATMAN :: Rubixcube :: Rotate :: voxel !"+faceIndex+" v("+t+") :: " + voxel[t].toString());
        }
        */
    }

    public void             rotate(int face, int numRotation)
    {
        // DEBUG
        if (1 == 1) {
            switch (face)
            {
                case 0:
                    rotate_face0(numRotation);
                    break;
                case 1:
                    rotate_face1(numRotation);
                    break;
                case 2:
                    rotate_face2(numRotation);
                    break;
                case 3:
                    rotate_face3(numRotation);
                    break;
                case 4:
                    rotate_face4(numRotation);
                    break;
                case 5:
                    rotate_face5(numRotation);
                    break;
            }
            //debugRotate(face, 0);
            //debugRotate(5, 0);
            //updateStatus();
            if (1234567 == 1234567)
                return;
        }
        // DEBUG

        int         offset;
        int         faceOffset;

        Log.e("ROTATE", "Face: #" + numRotation); // Debug
        face = Math.abs(face) % 6;
        numRotation = Math.abs(numRotation) % 4;
        offset = 8 * face;
        while (numRotation-- > 0)
        {
            for (int t = 0; t < 8; t++)
            {
                /** ORIGINAL
                 * voxelBuffer[t].copy(voxel[grab[offset + t]]);
                 */
                voxelRingBuffer[t].copy(voxel[grab[offset + t]]);
            }
            for (int t = 0; t < 8; t++)
            {
                /**
                 * ORIGINAL
                 * voxel[grab[offset + ((t + 2) % 8)]].copy(voxelRingBuffer[t]);
                 */
                // TESTING
                voxel[grab[offset + ((t + 2) % 8)]].copyRot(voxelRingBuffer[t], t);
            }
            /*
            voxel[grab[offset + 2]].copy(voxelBuffer[t]);
            voxel[grab[offset + 5]].copy(voxelBuffer[1]);
            voxel[grab[offset + 8]].copy(voxelBuffer[2]);
            voxel[grab[offset + 1]].copy(voxelBuffer[3]);

            voxel[grab[offset + 7]].copy(voxelBuffer[5]);
            voxel[grab[offset + 0]].copy(voxelBuffer[6]);
            voxel[grab[offset + 3]].copy(voxelBuffer[7]);
            voxel[grab[offset + 6]].copy(voxelBuffer[8]);
            */
        }
        updateStatus();
    }

    private void updateStatus()
    {
        /* // TODO Debug
        for (int f = 0; f < 6; f++)
            for (int t = 0; t < 9; t++)
                setStatus(f, t, f);
        */
        setStatus(0, 0, voxel[6].colorIndex[2]);
        setStatus(0, 1, voxel[7].colorIndex[1]);
        setStatus(0, 2, voxel[8].colorIndex[1]);
        setStatus(0, 3, voxel[15].colorIndex[1]);
        setStatus(0, 4, voxel[16].colorIndex[0]); // TODO
        setStatus(0, 5, voxel[17].colorIndex[0]);
        setStatus(0, 6, voxel[24].colorIndex[1]);
        setStatus(0, 7, voxel[25].colorIndex[1]);
        setStatus(0, 8, voxel[26].colorIndex[2]);
        /** Layout OK **/

        setStatus(1, 0, voxel[0].colorIndex[0]);
        setStatus(1, 1, voxel[1].colorIndex[0]);
        setStatus(1, 2, voxel[2].colorIndex[0]);
        setStatus(1, 3, voxel[3].colorIndex[0]);
        setStatus(1, 4, voxel[4].colorIndex[0]); // TODO
        setStatus(1, 5, voxel[5].colorIndex[0]);
        setStatus(1, 6, voxel[6].colorIndex[0]);
        setStatus(1, 7, voxel[7].colorIndex[0]);
        setStatus(1, 8, voxel[8].colorIndex[0]);
        /** Layout OK **/

        setStatus(2, 0, voxel[2].colorIndex[2]);
        setStatus(2, 1, voxel[1].colorIndex[1]);
        setStatus(2, 2, voxel[0].colorIndex[1]);
        setStatus(2, 3, voxel[11].colorIndex[1]);
        setStatus(2, 4, voxel[10].colorIndex[0]); // TODO
        setStatus(2, 5, voxel[9].colorIndex[0]);
        setStatus(2, 6, voxel[20].colorIndex[1]);
        setStatus(2, 7, voxel[19].colorIndex[1]);
        setStatus(2, 8, voxel[18].colorIndex[2]);
        /** Layout OK **/

        setStatus(3, 0, voxel[20].colorIndex[0]);
        setStatus(3, 1, voxel[19].colorIndex[0]);
        setStatus(3, 2, voxel[18].colorIndex[0]);
        setStatus(3, 3, voxel[23].colorIndex[0]);
        setStatus(3, 4, voxel[22].colorIndex[0]); // TODO
        setStatus(3, 5, voxel[21].colorIndex[0]);
        setStatus(3, 6, voxel[26].colorIndex[0]);
        setStatus(3, 7, voxel[25].colorIndex[0]);
        setStatus(3, 8, voxel[24].colorIndex[0]);
        /** Layout OK **/

        setStatus(4, 0, voxel[0].colorIndex[2]);
        setStatus(4, 1, voxel[3].colorIndex[1]);
        setStatus(4, 2, voxel[6].colorIndex[1]);
        setStatus(4, 3, voxel[9].colorIndex[1]);
        setStatus(4, 4, voxel[12].colorIndex[0]); // TODO
        setStatus(4, 5, voxel[15].colorIndex[0]);
        setStatus(4, 6, voxel[18].colorIndex[1]);
        setStatus(4, 7, voxel[21].colorIndex[1]);
        setStatus(4, 8, voxel[24].colorIndex[2]);
        /** Layout OK **/

        setStatus(5, 0, voxel[8].colorIndex[2]);
        setStatus(5, 1, voxel[5].colorIndex[1]);
        setStatus(5, 2, voxel[2].colorIndex[1]);
        setStatus(5, 3, voxel[17].colorIndex[1]);
        setStatus(5, 4, voxel[14].colorIndex[0]); // TODO
        setStatus(5, 5, voxel[11].colorIndex[0]);
        setStatus(5, 6, voxel[26].colorIndex[1]);
        setStatus(5, 7, voxel[23].colorIndex[1]);
        setStatus(5, 8, voxel[20].colorIndex[2]);
        /** Layout OK **/
        /*
        setStatus(0, 0, getCube(14, 0));
        setStatus(0, 1, getCube(2, 0));
        setStatus(0, 2, getCube(15, 0));
        setStatus(0, 3, getCube(7, 0));
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(0, 5, getCube(6, 1));
        setStatus(0, 6, getCube(18, 2));
        setStatus(0, 7, getCube(10, 0));
        setStatus(0, 8, getCube(19, 1));

        setStatus(1, 0, getCube(13, 0));
        setStatus(1, 1, getCube(0, 0));
        setStatus(1, 2, getCube(12, 0));
        setStatus(1, 3, getCube(1, 0));
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(1, 5, getCube(3, 0));
        setStatus(1, 6, getCube(14, 0));
        setStatus(1, 7, getCube(2, 0));
        setStatus(1, 8, getCube(15, 0));

        setStatus(2, 0, getCube(13, 2));
        setStatus(2, 1, getCube(0, 1));
        setStatus(2, 2, getCube(12, 1));
        setStatus(2, 3, getCube(5, 0));
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(2, 5, getCube(4, 1));
        setStatus(2, 6, getCube(17, 1));
        setStatus(2, 7, getCube(8, 1));
        setStatus(2, 8, getCube(16, 2));

        setStatus(3, 0, getCube(17, 0));
        setStatus(3, 1, getCube(8, 0));
        setStatus(3, 2, getCube(16, 0));
        setStatus(3, 3, getCube(9, 0));
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(3, 5, getCube(11, 0));
        setStatus(3, 6, getCube(18, 0));
        setStatus(3, 7, getCube(10, 0));
        setStatus(3, 8, getCube(19, 0));

        setStatus(4, 0, getCube(15, 1));
        setStatus(4, 1, getCube(3, 1));
        setStatus(4, 2, getCube(12, 2));
        setStatus(4, 3, getCube(6, 0)); //////////////////////////////////////////////////
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(4, 5, getCube(4, 1));
        setStatus(4, 6, getCube(19, 2));
        setStatus(4, 7, getCube(11, 1));
        setStatus(4, 8, getCube(16, 1));

        setStatus(5, 0, getCube(13, 1));
        setStatus(5, 1, getCube(1, 1));
        setStatus(5, 2, getCube(14, 2));
        setStatus(5, 3, getCube(5, 1)); /////////////////////////
        //setStatus(0, 4, getCube(14, 0)); // TODO Verifier qu'il n'y ai pas de corruption
        setStatus(5, 5, getCube(7, 0));
        setStatus(5, 6, getCube(17, 2));
        setStatus(5, 7, getCube(9, 0));
        setStatus(5, 8, getCube(18, 1));
        */
    }

    private void setStatus(int face, int tile, int value)
    {
        status[(face * 9) + tile] = value;
    }

    private int getStatus(int face, int tile)
    {
        return (status[face * 9 + tile]);
    }

    /*private int getCube(int index, int face)
    {
        return (cube[index * 3 + face]);
    }
    */
    public int               getColorIndex(int face, int index)
    {
        return (0); // TODO ---------------------------------------------
    }

    public void              setCullingMode(boolean bool)
    {
        cullingMode = bool;
        if (!cullingMode)
        {
            for (int i = 0; i < 6; i++)
                culling[i] = 1f;
            return ;
        }
    }

    public boolean              getCullingMode() { return (cullingMode); }

    public void       setPalette(Tileset16[] tile, int palette[], int color_a, int color_b, int color_c, int color_d, int color_e, int color_f)
    {
        palette[0] = mulColor(color_a, ambiantLight);
        palette[1] = mulColor(color_b, ambiantLight);
        palette[2] = mulColor(color_c, ambiantLight);
        palette[3] = mulColor(color_d, ambiantLight);
        palette[4] = mulColor(color_e, ambiantLight);
        palette[5] = mulColor(color_f, ambiantLight);
        if (1 == 0)
            for (int i = 0; i < 6; i++)
                tile[i].setColorBackground(palette[i]);
    }

    public static int              complementaryColor(int index)
    {
        index %= 6;
        switch (index)
        {
            case 0: return (0xFFFF00FF);
            case 1: return (0xFF0000FF);
            case 2: return (0xFF00FFFF);
            case 3: return (0xFFFFFF00);
            case 4: return (0xFFFF0000);
            case 5: return (0xFF005AFF);
            default: return (Color.LTGRAY);
        }
    }

    public static int[]       makePalette(int color_a, int color_b, int color_c, int color_d, int color_e, int color_f)
    {
        int[]     palette;

        palette = new int[6];
        palette[0] = color_a;
        palette[1] = color_b;
        palette[2] = color_c;
        palette[3] = color_d;
        palette[4] = color_e;
        palette[5] = color_f;
        return (palette);
    }

    private void        straightenCube(InterplanetaryCompanion comp, float[] out, float[] in)
    {
        float[]     right;
        float[]     up;
        float[]     front;
        int         size;
        int         i;
        int         j;

        size = in.length / 3;
        i = -1;
        j = 0;
        right = comp.getRight();
        up = comp.getUp();
        front = comp.getFront();
        while (++i < size)
        {
            vecAt(this.tmp, in, i);
            copyVec(tmp___, right);
            mulVector(tmp___, this.tmp[0]);
            copyVec(tmp_, up);
            mulVector(tmp_, this.tmp[1]);
            copyVec(tmp__, front);
            mulVector(tmp__, this.tmp[2]);
            addVectorCopy(this.tmp, tmp_, tmp___);
            addVectorCopy(tmp____, this.tmp, tmp__);
            out[j++] = tmp____[0];
            out[j++] = tmp____[1];
            out[j++] = tmp____[2];
        }
    }

    static private float[]     getFaceAxis(int face, InterplanetaryCompanion comp)
    {
        switch (face)
        {
            case 0: return (comp.getFront()); // GREEN :: FRONT
            case 1: return (comp.getUp()); // YELLOW :: UP
            case 2: return (comp.getBack()); // RED :: BACK
            case 3: return (comp.getDown()); // BLUE :: DOWN
            case 4: return (comp.getLeft()); // WHITE :: LEFT
            case 5: return (comp.getRight()); // ORANGE :: RIGHT
            default: return (comp.getDown()); // ...
        }
    }

    private void        culling(InterplanetaryCompanion comp)
    {
        float       dot;
        float       max;
        float[]     axis;

        max = 0f;
        for (int face = 0; face < 6; face++)
        {
            axis = getFaceAxis(face, comp);
            dot = dotProd(axis, cameraDirection);
            culling[face] = dot;
            if (dot > max)
            {
                max = dot;
                culling[6] = (float)face;
            }
        }
    }

    public void         straightenLight(InterplanetaryCompanion comp)
    {
        copyVec(tmp___, comp.getRight());
        mulVector(tmp___, sunPosition_global[0]);
        //mulVector(tmp___, sunPosition[0]);
        copyVec(tmp_, comp.getUp());
        mulVector(tmp_, sunPosition_global[1]);
        //mulVector(tmp_, sunPosition[1]);
        copyVec(tmp__, comp.getFront());
        mulVector(tmp__, sunPosition_global[2]);
        //mulVector(tmp__, sunPosition[2]);
        addVectorCopy(tmp, tmp_, tmp___);
        addVectorCopy(sunPosition, tmp, tmp__);

        if (1 == 1)
            return ;
        normalize(sunDirection, sunPosition);
        revVectorCopy(sunDirection_, sunDirection);

        normalize(sunDirection, sunPosition_global); // TODO DEBUG
        revVectorCopy(sunDirection_, sunDirection);

        /*
        float[]         axis;

        axis = comp.getRight();
        sunPosition[0] = axis[0] * sunPosition_global[0];
        sunPosition[1] = axis[1] * sunPosition_global[0];
        sunPosition[2] = axis[2] * sunPosition_global[0];
        axis = comp.getUp();
        sunPosition[0] += axis[0] * sunPosition_global[1];
        sunPosition[1] += axis[1] * sunPosition_global[1];
        sunPosition[2] += axis[2] * sunPosition_global[1];
        axis = comp.getFront();
        sunPosition[0] += axis[0] * sunPosition_global[2];
        sunPosition[1] += axis[1] * sunPosition_global[2];
        sunPosition[2] += axis[2] * sunPosition_global[2];
        */
    }

    private void computeSpecular()
    {
        int         offset;

        offset = 0;
        for (int face = 0; face < 6; face++)
        {
            //specularFaceDot[face * 9 + 4] = dotProd(axis, sunDirection); // TODO
            for (int tile = 0; tile < 9; tile++) {
                // TODO May cause conflicts if change vertice layouts in RubixCube and Rasterizer
                tmp[0] = verticeOutput[offset];
                tmp[1] = verticeOutput[offset + 1];
                tmp[2] = verticeOutput[offset + 2];
                offset += 3;
                subVectorCopy(tmp_, sunPosition_global, tmp);
                normalize(tmp__, tmp_);
                subVectorCopy(tmp___, cameraPosition, tmp);
                normalize(tmp____, tmp___);
                // TODO A verifier
                specularFaceDot[face * 9 + tile] = dotProd(tmp____, tmp__);
            }
            offset += 12;
        }
    }

    private void computeDiffuse(InterplanetaryCompanion comp)
    {
        int         faceOffset;
        int         offset;
        float       dot;
        float[]     axis;

        offset = 0;
        faceOffset = 0;
        for (int face = 0; face < 6; face++)
        {
            axis = getFaceAxis(face, comp);
            //specularFaceDot[face * 9 + 4] = dotProd(axis, sunDirection); // TODO
            for (int tile = 0; tile < 9; tile++) {
                // TODO May cause conflict if change vertice layouts in RubixCube and Rasterizer
                //tmp[0] = tilePosition[offset];
                //tmp[1] = tilePosition[offset + 1];
                //tmp[2] = tilePosition[offset + 2];
                tmp[0] = verticeOutput[offset];
                tmp[1] = verticeOutput[offset + 1];
                tmp[2] = verticeOutput[offset + 2];
                offset += 3;
                //subVectorCopy(tmp___, sunPosition_global, tmp);
                subVectorCopy(tmp_, cameraPosition, tmp);
                normalize(tmp__, tmp_);
                // TODO A verifier
                //diffuseFaceDot[faceOffset + tile] = dotProd(axis, tmp__);
                //dot = dotProd(axis, tmp__);
                //if (dot > 0f)
                //    dot = 1f - dot;
                dot = dotProd(axis, tmp__);
                diffuseFaceDot[faceOffset + tile] = dot * dot * dot;
            }
            faceOffset += 9;
        }
    }

    public float[]      getVertices(float scale, InterplanetaryCompanion comp, int clock)
    {
        return (scaleModel(getVertices(comp, clock), scale));
    }

    public float[]      getVertices(InterplanetaryCompanion comp, int clock)
    {
        comp.refreshOrientation();
        straightenLight(comp);
        if (cullingMode)
            culling(comp);
        else
            for (int i = 0; i < 6; i++)
                culling[i] = 1f;
        straightenCube(comp, verticeOutput, vertice);
        refreshTilesPosition(comp, verticeOutput);
        computeSpecular();
        computeDiffuse(comp);
        return (verticeOutput);
    }

    public float[]   getTilePosition()
    {
        return (tilePosition);
    }

    public void      refreshTilesPosition(InterplanetaryCompanion comp, float[] vertice)
    {
        float x;
        float y;
        float z;
        float x_;
        float y_;
        float z_;
        int verticeOffset;
        int tile;
        int offset;

        tile = 0;
        offset = 0;
        //verticeOffset = 0; // TODO ORIGINAL Normal
        verticeOffset = 3 * 10; // TODO DEBUG Reversed
        //for (int t = 0; t < totalTile; t++) // TODO ORIGINAL
        while (verticeOffset < numVerticeFaceLength) // TODO DEBUG
        {
            x = vertice[verticeOffset];
            y = vertice[verticeOffset + 1];
            z = vertice[verticeOffset + 2];
            x_ = vertice[verticeOffset + 15];
            y_ = vertice[verticeOffset + 16];
            z_ = vertice[verticeOffset + 17];
            tilePosition[offset] = x + ((x_ - x) * 0.5f);
            tilePosition[offset + 1] = y + ((y_ - y) * 0.5f);
            tilePosition[offset + 2] = z + ((z_ - z) * 0.5f);
            offset += 3;
            tile++;
            if (tile % 3 == 0) {
                if (tile == 9) {
                    tile = 0;
                    //verticeOffset += 18; // TODO ORIGINAL
                    verticeOffset += (16 * 3) + 30; // TODO DEBUG
                    //verticeOffset += (3 * 26); // TODO DEBUG
                } else
                    //verticeOffset += 6; // TODO ORIGINAL
                    verticeOffset -= 6; // TODO DEBUG
            }
            else
                //verticeOffset += 3; // TODO ORIGINAL
                verticeOffset -= 3; // TODO Debug
        }
    }
}
