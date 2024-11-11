package com.rubix.rubix;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class UserInterface {
    public int[]                pixel;
    private UIButtonSet[]       uiSet;
    private int                 numberUi;
    private int                 screenWidth;
    private int                 screenHeight;

    public UserInterface(UIButtonSet[] uiSet, int screenWidth, int screenHeight)
    {
        pixel = new int[screenWidth * screenHeight];
        for (int y = 0; y < screenHeight; y++)
            for (int x = 0; x < screenWidth; x++)
                pixel[y * screenWidth + x] = 0x00FF0000; // Transparent
        this.uiSet = uiSet;
        numberUi = uiSet.length;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public boolean click(int x, int y)
    {
        for (int i = 0; i < numberUi; i++)
            if (uiSet[i].buttonClick(x, y))
            {
                System.out.println("BATMAN :: UserInterface :: click :: UiSet ["+i+"]"); // Debug
                return (true);
            }
        return (false);
    }

    private UIButtonSet getSetByName(String name)
    {
        if (name == null)
            return (null);
        for (int i = 0; i < numberUi; i++)
            if (uiSet[i].getName() == name)
                return (uiSet[i]);
        return (null);
    }

    private void setActive(int uiIndex, boolean status)
    {
        if (uiIndex < 0 || uiIndex > numberUi)
            return ;
        uiSet[0].active = status;
    }

    public void setActive(String name, boolean status)
    {
        UIButtonSet set = getSetByName(name);
        if (set == null)
            return ;
        set.active = status;
    }

    public Bitmap       getAsset(int uiSetIndex, int elemIndex) // Debug
    {
        return (uiSet[uiSetIndex].getAsset(elemIndex));
    }

    public void setPixels_ommitAlpha(int[] dst)
    {
        int len;
        int offset;

        len = screenWidth * screenHeight;
        offset = -1;
        while (++offset < len)
            if ((pixel[offset] & 0xFF000000) != 0)
                dst[offset] = pixel[offset];
    }

    public void render()
    {
        for (int i = 0; i < numberUi; i++)
            uiSet[i].render(pixel, screenWidth);
    }
}
