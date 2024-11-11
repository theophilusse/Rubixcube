package com.rubix.rubix;

import android.media.MediaPlayer;
import android.provider.MediaStore;

public class Sound {
    private MediaPlayer        sound;
    private String             name;

    public Sound(MediaPlayer sound, String name)
    {
        this.sound = sound;
        if (name == null)
            name = "";
        this.name = name;
    }

    public Sound(MediaPlayer sound)
    {
        this.sound = sound;
        name = "";
    }

    public String       getName()
    {
        return (name);
    }

    public String         rename(String name)
    {
        if (name == null)
            this.name = "";
        this.name = name;
        return (this.name);
    }

    public void         play()
    {
        if (sound == null)
            return ;
        sound.start();
    }

    public void stop()
    {
        if (sound == null)
            return ;
        sound.stop();
    }
}
