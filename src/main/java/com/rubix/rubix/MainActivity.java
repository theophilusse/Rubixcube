package com.rubix.rubix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.rubix.rubix.GameManager;
import com.rubix.rubix.InterplanetaryCompanion;
import com.rubix.rubix.Toolbox;
import com.rubix.rubix.Viewport;
import com.rubix.rubix.R;

/*
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
*/

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private int currentApiVersion;
    Boolean stop;
    GameManager game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("BATMAN :: Start application"); // Debug
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            System.out.println("BATMAN :: Application :: KitKat"); // Debug
            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                                decorView.setSystemUiVisibility(flags);
                        }
                    });
        }

        /// TODO ------------- SPLASH ACTIVITY
        /*
        //setContentView(R.layout.splash);
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(10000);  //Delay of 10 seconds
                } catch (Exception e) {

                } finally {
                    setContentView(R.layout.splash);
                    Intent i = new Intent(MainActivity.this,
                            MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
        */
        /// TODO ------------- SPLASH ACTIVITY

        System.out.println("BATMAN :: Application :: Initialize"); // Debug
        setContentView(R.layout.activity_main);

        advertisement();

        //FirebaseApp.initializeApp(this); // TODO Default FirebaseApp is not initialized in this process
        /// TODO Touchescreen ----------------
        stop = false;
        System.out.println("BATMAN :: Application :: Game Init"); // Debug
        game = initGame((Viewport) findViewById(R.id.viewport), false, false);
        try {
            System.out.println("BATMAN :: Application :: Game Link"); // Debug
            if (game.linkGame(this,
                    new InterplanetaryCompanion(this, (SensorManager) getSystemService(SENSOR_SERVICE))))
                throw new Exception("Game returned an error.");
        } catch (Exception e) {
            Log.e("ERR", "ERR runtime: [" + e.getMessage() + "]");
            Toast.makeText(this, "ERR runtime: [" + e.getMessage() + "]", Toast.LENGTH_SHORT);
            System.out.println("BATMAN :: Application :: Game Link :: ERROR"); // Debug
        }
        System.out.println("BATMAN :: Application :: Game Link :: OK"); // Debug
        Log.e("RUNNING", "TRY CATCH PASSED : r" + Toolbox.getRed(Color.WHITE) + " g" + Toolbox.getGreen(Color.WHITE) + " b" + Toolbox.getBlue(Color.WHITE)); // Debug
        game.setRunning(true);
        System.out.println("BATMAN :: Application :: Game Start !"); // Debug
        game.start();
    }

    private AdView mAdView;
    private void advertisement()
    {
        AdRequest adRequest;

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView_left);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView = findViewById(R.id.adView_right);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            game.setRunning(true);
        }
        else
            game.setRunning(false);
    }

    private int getNumberOfCores() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    public GameManager initGame(Viewport viewport, boolean renderDebug, boolean renderAnaglyph) {

        stop = false;
        if (viewport == null) {
            Log.e("FATAL ERROR", "Layout error");
            finish();
        }
        try {
            return (new GameManager((Activity) this,
                    viewport,
                    renderAnaglyph,
                    renderDebug,
                    /*getNumberOfCores()*/4));
        } catch (Exception e) {
            Log.e("ERR", "ERR init: [" + e.getMessage() + "]");
            Toast.makeText(this, "ERR init: [" + e.getMessage() + "]", Toast.LENGTH_SHORT);
            return (null);
        }
    }

    public Boolean isRunning(){
        return stop;
    }

    public void setRunning(Boolean bools)
    {
        stop = bools;
        if (game != null)
            game.setRunning(!stop);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setRunning(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRunning(true);
    }
}