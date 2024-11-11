package com.rubix.rubix;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.rubix.rubix.MainActivity;
import com.rubix.rubix.SoundManager;
import com.rubix.rubix.Viewport;
import com.rubix.rubix.R;

import java.util.concurrent.TimeUnit;

public class GameManager extends Thread {
    public Context              context;
    public Viewport             viewport;
    public SoundManager         soundManager;
    public Bitmap[]             bitmap;
    public long                 timerBase;
    public long                 timer;
    public long                 score;
    private boolean             hasMadeAMove;
    int                         availableCores;
    private boolean             win;
    private boolean             scoreSended;
    private boolean             running;
    private boolean             quit;

    public GameManager(Activity mainActivity, Viewport viewport,
                       boolean renderAnaglyph, boolean renderDebug, int numCore) throws Exception
    {
        Log.i("GM:CORE", "Build...");
        Toast.makeText((MainActivity)mainActivity, "Loading...", Toast.LENGTH_SHORT);
        sleep(1000);
        availableCores = numCore;
        this.context = mainActivity.getApplicationContext();
        this.viewport = viewport;
        running = false;
        quit = false;
        win = false;
        scoreSended = false;
        timerBase = 0;
        timer = 0;
        reset();
        this.soundManager = new SoundManager(context, soundList(), soundListName());
        this.bitmap = loadBitmapList();
        viewport.setRenderDebug(renderDebug);
        viewport.setRenderAnaglyph(renderAnaglyph);
        Log.i("GM:CORE", "Complete.");
    }

    public boolean isRunning()
    {
        return (running);
    }

    public void madeAMove()
    {
        hasMadeAMove = true;
    }

    public void reset()
    {
        hasMadeAMove = false;
        setWin(false);
    }

    public boolean hasMadeAMove()
    {
        if (!hasMadeAMove) // Testing
        {
            timerBase = System.nanoTime();
            timer = (System.nanoTime() - timerBase) + timer;
        }
        return (hasMadeAMove);
    }
    public long getScore()
    {
        if (!hasMadeAMove())
            return (0);
        if (hasWon())
            return (score);
        score = (System.nanoTime() - timerBase) + timer;
        return (score);
    }

    public void setWin(boolean win)
    {
        long score;

        score = getScore();
        if (win && !scoreSended)
        {
            timerBase = System.nanoTime();
            sendDatabase("win", ""+TimeUnit.NANOSECONDS.toMillis(score));
            scoreSended = true;
        }
        this.win = win;
    }

    private boolean        sendDatabase(String reference, String data)
    {
        /* // TODO
        FirebaseDatabase mFirebaseInstance; // TODO

        mFirebaseInstance = FirebaseDatabase.getInstance();
        if (mFirebaseInstance == null)
            return (true);
        mFirebaseInstance.getReference("win").push().setValue(data); // Todo Firebase
        */
        return (false);
    }

    public boolean hasWon()
    {
        return (win);
    }

    public void setRunning(boolean run)
    {
        Log.i("GM:STATUS", "Running: [" + run + "]");
        running = run;
        if (1 == 0 && viewport != null) // TODO Bug ici ?
            viewport.setRunning(run);
    }

    public boolean linkGame(MainActivity mainActivity,
                            InterplanetaryCompanion comp)
    {
        System.out.println("BATMAN :: GameManager :: Link"); // Debug
        try {
            /// DEBUG ENTRYPOINT
            System.out.println("LINK GAME IN"); // Debug removeme
            System.out.println("BATMAN :: GameManager :: Link :: Viewport"); // Debug
            viewport.initialize(this, mainActivity, comp, bitmap, availableCores);
            System.out.println("LINK GAME OUT"); // Debug removeme
        }
        catch (Exception e)
        {
            System.out.println("BATMAN :: GameManager :: Link :: ERROR"); // Debug
            Log.e("GM: FATAL ERROR", e.getMessage());
            return (true);
        }
        return (false);
    }

    public void shutdown() { quit = true; }

    public Bitmap[] loadBitmapList() throws Exception
    {
        int         numBitmap;
        Resources   res;
        Bitmap[]    bitmap;

        numBitmap = 5;
        bitmap = new Bitmap[numBitmap];
        for (int i = 0; i < numBitmap; i++)
            bitmap[i] = null;
        try {
            res = context.getResources();
            bitmap[0] = BitmapFactory.decodeResource(res, R.drawable.splash);

            bitmap[1] = BitmapFactory.decodeResource(res, R.drawable.left_bottom);
            bitmap[2] = BitmapFactory.decodeResource(res, R.drawable.right_bottom);
            bitmap[3] = BitmapFactory.decodeResource(res, R.drawable.bottom_right);
            bitmap[4] = BitmapFactory.decodeResource(res, R.drawable.up_right);
            for (int i = 0; i < numBitmap; i++)
                if (bitmap[i] == null)
                    throw new Exception("Bitmap #" + i + " is null");
        } catch (Exception e)
        {
            Log.e("BITMAPLIST", "Fatal error. [" + e.getMessage() + "]");
            throw new Exception(" ");
        }
        return (bitmap);
    }

    public static Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private int[]    soundList()
    {
        int[]               soundFiles;

        soundFiles = new int[5];
        for (int i = 0; i < 5; i++)
            soundFiles[i] = 0;
        soundFiles[0] = R.raw.boot;
        soundFiles[1] = R.raw.clock;
        soundFiles[2] = R.raw.click;
        soundFiles[3] = R.raw.plock;
        soundFiles[4] = R.raw.tada;
        return (soundFiles);
    }

    private String[]    soundListName()
    {
        String[]               soundName;

        soundName = new String[5];
        for (int i = 0; i < 5; i++)
            soundName[i] = null;
        soundName[0] = "boot";
        soundName[1] = "clock";
        soundName[2] = "click";
        soundName[3] = "plock";
        soundName[4] = "tada";
        return (soundName);
    }

    private String getStringByName(String idName)
    {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(idName, "string", context.getPackageName()));
    }

    @Override
    public void run()
    {
        SurfaceHolder holder;
        int             clock;

        System.out.println("BATMAN :: GameManager :: Running :: Splash"); // Debug
        splashScreen_functionnal(getStringByName("app_name") + " [ALPHA]", 4);
        System.out.println("BATMAN :: GameManager :: Running :: End Splash"); // Debug
        System.gc(); // Il a free, il a tout compris
        clock = 0;
        running = true;
        System.out.println("BATMAN :: GameManager :: Looping"); // Debug
        if (viewport.isLocked())
            viewport.postCanvas();
        while (true)
        {
            if (quit)
                break;
            if (running) {
                timerBase = System.nanoTime();
                while (running) {
                    try { // Thread ui
                        while (!viewport.getSurface().isValid())
                            ;
                        synchronized (viewport.getHolder()) {
                            viewport.renderFrame(clock++);
                        }
                    } catch (Exception e) {
                        Log.e("GM:CRASH", e.getCause() + " ::: " + e.getMessage() + " ::: " + e.getLocalizedMessage());
                        return;
                    }
                }
                Log.i("GM:ENGINE", "Paused.");
                timer = (System.nanoTime() - timerBase) + timer;
            }
        }
        System.out.println("BATMAN :: GameManager :: Stop"); // Debug
        Log.i("GM:ENGINE", "Shutdown.");
    }

    public void splashScreen(String text, int speed, int coolDown) {
        Tileset16[] bootFont;
        boolean blip;
        int     state;
        int     width;
        int     clock;
        int     animationLength;
        int     verticalSteps;

        bootFont = new Tileset16[2];
        bootFont[0] = new Tileset16(Color.WHITE, Color.BLACK);
        bootFont[1] = new Tileset16(Color.BLACK, Color.WHITE);
        width = viewport.getWidth();
        verticalSteps = speed < 1 ? 1 : speed;
        animationLength = (width / verticalSteps) + coolDown;
        clock = 0;
        state = 0;
        while (true)
        {
            try { // Thread ui
                while (!viewport.getSurface().isValid())
                    ;
                synchronized (viewport.getHolder()) {
                    blip = viewport.renderSplashScreen(clock++, state, verticalSteps, bootFont, text);
                }
                if (blip)
                {
                    soundManager.play(0);
                    sleep(1200);
                }
                else if (clock >= animationLength)
                    break;
            } catch (Exception e) {
                Log.e("GM:CRASH", e.getCause() + " ::: " + e.getMessage() + " ::: " + e.getLocalizedMessage());
                return;
            }
        }
    }

    public void splashScreen_functionnal(String text, int lengthSeconds)
    {
        Tileset16[] bootFont;
        boolean bloped;
        float blip;
        float x;
        float xDelta;
        int framePerSeconds = 6;
        int frames;

        frames = lengthSeconds * framePerSeconds;
        if (frames == 0)
            return ;
        bloped = false;
        bootFont = new Tileset16[2];
        bootFont[0] = new Tileset16(Color.BLACK, Color.WHITE);
        bootFont[1] = new Tileset16(Color.WHITE, Color.BLACK);
        x = 0f;
        // TODO xDelta = 1f / frames;
        //xDelta = 1f / 330;
        xDelta = 0.009f;
        while (x <= 1f)
        {
            try { // Thread ui
                while (!viewport.getSurface().isValid())
                    ;
                synchronized (viewport.getHolder()) {
                    blip = viewport.renderSplashScreen_functionnal(x, bootFont, text);
                }
                if (blip > 0.05f && blip < 1f)
                {
                    viewport.setScale(blip);
                    while (!viewport.getSurface().isValid())
                        ;
                    synchronized (viewport.getHolder()) {
                        viewport.renderFrame(0);
                    }
                }
                if (blip < 0f)
                {
                    bloped = false;
                }
                if (blip > 0f && !bloped)
                {
                    soundManager.play(0);
                    bloped = true;
                }
            } catch (Exception e) {
                System.out.println("BATMAN :: GameManager :: SplashScreen :: CRASH"); // Debug
                Log.e("GM:CRASH", e.getCause() + " ::: " + e.getMessage() + " ::: " + e.getLocalizedMessage());
                return;
            }
            x += xDelta;
        }
        viewport.resetScale();
    }
}