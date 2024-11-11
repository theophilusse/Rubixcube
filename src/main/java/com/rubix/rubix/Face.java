package com.rubix.rubix;

import com.rubix.rubix.Toolbox;
public class Face
{
    public float[]             point;

    public Face(float[] tmpa, float[] tmpb, Toolbox tool, int side)
    {
        final float unit = 2 / 3f;
        int         offset;

        point = new float[16 * 3];
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 4; x++)
            {
                offset = ((y * 4) + x) * 3;
                point[offset] = -1f + ((float)x * unit);
                point[offset + 1] = -1f - ((float)y * unit * -1f);
                point[offset + 2] = -1f;
            }
        if (side < 4)
        {
            while (side-- > 0)
                for (int i = 0; i < point.length; i += 3) {
                    tmpb[0] = point[i];
                    tmpb[1] = point[i + 1];
                    tmpb[2] = point[i + 2];
                    Toolbox.rotVec(tmpa, tool.axis.front, tmpb, Toolbox.rad90);
                    point[i] = tmpa[0];
                    point[i + 1] = tmpa[1];
                    point[i + 2] = tmpa[2];
                }
        }
        else
            for (int i = 0; i < point.length; i += 3)
            {
                tmpb[0] = point[i];
                tmpb[1] = point[i + 1];
                tmpb[2] = point[i + 2];
                if (side == 4)
                    Toolbox.rotVec(tmpa, tool.axis.up, tmpb, Toolbox.rad90);
                else
                    Toolbox.rotVec(tmpa, tool.axis.up, tmpb, Toolbox.rad90 * 3f);
                point[i] = tmpa[0];
                point[i + 1] = tmpa[1];
                point[i + 2] = tmpa[2];
            }
    }
}
