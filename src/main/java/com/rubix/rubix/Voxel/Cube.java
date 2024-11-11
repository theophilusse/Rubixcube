package com.rubix.rubix.Voxel;

public class Cube
{
    public int[] colorIndex;

    public Cube(int a, int b, int c)
    {
        int         type;

        type = 0;
        type += (a != -1) ? 1 : 0;
        type += (b != -1) ? 1 : 0;
        type += (c != -1) ? 1 : 0;
        if (type == 0)
        {
            colorIndex = null;
            type--;
        }
        else
            colorIndex = new int[3];
            //colorIndex = new int[type];
        switch (type)
        {
            case 1:
                colorIndex[0] = a;
                break;
            case 2:
                colorIndex[0] = a;
                colorIndex[1] = b;
                break;
            case 3:
                colorIndex[0] = a;
                colorIndex[1] = b;
                colorIndex[2] = c;
                break;
        }
    }

    public void         setType(int a, int b, int c)
    {
        this.colorIndex[0] = a;
        this.colorIndex[1] = b;
        this.colorIndex[2] = c;
    }

    public void         copy(Cube c)
    {
        this.colorIndex[0] = c.colorIndex[0];
        this.colorIndex[1] = c.colorIndex[1];
        this.colorIndex[2] = c.colorIndex[2];
    }

    public void         copyRot(Cube c, int index)
    {
        if (index % 2 == 0) {
            if ((index / 2) % 2 > 0)
                copyRot_counterClockwise(c);
            else
                copyRot_clockwise(c);
        }
        else
        {
            copyRot_swap(c);
        }
        /*
        switch (index)
        {
            case 0: copyRot_clockwise(c);
            case 2: copyRot_clockwise(c);
            case 6: copyRot_counterClockwise(c);
        }*/
    }

    public void         copyRot_swap(Cube c)
    {
        this.colorIndex[0] = c.colorIndex[1];
        this.colorIndex[1] = c.colorIndex[0];
    }

    public void         copyRot_clockwise(Cube c)
    {
        this.colorIndex[0] = c.colorIndex[1];
        this.colorIndex[1] = c.colorIndex[2];
        this.colorIndex[2] = c.colorIndex[0];
    }

    public void         copyRot_counterClockwise(Cube c)
    {
        this.colorIndex[0] = c.colorIndex[2];
        this.colorIndex[1] = c.colorIndex[0];
        this.colorIndex[2] = c.colorIndex[1];
    }

    public String       toString() // Debug
    {
        String str;
        int type;
        int a;
        int b;
        int c;

        a = this.colorIndex[0];
        b = -1;
        c = -1;
        type = 2;
        try {
            b = this.colorIndex[1];
            try {
                c = this.colorIndex[2];
            } catch (Exception e) { type = 1; }
        } catch (Exception e) { type = 0; }
        str = "Cube("+type+")["+a+"]["+b+"]["+c+"]";
        return (str);
    }
}