package com.rubix.rubix;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class UIButtonSet {
    public Bitmap       pixel;
    private Button[]    uiButton;
    private int         numButton;
    private int         functionIndex;
    private Object      object_a;
    private Object      object_b;
    private Object      object_c;
    private String      name;
    public boolean      active;


    public UIButtonSet(int screenWidth, int screenHeight, Button[] uiButton, int functionIndex, String name, Object arg0, Object arg1, Object arg2)
    {
        pixel = Bitmap.createBitmap(
                screenWidth,
                screenHeight,
                Bitmap.Config.ARGB_8888);
        setName(name);
        this.uiButton = uiButton;
        numButton = uiButton.length;
        this.functionIndex = functionIndex;
        object_a = arg0;
        object_b = arg1;
        object_c = arg2;
        active = true;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getName()
    {
        return (name);
    }

    public void setName(String name)
    {
        this.name = name;
        if (name == null)
            this.name = "";
    }

    private int getButtonClick(int x, int y)
    {
        int button;

        button = -1;
        while (++button < numButton)
        {
            if (x >= uiButton[button].startX &&
                    x <= uiButton[button].stopX &&
                    y >= uiButton[button].startY &&
                    y <= uiButton[button].stopY)
                break;
        }
        return (button);
    }

    private void callButtonFunction(int button)
    {
        switch (functionIndex)
        {
            case 0: ft_rotate(button);
        }
    }

    public boolean pressButton(int button)
    {
        GameManager gm;
        Button b;

        if (active == false)
            return (false);
        System.out.println("BATMAN :: UiButtonSet :: pressButton :: button["+button+"]"); // Debug
        gm = (GameManager)object_a;
        if (button < 0 || button >= uiButton.length)
            return (false);
        System.out.println("BATMAN :: UserInterface :: UIButtonSet :: Button #" + button); // Debug
        b = uiButton[button];
        System.out.println("BATMAN :: UserInterface :: UIButtonSet :: play '" + b.soundName + "'"); // Debug
        gm.soundManager.play(b.soundName);
        callButtonFunction(button);
        return (true);
    }

    public boolean buttonClick(int x, int y)
    {
        if (active)
            return (pressButton(getButtonClick(x, y)));
        return (false);
    }

    public Bitmap       getAsset(int elemIndex)
    {
        return (uiButton[elemIndex].asset);
    }

    private void renderButton(int[] dst, int width, Button b)
    {
        int[]   pix;
        int     w;
        int     h;

        pix = b.getPixels();
        w = b.width();
        h = b.height();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                dst[(y + b.posY) * width + (x + b.posX)] = pix[y * w + x];
    }

    //private void render(backbuffer.getWidth(), backbuffer.getHeight())
    public void render(int[] dest, int width)
    {
        if (active == false)
            return ;
        for (int i = 0; i < numButton; i++)
            renderButton(dest, width, uiButton[i]);
    }

    private void ft_rotate(int button)
    {
        GameManager gameManager = (GameManager)object_a;
        Rubixcube rubix = (Rubixcube)object_b;

        gameManager.madeAMove();
        rubix.easterEgg = 0;
        // TODO Rotation des faces ----------------------------
        // TODO rubix.rotate(button);
        boolean clockwise;
        int face;

        if (button > 1)
            return ;
        face = 0;
        clockwise = button == 1;
        rubix.rotate(rubix.grabbed, clockwise ? 3 : 1);
    }
}
