package com.rubix.rubix;

import android.graphics.Bitmap;

public class Button {
    public Bitmap       asset;
    private int[]       pixel;
    private int                 assetWidth;
    private int                 assetHeight;
    public Bitmap       asset_alt;
    public int          startX;
    public int          startY;
    public int          stopX;
    public int          stopY;
    public int          posX;
    public int          posY;
    public String       soundName;

    public Button(Bitmap asset, int posX, int posY, int startX, int stopX, int startY, int stopY, String soundName)
    {
        this.asset = asset;
        assetHeight = asset.getHeight();
        assetWidth = asset.getWidth();
        pixel = new int[assetWidth * assetHeight];
        for (int y = 0; y < assetHeight; y++)
            for (int x = 0; x < assetWidth; x++)
                pixel[y * assetWidth + x] = asset.getPixel(x, y);
        asset_alt = null;
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
        this.posX = posX;
        this.posY = posY;
        this.soundName = soundName;
    }

    public int[] getPixels()
    {
        return (pixel);
    }

    public int width()
    {
        return (assetWidth);
    }

    public int height()
    {
        return (assetHeight);
    }
}
