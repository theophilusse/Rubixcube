package com.rubix.rubix;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.rubix.rubix.Sound;

public class SoundManager {
    private Sound[]        sound;
    private Context        context;

    public SoundManager(Context context, int[] soundFile, String[] name)
    {
        this.context = context;
        for (int i = 0; i < name.length; i++) // Debug
            System.out.println("BATMAN :: SoundManager :: name["+i+"] = '"+name[i]+"'"); // Debug
        this.sound = initSound(soundFile, name);
    }

    private Sound[]       initSound(int[] file, String[] name)
    {
        Sound[]             sound;
        MediaPlayer[]       mp;

        mp = new MediaPlayer[file.length];
        try {
            for (int i = 0; i < file.length; i++)
                mp[i] = loadSound(file[i]);
        }
        catch (Exception e) {
            Log.e("INITSOUND", "Game corrupted");
        }
        sound = new Sound[file.length];
        if (name == null)
            for (int i = 0; i < file.length; i++)
                sound[i] = new Sound(mp[i]);
        else
        {
            System.out.println("BATMAN :: SoundManager :: initSound name"); // Debug
            for (int i = 0; i < file.length; i++)
                sound[i] = new Sound(mp[i], i < name.length ? name[i] : null);
        }
        return (sound);
    }

    public int          addSound(int id, String name)
    {
        Sound[] aSound;
        MediaPlayer sound;
        int i;
        int len;

        if ((sound = loadSound(id)) == null)
            return (-1);
        len = this.sound.length;
        aSound = new Sound[len + 1];
        i = -1;
        while (++i < len)
            aSound[i] = this.sound[i];
        aSound[i] = new Sound(sound, name);
        this.sound = aSound;
        return (i);
    }

    private MediaPlayer loadSound(int id) //String filename)
    {
        AssetFileDescriptor afd;
        MediaPlayer             sound;

        try {
            afd = context.getResources().openRawResourceFd(id);
            sound = new MediaPlayer();
            sound.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            sound.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LOADSOUND", "Exception with fileId '" + id + "'");
            return (null);
        }
        return (sound);
    }

    public void play(int soundId)
    {
        if (soundId < 0 || soundId >= sound.length)
            return ;
        sound[soundId].play();
    }

    public void play(String soundName) {
        int soundId;

        if (soundName == null)
            return;
        soundId = -1;
        for (int i = 0; i < sound.length; i++)
        {
            System.out.println("BATMAN :: SoundManager :: play :: soundName["+i+"] '" + sound[i].getName() + "'"); // Debug
            if (sound[i].getName() == soundName) {
                soundId = i;
                break;
            }
        }
        if (soundId < 0)
            return ;
        sound[soundId].play();
    }
}
