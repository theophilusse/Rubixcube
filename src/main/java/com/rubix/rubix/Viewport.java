package com.rubix.rubix;

import static com.rubix.rubix.Toolbox.copyVec;
import static com.rubix.rubix.Toolbox.drawLine;
import static com.rubix.rubix.Toolbox.getColor;
import static com.rubix.rubix.Toolbox.mulColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.rubix.rubix.GameManager;
import com.rubix.rubix.InterplanetaryCompanion;
import com.rubix.rubix.Rubixcube;
import com.rubix.rubix.Tileset16;
import com.rubix.rubix.Toolbox;
import com.rubix.rubix.Rasterizer;
import com.rubix.rubix.UIButtonSet;
import com.rubix.rubix.UserInterface;
import com.rubix.rubix.Button;

//import com.google.firebase.database.DatabaseReference;

public class Viewport extends SurfaceView implements View.OnClickListener {
    public boolean              running;
    private boolean             initialized;
    private boolean             locked;
    private boolean             doNotRender;
    private boolean             renderDebug;
    private boolean             renderWireframe;
    private boolean             renderAnaglyph;
    private boolean             renderFisheye;
    private Context             context;
    private int                 availableCores;
    private int                 numberCores;

    private Toolbox             tool;
    private Rasterizer[]        raster;
    private SurfaceHolder       holder;
    protected Canvas            canvas;

    private BitmapShader        sh;
    private Bitmap[]            asset;
    private Bitmap              skymapBuffer;
    private Bitmap              backbuffer;
    private int[]               backbufferPixel;
    private int[]               imgBufferA;
    private int[]               imgBufferB;
    private int[]               imgBufferC;
    private Bitmap              anaglyph_L; // Todo Blue
    private Bitmap              anaglyph_R; // Todo Red
    public InterplanetaryCompanion comp;
    private Tileset16           tileset;
    private Tileset16           tileset_blue;
    private int                 anaglyphDepth;

    //public DatabaseReference firebaseDatabase;
    public GameManager          gameManager;
    public Rubixcube            rubix;
    private Matrix              mata;
    private int                 width;
    private int                 height;
    private int                 halfScreenWidth;
    private int                 halfScreenHeight;
    private int                 quarterScreenWidth;
    private int                 quarterScreenHeight;
    private int                 sixthScreenWidth;
    private int                 sixthScreenHeight;
    private int                 clipX_start;
    private int                 clipX_stop;
    private int                 clipY_start;
    private int                 clipY_stop;
    private float               baseScale;
    private float               scale;
    private int                 selectFrameWidth;
    private int                 marginWidth;

    private float               ambiantLight;
    private float[]             refAxis;
    private float[]             tmp;
    private int[]               tmpa;
    private int[]               tmpb;
    private int[]               tmpVec2;

    private boolean             side;
    private UserInterface       userInterface;
    private int[]               uiLayout;
    private int                 buttonMargin;
    private int                 buttonDim;
    private int                 buttonDim2;
    private int                 numButton;
    private int                 numMode;
    private boolean             renderMode;

    private String              timer;
    private int                 selectionEffect;
    private int                 numSelectionEffect;
    private MengerSponge_face   mengerSponge[];

    public Viewport(Context context, AttributeSet attrs)
    {
        super(context, attrs, 0);
        running = false;
        renderFisheye = false;
        selectionEffect = 0;
        anaglyphDepth = 25;
        availableCores = 4;
        numberCores = 4;
        selectFrameWidth = 3;
        marginWidth = 4;
        rubix = null;
        this.asset = null;
        this.context = context;
        this.
        locked = false;
        numSelectionEffect = 8;
        //setRenderMode(true, false); // Mode Debug
        setRenderMode(false, false);
        gameManager = null;
        tmp = new float[3];
        tmpa = new int[2];
        tmpb = new int[2];
        tmpVec2 = new int[2];
        mata = new Matrix();
        tool = new Toolbox(16 * 6);
        tileset = new Tileset16(Color.RED, Color.BLACK);
        tileset_blue = new Tileset16(Color.BLUE, Color.BLACK);
        rubix = new Rubixcube(
                tool,
                Rubixcube.makePalette(
                        Color.GREEN,
                        Color.YELLOW,
                        Color.RED,
                        Color.BLUE,
                        Color.WHITE,
                        Toolbox.getColor(255, 165, 0) // Orange
                ),
                0.2f,
                true);
        mengerSponge = new MengerSponge_face[3];
        int dim[] = new int[2];
        dim[0] = width;
        dim[1] = height;
        mengerSponge[0] = new MengerSponge_face(9, backbufferPixel, dim);
        mengerSponge[1] = new MengerSponge_face(9, backbufferPixel, dim);
        mengerSponge[2] = new MengerSponge_face(9, backbufferPixel, dim);

        timer = "-";
        initialized = false;
    }

    public void         setSelectFrameWidth(int width)
    {
        selectFrameWidth = Math.min(Math.abs(width), 15);
    }

    public void resetRubixcube()
    {
        gameManager.soundManager.play("click");
        //gameManager.setWin(false);
        gameManager.reset();
        //timer = 0;
        //timerBase = System.nanoTime();
        rubix.shuffle((int)(Math.random() * 20) + 1);
        userInterface.setActive("btnRotation", true);
    }

    ///// TODO TOUCHSCREEN ----------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean     won;
        int         action;
        int         button;
        int         x;
        int         y;

        super.onTouchEvent(event);
        if (!initialized)
            return false;
        action = event.getAction();
        ////
        //if (event.getAction() == MotionEvent.ACTION_DOWN)
        if (action == MotionEvent.ACTION_UP)
        {
            comp.touchScreen_swipeUp();
            doNotRender = true;
        }
        else if (action == MotionEvent.ACTION_DOWN)
        {
            comp.touchScreen_swipeDown((int) event.getX(), (int) event.getY());
            doNotRender = false;
        }
        else if (action == MotionEvent.ACTION_MOVE)
            comp.touchScreen_swipeUpdate((int) event.getX(), (int) event.getY(), halfScreenWidth, halfScreenHeight);
        //System.out.println("BATMAN :: Viewport :: Event Action :: " + event.getAction()); // Debug
        //System.out.println("BATMAN :: Viewport :: Event Action Masked :: " + event.getActionMasked()); // Debug
        ////
        won = gameManager.hasWon();
        if (false && renderMode) // Debug
        {
            if (renderAnaglyph && !renderFisheye)
            {
                renderDebug = true;
                renderWireframe = true;
                renderFisheye = true;
                renderAnaglyph = false;
            }
            else if (!renderAnaglyph && renderFisheye)
            {
                renderDebug = true;
                renderWireframe = true;
                renderFisheye = false;
                renderAnaglyph = false;
            }
            else
            {
                renderDebug = false;
                renderWireframe = true;
                renderFisheye = false;
                renderAnaglyph = true;
            }
            gameManager.soundManager.play(3);
            return true;
        }
        else
        {
            x = (int)event.getX();
            y = (int)event.getY();
            if (action == MotionEvent.ACTION_DOWN)
                if (userInterface.click(x, y)) {
                    if (!gameManager.hasWon() && rubix.isSolved())
                        gameManager.setWin(true);
                } else { // Player touching cube?
                    int pixel;

                    pixel = backbufferPixel[y * width + x];
                    System.out.println("BATMAN :: Viewport :: onTouchEvent :: ColorPix["+pixel+"]"); // Debug
                    if (pixel != 0xFF000000)
                    {
                        if (gameManager.hasWon())
                        {
                            won = false;
                            resetRubixcube();
                        }
                    }
                }
            if (false) // Original FX ring
            {
                selectionEffect = (selectionEffect + 1) % numSelectionEffect;
                renderFisheye = (selectionEffect > 5);
                for (int i = 0; i < numberCores; i++)
                    raster[i].setMarginEffect(selectionEffect > 3 || selectionEffect % 2 == 0);
                if (!gameManager.hasWon() && rubix.easterEgg())
                    gameManager.setWin(true);
                else
                    gameManager.soundManager.play(3);
            }
        }
        if (won != gameManager.hasWon())
        {
            userInterface.setActive("btnRotation", false); // TODO BATMAN
            gameManager.soundManager.play(4);
        }
        return true;
    }

    public void postCanvas() // Testing
    {
        SurfaceHolder holder;

        holder = getHolder();
        holder.unlockCanvasAndPost(canvas);
        locked = false;
    }

    public boolean isLocked()
    {
        return (locked);
    }

    public void setRenderMode(boolean mode, boolean playSound)
    {
        MediaPlayer mp;

        renderMode = mode;
        if (mode)
        {
            renderDebug = true;
            renderAnaglyph = false;
            renderWireframe = true;
            renderFisheye = false;
        }
        else
        {
            renderDebug = false;
            renderAnaglyph = false;
            renderWireframe = false;
            renderFisheye = true;
        }
        if (playSound)
            gameManager.soundManager.play(1);
    }

    @Override
    public void onClick(View v) {
        // TODO Ne passe pas la dedans
        /*
        MediaPlayer mp;

        if (!renderDebug && !renderAnaglyph)
            renderDebug = true;
        else if (renderDebug && !renderAnaglyph)
            renderAnaglyph = true;
        else if (renderDebug && renderAnaglyph)
            renderDebug = false;
        else
            renderAnaglyph = false;

        mp = gameManager.mediaPlayer[1];
        if (mp == null)
            return ;
        //firebaseDatabase.child("onClick").setValue("click!");
        mp.start();
         */
    }
    ///// TODO TOUCHSCREEN ----------------------------------------------------

    public void setRenderAnaglyph(boolean anaglyph)
    {
        renderAnaglyph = anaglyph;
    }

    public void setRenderDebug(boolean debug)
    {
        renderDebug = debug;
    }

    public void initialize(GameManager gameManager,
                           MainActivity mainActivity,
                           InterplanetaryCompanion comp,
                           Bitmap[] asset,
                           int availableCores) throws Exception
    {
        System.out.println("BATMAN :: Viewport :: Initialize"); // Debug
        this.comp = comp;
        this.side = comp.getPortraitSide();
        this.asset = asset;
        this.gameManager = gameManager;
        setWillNotDraw(false);
        holder = getHolder();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        backbufferPixel = new int[width * height];
        imgBufferA = new int[width * height];
        imgBufferB = new int[width * height];
        imgBufferC = new int[width * height];
        backbuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        anaglyph_L = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        anaglyph_R = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        anaglyph_L.eraseColor(Color.TRANSPARENT);
        anaglyph_R.eraseColor(Color.TRANSPARENT);
        canvas = new Canvas(backbuffer);
        halfScreenWidth = width / 2;
        halfScreenHeight = height / 2;
        quarterScreenWidth = halfScreenWidth / 2;
        quarterScreenHeight = halfScreenHeight / 2;
        sixthScreenWidth = width / 6;
        sixthScreenHeight = height / 6;
        baseScale = (float)sixthScreenWidth * 1.41f;
        scale = baseScale; // Hypotenuse
        clipX_start = halfScreenWidth - sixthScreenWidth;
        clipX_stop = halfScreenWidth + sixthScreenWidth;
        clipY_start = halfScreenHeight - sixthScreenWidth;
        clipY_stop = halfScreenHeight + sixthScreenWidth;

        this.availableCores = availableCores;
        numberCores = this.availableCores >= 8 ? 8 : 4;
        raster = new Rasterizer[numberCores];
        for (int id = 0; id < numberCores; id++)
            raster[id] = new Rasterizer(rubix, 0, id, numberCores);
        for (int id = 0; id < numberCores; id++)
            if (raster[id] != null)
                raster[id].start();
        userInterface = uiInit();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                for (int id = 0; id < numberCores; id++)
                    if (raster[id] != null)
                        raster[id].interrupt();
                running = false;
                /*
                boolean retry = true;
                gameManager.setRunning(false);
                while (retry) {
                    try {
                        gameManager.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
                for (int id = 0; id < numberCores; id++)
                    if (raster[id] != null)
                        raster[id].interrupt();
                */
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //gameManager.setRunning(true);
                //gameManager.start();
                if (running)
                    return ;
                sh = new BitmapShader(backbuffer,Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                running = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
        initialized = true;
    }

    public void setRunning(boolean run)
    {
        if (1 == 1)
            return ;
        if (run != running) {
            if (run) {
                for (int id = 0; id < numberCores; id++)
                    if (raster[id] != null)
                        raster[id].start();
                if (comp != null)
                    comp.resume();
            } else {
                for (int id = 0; id < numberCores; id++)
                    if (raster[id] != null)
                        raster[id].interrupt();
                if (comp != null)
                    comp.pause();
            }
        }
        running = run;
    }

    private UserInterface uiInit()
    {
        Button[]        uiButton;
        UIButtonSet[]   buttonSet;
        UserInterface   userInterface;

        numButton = 4;
        buttonMargin = 15; // Margin
        buttonDim = 128; // ; // Button dimension
        buttonDim2 = buttonDim / 2; // Half dimension
        uiLayout = new int[12]; // int2 collection

        uiLayout[0] = width;
        uiLayout[1] = width - buttonDim;
        uiLayout[2] = halfScreenWidth + buttonDim2;
        uiLayout[3] = halfScreenWidth - buttonDim2;
        uiLayout[4] = buttonDim;
        uiLayout[5] = 0;
        uiLayout[6] = height;
        uiLayout[7] = height - buttonDim;
        uiLayout[8] = halfScreenHeight + buttonDim2;
        uiLayout[9] = halfScreenHeight - buttonDim2;
        uiLayout[10] = buttonDim;
        uiLayout[11] = 0;

        uiButton = new Button[2];
        uiButton[0] = new Button(asset[1],
                uiLayout[3],
                uiLayout[7],
                uiLayout[3],
                uiLayout[2],
                uiLayout[7],
                uiLayout[6],
                "plock");

        uiButton[1] = new Button(asset[2],
                uiLayout[3],
                uiLayout[11],
                uiLayout[3],
                uiLayout[2],
                uiLayout[11],
                uiLayout[10],
                "plock");

        /*
        uiButton[2] = new Button(asset[2],
                uiLayout[1],
                uiLayout[9],
                uiLayout[1],
                uiLayout[0],
                uiLayout[9],
                uiLayout[8],
                "plock");

        uiButton[3] = new Button(asset[3],
                uiLayout[5],
                uiLayout[9],
                uiLayout[5],
                uiLayout[4],
                uiLayout[9],
                uiLayout[8],
                "plock");
        */

        buttonSet = new UIButtonSet[1];
        buttonSet[0] = new UIButtonSet(width, height, uiButton, 0, "btnRotation",
                gameManager, rubix, null);

        userInterface = new UserInterface(buttonSet, width, height);
        userInterface.render();
        return (userInterface);
    }

    public Surface getSurface()
    {
        return (holder.getSurface());
    }

    private void drawSkymap(int[] surface, int color)
    {
        int         x;
        int         y;

        y = -1;
        while (++y < clipY_start) {
            x = -1;
            while (++x < width) {
                Toolbox.putPixel(surface, x, y, width, height, color);
            }
        }
        while (y < clipY_stop)
        {
            x = -1;
            while (++x < clipX_start) {
                Toolbox.putPixel(surface, x, y, width, height, color);
            }
            x = clipX_stop;
            while (++x < width) {
                Toolbox.putPixel(surface, x, y, width, height, color);
            }
            y++;
        }
        while (y < height)
        {
            x = -1;
            while (++x < width) {
                Toolbox.putPixel(surface, x, y, width, height, color);
            }
            y++;
        }
    }

    private int         getOrientationColor(boolean reverse, float[] groundDirection)
    {
        float dotFront;
        float dotDown;
        int smallValue;

        int colorGround;
        int colorHorizon;
        int colorSky;

        colorGround = Toolbox.getColor(0, 255, 0);
        colorHorizon = Toolbox.getColor(100, 255, 100);
        colorSky = Toolbox.getColor(0, 255, 255);
        smallValue = 0; // Todo Small value!
        dotFront = Toolbox.dotProd(tool.axis.front, groundDirection);
        dotDown = Toolbox.dotProd(tool.axis.up, groundDirection);
        return (getColor((int) (255f * Math.abs(dotDown)), 0, 0));
    }

    private void addMarker(int[] surface, int[] a, int[] b, Tileset16 coloredTileset, int value)
    {
        tmpVec2[0] = a[0] + ((b[0] - a[0]) / 2) - 8;
        tmpVec2[1] = a[1] + ((b[1] - a[1]) / 2) - 8;
        switch (value)
        {
            case 0: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "0"); break;
            case 1: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "1"); break;
            case 2: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "2"); break;
            case 3: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "3"); break;
            case 4: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "4"); break;
            case 5: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "5"); break;
            case 6: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "6"); break;
            case 7: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "7"); break;
            case 8: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "8"); break;
            case 9: coloredTileset.printVertical(surface, tmpVec2[0], tmpVec2[1], width, height, "9"); break;
            default: coloredTileset.print(surface, tmpVec2[0], tmpVec2[1], width, height, " "); break;
        }
    }

    private float       colorIntensity(float diffuse)
    {
        return (Math.min(diffuse + getAmbiantLight(), 1));
    }

    private float       colorIntensity(float diffuse, float sunExposition)
    {
        /*
        if (sunExposition < 0f)
            return (diffuse * 0.6f + getAmbiantLight());
        return (diffuse * 0.6f * ((1 + sunExposition * 1.4f) + getAmbiantLight()));
        */
        return (diffuse * 0.6f + getAmbiantLight());
    }

    private float       getAmbiantLight()
    {
        if (1 == 1)
            return (0.1f);
        ambiantLight = Math.min(comp.getLux(), 100f) / 200; // TODO
        return (ambiantLight);
    }

    private void drawFaceWireframe(int[] surface, int[] point, int offset, int[] palette, int face, float diffuse)
    {
        int faceOffset;
        int color;

        faceOffset = face * 9;
        if (palette != null)
        {
            tmpa[0] = point[offset + (2 * 0)];
            tmpa[1] = point[offset + (2 * 0) + 1];
            tmpb[0] = point[offset + (2 * 5)];
            tmpb[1] = point[offset + (2 * 5) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 0]], 0);

            tmpa[0] = point[offset + (2 * 1)];
            tmpa[1] = point[offset + (2 * 1) + 1];
            tmpb[0] = point[offset + (2 * 6)];
            tmpb[1] = point[offset + (2 * 6) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 1]], 1);

            tmpa[0] = point[offset + (2 * 2)];
            tmpa[1] = point[offset + (2 * 2) + 1];
            tmpb[0] = point[offset + (2 * 7)];
            tmpb[1] = point[offset + (2 * 7) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 2]], 2);

            tmpa[0] = point[offset + (2 * 4)];
            tmpa[1] = point[offset + (2 * 4) + 1];
            tmpb[0] = point[offset + (2 * 9)];
            tmpb[1] = point[offset + (2 * 9) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 3]], 3);

            tmpa[0] = point[offset + (2 * 5)];
            tmpa[1] = point[offset + (2 * 5) + 1];
            tmpb[0] = point[offset + (2 * 10)];
            tmpb[1] = point[offset + (2 * 10) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 4]], 4);

            tmpa[0] = point[offset + (2 * 6)];
            tmpa[1] = point[offset + (2 * 6) + 1];
            tmpb[0] = point[offset + (2 * 11)];
            tmpb[1] = point[offset + (2 * 11) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 5]], 5);

            tmpa[0] = point[offset + (2 * 8)];
            tmpa[1] = point[offset + (2 * 8) + 1];
            tmpb[0] = point[offset + (2 * 13)];
            tmpb[1] = point[offset + (2 * 13) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 6]], 6);

            tmpa[0] = point[offset + (2 * 9)];
            tmpa[1] = point[offset + (2 * 9) + 1];
            tmpb[0] = point[offset + (2 * 14)];
            tmpb[1] = point[offset + (2 * 14) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 7]], 7);

            tmpa[0] = point[offset + (2 * 10)];
            tmpa[1] = point[offset + (2 * 10) + 1];
            tmpb[0] = point[offset + (2 * 15)];
            tmpb[1] = point[offset + (2 * 15) + 1];
            addMarker(surface, tmpa, tmpb, rubix.tile[rubix.status[faceOffset + 8]], 8);

            color = palette[face];
        }
        else
            color = mulColor(rubix.delimiterColor, colorIntensity(diffuse));
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1], point[offset + (2 * 3)], point[offset + (2 * 3) + 1]); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1], point[offset + (2 * 12)], point[offset + (2 * 12) + 1]);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 3)], point[offset + (2 * 3) + 1], point[offset + (2 * 15)], point[offset + (2 * 15) + 1]); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 12)], point[offset + (2 * 12) + 1], point[offset + (2 * 15)], point[offset + (2 * 15) + 1]); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 1)], point[offset + (2 * 1) + 1], point[offset + (2 * 13)], point[offset + (2 * 13) + 1]);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 2)], point[offset + (2 * 2) + 1], point[offset + (2 * 14)], point[offset + (2 * 14) + 1]);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1], point[offset + (2 * 7)], point[offset + (2 * 7) + 1]);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 8)], point[offset + (2 * 8) + 1], point[offset + (2 * 11)], point[offset + (2 * 11) + 1]);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1], point[offset + (2 * 5)], point[offset + (2 * 5) + 1]);
    }

    public  void drawFaceRasterized(int[] surface, int[] point, int offset, float diffuse, float specular, int mask, boolean mixed, boolean odd, int margin, int face, boolean isSelected)
    {
        int numberCores = 4; // TODO DEBUG


        for (int id = 0; id < numberCores; id++)
            raster[id].setMarginEffect(isSelected);
        for (int id = 0; id < numberCores; id++)
        {
            raster[id].setSurface(surface, width, height);
            raster[id].loadVertices(point, offset, diffuse, specular, mask, mixed, odd, margin);
        }
        for (int id = 0; id < numberCores; id++)
            raster[id].render();
        if (isSelected) // TODO Passe ca en multithread
            drawFaceWireframe(surface, point, offset, null, face, diffuse);
        for (int id = 0; id < numberCores; id++)
            if (!raster[id].hasFinished())
                id--;
    }

    public void renderCube(int[] surface, int[] point, int[] palette)
    {
        boolean     marginMode;
        int         nearFace;
        int         margin;
        int         offset2d;
        float       culling;
        float       tolerance;

        getAmbiantLight();
        tolerance = renderFisheye ? 0.18f : 0f;
        offset2d = 0;
        nearFace = (int)rubix.culling[6];
        margin = marginWidth <= 0 ? 0 : 1;
        marginMode = selectionEffect % 2 == 0;
        for (int face = 0; face < 6; face++)
        {
            culling = rubix.culling[face];
            if (culling > tolerance)
            {
                if (renderWireframe)
                    drawFaceWireframe(surface, point, offset2d, palette, face, 0f);
                else
                {
                    //Log.e("DRAW_FACE #" + face, "Specular: " + rubix.specularFaceDot[face]); // TODO Debug
                    drawFaceRasterized(surface, point, offset2d,
                            culling, rubix.specularFaceDot[face * 9 + 4], 0xffffffff, // TODO ORIGINAL
                            //colorIntensity(culling), 1f, 0xffffffff, // TODO DEBUG
                            false, false,
                            (int) (culling * marginWidth) + margin,
                            face,
                            true
                            //selectionEffect > 3 || (marginMode && nearFace == face)
                            );
                }
            }
            offset2d += 32;
        }
        this.rubix.grabbed = nearFace;
        drawSelectionMarker(surface, point, nearFace); // Debug
        if (nearFace == -1 || renderWireframe || renderDebug || renderFisheye)
            return ;
        if (selectionEffect < 2 || selectionEffect == 4)
            drawSelectionMarker(surface, point, nearFace);
    }

    private void        drawSelectionMarker(int[] surface, int[] point, int faceIndex)
    {
        int color;
        int offset;

        offset = faceIndex * 32;
        color = rubix.complementaryColor(faceIndex);
        for (int lineWidth = 0; lineWidth < selectFrameWidth; lineWidth++)
        {
            drawLine(surface, width, height, color, point[offset + (2 * 0)] + lineWidth, point[offset + (2 * 0) + 1] + lineWidth, point[offset + (2 * 3)] + lineWidth, point[offset + (2 * 3) + 1] + lineWidth);
            drawLine(surface, width, height, color, point[offset + (2 * 0)] + lineWidth, point[offset + (2 * 0) + 1] + lineWidth, point[offset + (2 * 12)] + lineWidth, point[offset + (2 * 12) + 1] + lineWidth);
            drawLine(surface, width, height, color, point[offset + (2 * 3)] + lineWidth, point[offset + (2 * 3) + 1] + lineWidth, point[offset + (2 * 15)] + lineWidth, point[offset + (2 * 15) + 1] + lineWidth);
            drawLine(surface, width, height, color, point[offset + (2 * 12)] + lineWidth, point[offset + (2 * 12) + 1] + lineWidth, point[offset + (2 * 15)] + lineWidth, point[offset + (2 * 15) + 1] + lineWidth);
        }
    }

    public void         renderDebugText()
    {
        int line;

        line = 1;
        copyVec(tmp, comp.getDown());
        tileset_blue.printVertical(backbufferPixel, width,0, width, height, "Down");
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "X: " + tmp[0]);
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "Y: " + tmp[1]);
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "Z: " + tmp[2]);
        tileset_blue.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "Sun:");
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "X: " + rubix.sunPosition[0]);
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "Y: " + rubix.sunPosition[1]);
        tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "Z: " + rubix.sunPosition[2]);
        tileset_blue.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "SunDots:");
        for (int i = 0; i < 6; i++) { // TODO Debug Pas bon, ai change le layout
            tileset.printVertical(backbufferPixel, width - line++ * 16, 0, width, height, "#" + i + ": " + rubix.specularFaceDot[i]);
        }
    }
    public void         renderText(int clock)
    {
        if (false) // Debug
        {
            renderDebugText();
            drawAxis(backbufferPixel, scale);
        }
        if (clock == 0)
            timer = "0";
        else if (clock % 10 == 0)
            timer = "" + (gameManager.getScore() / 1000000000);
        rubix.tile[3].printVertical(backbufferPixel, 16, 0, width, height, "time: " + timer);
    }

    public void renderCube_anaglyph(int[] surface, int[] point, int[] palette, int xOffset)
    {
        boolean     displayMargin;
        boolean     marginMode;
        int         nearFace;
        int         margin;
        int         offset;
        int         offsetTmp;
        int         colorMask;
        float       culling;
        float       light;

        if (!renderWireframe)
        {
            for (int i = 0; i < point.length; i += 2) {
                tool.anaglyphPoint[i] = point[i];
                tool.anaglyphPoint[i + 1] = point[i + 1] + xOffset;
                point[i + 1] -= xOffset;
            }
        }
        //colorMask = xOffset < 0 ? 0xffff0000 : 0xff0000ff; // Todo Original
        getAmbiantLight();
        offset = 0;
        nearFace = (int)rubix.culling[6];
        margin = marginWidth <= 0 ? 0 : 1;
        marginMode = selectionEffect % 2 == 0;
        for (int face = 0; face < 6; face++)
        {
            culling = rubix.culling[face];
            if (culling > 0)
            {
                if (renderWireframe) // TODO Rasterized Anaglyph mode
                {
                    drawFaceWireframe_anaglyph(surface, point, offset, palette, face, xOffset);
                    drawFaceWireframe_anaglyph(surface, point, offset, palette, face, xOffset * -1);
                }
                else
                {
                    displayMargin = selectionEffect > 3 || (marginMode && nearFace == face);
                    //light = colorIntensity(culling, rubix.specularFaceDot[face]);
                    light = colorIntensity(culling); // TODO DEBUG ANAGLYPH
                    drawFaceRasterized(surface, point, offset, light, rubix.specularFaceDot[face], 0xffff0000, true, false, (int)(culling * marginWidth) + margin, face, displayMargin);
                    drawFaceRasterized(surface, tool.anaglyphPoint, offset, light, rubix.specularFaceDot[face], 0xff0000ff, true, true, (int)(culling * marginWidth) + margin, face, displayMargin);
                }
            }
            offset += 32;
        }
        this.rubix.grabbed = nearFace;
        if (nearFace == -1 || renderWireframe || renderDebug)
            return ;
        offset = nearFace * 32;
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] + xOffset);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] + xOffset);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset);

        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 12)] + 1, point[offset + (2 * 12) + 1] + xOffset + 1, point[offset + (2 * 13)] + 1, point[offset + (2 * 13) + 1] + xOffset + 1);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 12)] + 1, point[offset + (2 * 12) + 1] + xOffset + 1, point[offset + (2 * 14)] + 1, point[offset + (2 * 14) + 1] + xOffset + 1);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 13)] + 1, point[offset + (2 * 13) + 1] + xOffset + 1, point[offset + (2 * 15)] + 1, point[offset + (2 * 15) + 1] + xOffset + 1);
        drawLine(surface, width, height, 0xff000088, point[offset + (2 * 14)] + 1, point[offset + (2 * 14) + 1] + xOffset + 1, point[offset + (2 * 15)] + 1, point[offset + (2 * 15) + 1] + xOffset + 1);



        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] - xOffset, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] - xOffset);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] - xOffset, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] - xOffset);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] - xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] - xOffset);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] - xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] - xOffset);

        offsetTmp = xOffset - 1;
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 12)] + 1, point[offset + (2 * 12) + 1] - offsetTmp, point[offset + (2 * 13)] + 1, point[offset + (2 * 13) + 1] - offsetTmp);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 12)] + 1, point[offset + (2 * 12) + 1] - offsetTmp, point[offset + (2 * 14)] + 1, point[offset + (2 * 14) + 1] - offsetTmp);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 13)] + 1, point[offset + (2 * 13) + 1] - offsetTmp, point[offset + (2 * 15)] + 1, point[offset + (2 * 15) + 1] - offsetTmp);
        drawLine(surface, width, height, 0xff880000, point[offset + (2 * 14)] + 1, point[offset + (2 * 14) + 1] - offsetTmp, point[offset + (2 * 15)] + 1, point[offset + (2 * 15) + 1] - offsetTmp);
    }

    private void addMarker_colorMask(int[] surface, int[] a, int[] b, Tileset16 coloredTileset, int mask)
    {
        tmpVec2[0] = a[0] + ((b[0] - a[0]) / 2) - 8;
        tmpVec2[1] = a[1] + ((b[1] - a[1]) / 2) - 8;
        coloredTileset.print(surface, tmpVec2[0], tmpVec2[1], width, height, " ", mask);
    }

    public void drawFaceWireframe_anaglyph(int[] surface, int[] point, int offset, int[] palette, int face, int xOffset)
    {
        int         colorMask;
        int         faceOffset;
        int         color;

        colorMask = xOffset < 0 ? 0xffff0000 : 0xff0000ff;
        color = palette[face] & colorMask;

        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1] + xOffset, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1] + xOffset, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 1)], point[offset + (2 * 1) + 1] + xOffset, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 2)], point[offset + (2 * 2) + 1] + xOffset, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset, point[offset + (2 * 7)], point[offset + (2 * 7) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 8)], point[offset + (2 * 8) + 1] + xOffset, point[offset + (2 * 11)], point[offset + (2 * 11) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset, point[offset + (2 * 5)], point[offset + (2 * 5) + 1] + xOffset);


        faceOffset = face * 9;
        tmpa[0] = point[offset + (2 * 0)];
        tmpa[1] = point[offset + (2 * 0) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 5)];
        tmpb[1] = point[offset + (2 * 5) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 0]);

        tmpa[0] = point[offset + (2 * 1)];
        tmpa[1] = point[offset + (2 * 1) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 6)];
        tmpb[1] = point[offset + (2 * 6) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 1]);

        tmpa[0] = point[offset + (2 * 2)];
        tmpa[1] = point[offset + (2 * 2) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 7)];
        tmpb[1] = point[offset + (2 * 7) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 2]);

        tmpa[0] = point[offset + (2 * 4)];
        tmpa[1] = point[offset + (2 * 4) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 9)];
        tmpb[1] = point[offset + (2 * 9) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 3]);

        tmpa[0] = point[offset + (2 * 5)];
        tmpa[1] = point[offset + (2 * 5) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 10)];
        tmpb[1] = point[offset + (2 * 10) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 4]);

        tmpa[0] = point[offset + (2 * 6)];
        tmpa[1] = point[offset + (2 * 6) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 11)];
        tmpb[1] = point[offset + (2 * 11) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 5]);

        tmpa[0] = point[offset + (2 * 8)];
        tmpa[1] = point[offset + (2 * 8) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 13)];
        tmpb[1] = point[offset + (2 * 13) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 6]);

        tmpa[0] = point[offset + (2 * 9)];
        tmpa[1] = point[offset + (2 * 9) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 14)];
        tmpb[1] = point[offset + (2 * 14) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 7]);

        tmpa[0] = point[offset + (2 * 10)];
        tmpa[1] = point[offset + (2 * 10) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 15)];
        tmpb[1] = point[offset + (2 * 15) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 8]);

        if (1 == 1) // TODO --------- Anaglyph
            return ;

        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset, point[offset + (2 * 0)], point[offset + (2 * 0) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1] + xOffset, point[offset + (2 * 1)], point[offset + (2 * 1) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 1)], point[offset + (2 * 1) + 1] + xOffset, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 12)], point[offset + (2 * 12) + 1] + xOffset, point[offset + (2 * 2)], point[offset + (2 * 2) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 0)], point[offset + (2 * 0) + 1] + xOffset, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 1)], point[offset + (2 * 1) + 1] + xOffset, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 2)], point[offset + (2 * 2) + 1] + xOffset, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset, point[offset + (2 * 5)], point[offset + (2 * 5) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 2)], point[offset + (2 * 2) + 1] + xOffset, point[offset + (2 * 6)], point[offset + (2 * 6) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 3)], point[offset + (2 * 3) + 1] + xOffset, point[offset + (2 * 7)], point[offset + (2 * 7) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 4)], point[offset + (2 * 4) + 1] + xOffset, point[offset + (2 * 8)], point[offset + (2 * 8) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 6)], point[offset + (2 * 6) + 1] + xOffset, point[offset + (2 * 7)], point[offset + (2 * 7) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 7)], point[offset + (2 * 7) + 1] + xOffset, point[offset + (2 * 8)], point[offset + (2 * 8) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 8)], point[offset + (2 * 8) + 1] + xOffset, point[offset + (2 * 9)], point[offset + (2 * 9) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 6)], point[offset + (2 * 6) + 1] + xOffset, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] + xOffset); // CORNER
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 7)], point[offset + (2 * 7) + 1] + xOffset, point[offset + (2 * 10)], point[offset + (2 * 10) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 8)], point[offset + (2 * 8) + 1] + xOffset, point[offset + (2 * 11)], point[offset + (2 * 11) + 1] + xOffset);

        ///

        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 13)], point[offset + (2 * 13) + 1] + xOffset, point[offset + (2 * 5)], point[offset + (2 * 5) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 5)], point[offset + (2 * 5) + 1] + xOffset, point[offset + (2 * 9)], point[offset + (2 * 9) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 9)], point[offset + (2 * 9) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 14)], point[offset + (2 * 14) + 1] + xOffset, point[offset + (2 * 10)], point[offset + (2 * 10) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 10)], point[offset + (2 * 10) + 1] + xOffset, point[offset + (2 * 11)], point[offset + (2 * 11) + 1] + xOffset);
        Toolbox.drawLine(surface, width, height, color, point[offset + (2 * 11)], point[offset + (2 * 11) + 1] + xOffset, point[offset + (2 * 15)], point[offset + (2 * 15) + 1] + xOffset);

        faceOffset = face * 9;
        tmpa[0] = point[offset + (2 * 12)];
        tmpa[1] = point[offset + (2 * 12) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 3)];
        tmpb[1] = point[offset + (2 * 3) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 0]);

        tmpa[0] = point[offset + (2 * 0)];
        tmpa[1] = point[offset + (2 * 0) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 4)];
        tmpb[1] = point[offset + (2 * 4) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 1]);

        tmpa[0] = point[offset + (2 * 1)];
        tmpa[1] = point[offset + (2 * 1) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 5)];
        tmpb[1] = point[offset + (2 * 5) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 2]);

        tmpa[0] = point[offset + (2 * 2)];
        tmpa[1] = point[offset + (2 * 2) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 7)];
        tmpb[1] = point[offset + (2 * 7) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 3]);

        tmpa[0] = point[offset + (2 * 3)];
        tmpa[1] = point[offset + (2 * 3) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 8)];
        tmpb[1] = point[offset + (2 * 8) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 4]);

        tmpa[0] = point[offset + (2 * 4)];
        tmpa[1] = point[offset + (2 * 4) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 9)];
        tmpb[1] = point[offset + (2 * 9) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 5]);

        tmpa[0] = point[offset + (2 * 6)];
        tmpa[1] = point[offset + (2 * 6) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 10)];
        tmpb[1] = point[offset + (2 * 10) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 6]);

        tmpa[0] = point[offset + (2 * 7)];
        tmpa[1] = point[offset + (2 * 7) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 11)];
        tmpb[1] = point[offset + (2 * 11) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 7]);

        tmpa[0] = point[offset + (2 * 8)];
        tmpa[1] = point[offset + (2 * 8) + 1] + xOffset;
        tmpb[0] = point[offset + (2 * 15)];
        tmpb[1] = point[offset + (2 * 15) + 1] + xOffset;
        addMarker(surface, tmpa, tmpb, xOffset < 0 ? tileset : tileset_blue, rubix.status[faceOffset + 8]);
    }

    private void        drawAxis(int[] surface, float scale)
    {
        float swap;
        int color;

        for (int face = 0; face < 6; face++)
        {
            switch (face) {
                case 0:
                    refAxis = comp.getFront();
                    color = Color.BLUE;
                    break; // TODO Mettre au clair les differents axes....
                case 1:
                    refAxis = comp.getUp();
                    color = Color.GREEN;
                    break;
                case 2:
                    refAxis = comp.getBack();
                    color = Color.BLUE;
                    break;
                case 3:
                    refAxis = comp.getDown();
                    color = Color.GREEN;
                    break;
                case 4:
                    refAxis = comp.getLeft();
                    color = Color.RED;
                    break;
                case 5:
                    refAxis = comp.getRight();
                    color = Color.RED;
                    break;
                default:
                    refAxis = comp.getDown();
                    color = Color.LTGRAY;
                    break; // ...
            }
            Toolbox.copyVec(tmp, refAxis); // TODO Remettre
            swap = tmp[1];
            tmp[1] = -  tmp[0];
            tmp[0] = swap * -1;
            Toolbox.mulVector(tmp, scale * 2.5f);
            drawLine(surface, width, height, color,
                    halfScreenWidth, halfScreenHeight,
                    halfScreenWidth + (int)tmp[0] * -1, halfScreenHeight + (int)tmp[1]);
        }

    }

    public int[] perspectiveCavaliere(float[] vert)
    {
        float x;
        float y;
        int sz;
        int i;
        int j;

        sz = vert.length;
        i = 0;
        j = 0;
        while (i < sz)
        {
            x = vert[i++];
            y = vert[i++];
            i++;
            tmpVec2[1] = (int)(-x + halfScreenHeight);
            tmpVec2[0] = (int)(y + halfScreenWidth);
            tool.point[j++] = tmpVec2[0];
            tool.point[j++] = tmpVec2[1];
        }
        return (tool.point);
    }

    public int[] perspectiveCavaliere_z(float[] vert)
    {
        float x;
        float y;
        float z;
        float dist;
        int sz;
        int i;
        int j;

        sz = vert.length;
        i = 0;
        j = 0;
        while (i < sz)
        {
            x = vert[i++];
            y = vert[i++];
            z = vert[i++];
            // TODO --------------------------------
            //dist = ((z / scale) - 1f) * 1.5;
            //dist = (z - 1.8f) * scale * 0.5f;
            dist = (z * 0.5f - 1.8f) * scale * 0.5f;
            tmpVec2[1] = (int)(-x * dist + halfScreenHeight);
            tmpVec2[0] = (int)(-y * dist + halfScreenWidth);
            /*
            tmpVec2[1] = (int)(x + halfScreenHeight);
            tmpVec2[0] = (int)(y + halfScreenWidth);
            */
            //Log.e("DIST", "Dist: " + dist + " Z:["+z+"] Scale:"+scale); // TODO DEBUG -------------------------
            tool.point[j++] = tmpVec2[0];
            tool.point[j++] = tmpVec2[1];
        }
        return (tool.point);
    }

    public boolean renderSplashScreen(int clock, int state, int steps, Tileset16[] printer, String splash)
    {
        int font;
        boolean bool;

        if (locked)
            return (false);
        canvas = holder.lockCanvas();
        if (canvas == null)
            return (false);
        locked = true;

        font = 1;
        clock *= steps;
        if (clock < halfScreenWidth)
        {
            font = 0;
            bool = false;
        }
        else
        {
            clock = halfScreenWidth;
            bool = state == 0;
        }
        backbuffer.eraseColor(font == 0 ? Color.WHITE : Color.BLACK);
        backbuffer.getPixels(backbufferPixel, 0, width, 0, 0, width, height);
        printer[font].printVertical(backbufferPixel, width - clock, halfScreenHeight - ((splash.length() / 2) * 16),
                width, height, splash); // Debug
        backbuffer.setPixels(backbufferPixel, 0, width, 0, 0, width, height);
        canvas.drawBitmap(backbuffer, 0, 0, null);
        holder.unlockCanvasAndPost(canvas);
        locked = false;
        return (bool);
    }

    public float renderSplashScreen_functionnal(float x, Tileset16[] printer, String splash)
    {
        float xTmp;
        float exp;
        float y;
        float z;
        int verticalOffset;
        int font;
        boolean bool;

        if (x < 0f)
            x = Math.abs(x);
        z = x * x * x * x * x * x * x * x * x * x * x * x;
        if (z < 0.05) {
            while (locked)
                ;
            canvas = holder.lockCanvas();
            if (canvas == null)
                return (-1);
            locked = true;

            if (x < 0.3f)
            {
                backbuffer.eraseColor(Color.WHITE);
                font = 0;
                z = -1;
            }
            else
            {
                backbuffer.eraseColor(Color.BLACK);
                font = 1;
                z = 2f;
            }
            exp = (x - 0.3f) * -3f;
            xTmp = exp * exp * exp;
            y = xTmp + 0.5f;
            verticalOffset = (int) (y * (float) width);
            if (x <= 0.8 && verticalOffset < width - 16 && verticalOffset > 16) {
                backbuffer.getPixels(backbufferPixel, 0, width, 0, 0, width, height);
                printer[font].printVertical(backbufferPixel, verticalOffset, halfScreenHeight - ((splash.length() / 2) * 16),
                        width, height, splash); // Debug
                backbuffer.setPixels(backbufferPixel, 0, width, 0, 0, width, height);
            }
            canvas.drawBitmap(backbuffer, 0, 0, null);
            holder.unlockCanvasAndPost(canvas);
            locked = false;
        }
        return (z);
    }

    public void setScale(float factor)
    {
        scale = factor * baseScale;
    }
    public void resetScale()
    {
        scale = baseScale;
    }

    public void renderWin(int clock)
    {
        clock = (clock >> 3) % 6;
        rubix.tile[clock].printVertical(backbufferPixel, halfScreenWidth - 8, halfScreenHeight - (15 * 8), width, height, "               ");
        rubix.tile[clock].printVertical(backbufferPixel, halfScreenWidth + 8, halfScreenHeight - (15 * 8), width, height, "  S O L V E D  ");
        rubix.tile[clock].printVertical(backbufferPixel, halfScreenWidth + 24, halfScreenHeight - (15 * 8), width, height, "               ");
    }

    public void renderMenger(int clock)
    {
        float upLeftCorner[] = new float[2];
        float downRightCorner[] = new float[2];
        upLeftCorner[0] = 0;
        upLeftCorner[1] = 0;
        downRightCorner[0] = width-1;
        downRightCorner[1] = height-1;
        AxisVectors axisVectors = new AxisVectors();
        axisVectors.front[0] = comp.getFront()[0];
        axisVectors.front[1] = comp.getFront()[1];
        axisVectors.front[2] = comp.getFront()[2];
        axisVectors.left[0] = comp.getLeft()[0];
        axisVectors.left[1] = comp.getLeft()[1];
        axisVectors.left[2] = comp.getLeft()[2];
        axisVectors.up[0] = comp.getUp()[0];
        axisVectors.up[1] = comp.getUp()[1];
        axisVectors.up[2] = comp.getUp()[2];
        axisVectors.down[0] = comp.getDown()[0];
        axisVectors.down[1] = comp.getDown()[1];
        axisVectors.down[2] = comp.getDown()[2];
        for (int i = 0; i < 3; i++)
            mengerSponge[i].render(
                    upLeftCorner, upLeftCorner,
                    downRightCorner, downRightCorner, axisVectors, width / 6, i);
    }
    public void renderFrame(int clock)
    {
        Canvas      canvas;
        int[]       point;
        boolean     side;

        //System.out.println("BATMAN :: Viewport :: renderFrame :: lock : " + locked);
        if (locked)
        {
            postCanvas();
            //return;
        }
        canvas = holder.lockCanvas();
        if (canvas == null)
            return ;
        locked = true;
        if (false && doNotRender)
            return ;

        /* Oriente le rubixcube */
        if (renderFisheye)
            point = perspectiveCavaliere_z(rubix.getVertices(comp, clock));
        else
            point = perspectiveCavaliere(rubix.getVertices(scale, comp, clock));
        /*
        backbuffer.eraseColor(
                Toolbox.mulColor(
                        getOrientationColor(true, comp.getAxlNormalized(this.tmp)),
                        comp.getLux() + 0.3f)); // 30% global illumination
        */
        /* Nettoie l'ecran */
        backbuffer.eraseColor(Color.BLACK);
        backbuffer.getPixels(backbufferPixel, 0, width, 0, 0, width, height);
        /* Orientation */
        side = comp.getPortraitSide();
        if (side != this.side)
        {
            this.side = side;
            System.gc();
            setRenderMode(side, true);
        }
        //System.out.println("BATMAN :: Viewport :: renderFrame :: Start Drawing"); // Debug
        /* Dessine le Rubixcube */
        /**
        if (renderAnaglyph) // Todo :: Bug !!!!!!!!!!
            renderCube_anaglyph(backbufferPixel, point, rubix.palette, anaglyphDepth);
        else
            renderCube(backbufferPixel, point, rubix.palette);
        */
        renderMenger(clock);

        /* Dessine le texte gagne */
        if (gameManager.hasWon())
            renderWin(clock);
        /* Dessine les informations de debogage */
        if (true || renderWireframe) // Debug
            renderText(clock); // Debug
        /* Dessine l'UI de jeu */
        if (!renderWireframe && !gameManager.hasWon())
            userInterface.setPixels_ommitAlpha(backbufferPixel);
        //backbuffer.setPixels(userInterface.pixel, 0, width, 0, 0, width, height);
        backbuffer.setPixels(backbufferPixel, 0, width, 0, 0, width, height);
        canvas.drawBitmap(backbuffer, 0, 0, null);
        //System.out.println("BATMAN :: Viewport :: renderFrame :: Drawing :: F"); // Debug
        holder.unlockCanvasAndPost(canvas);
        //System.out.println("BATMAN :: Viewport :: renderFrame :: Drawing :: J"); // Debug
        locked = false;
        //System.out.println("BATMAN :: Viewport :: renderFrame :: End Drawing");
    }
}