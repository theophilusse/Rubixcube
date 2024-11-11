package com.rubix.rubix;

import android.app.Activity;
import android.os.Bundle;

public class SplashScreen extends Activity {
    @Override
    public void onCreate(Bundle instance)
    {
        super.onCreate(instance);
        try {
            Thread.sleep(2000);
        } catch (Exception e)
        {
            return ;
        }
        // Rien de special a faire a part afficher l'image
    }
}
