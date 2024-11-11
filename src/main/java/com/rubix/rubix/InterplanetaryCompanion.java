package com.rubix.rubix;

import static com.rubix.rubix.Toolbox.*;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Calendar;

public class InterplanetaryCompanion implements SensorEventListener {
    private Context context;
    private Toolbox tool;
    private float[] swap;
    private float[] tmp;
    private float[] tmpx_;
    private float[] tmpy_;
    private float[] tmpz_;
    private float[] tmpx;
    private float[] tmpy;
    private float[] tmpz;

    private SensorManager sensorManager;
    private Sensor rotationVector;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetic;
    private Sensor light;
    private float[] axl;
    private float[] axis_x;
    private float[] axis_y;
    private float[] axis_z;
    private float[] orientation;
    private float[] rotationMatrix;
    private float[] rot_x;
    private float[] rot_y;
    private float[] rot_z;
    private float[] mag;
    private float[] gyr;
    private float lux;
    private float lightMaximumRange;

    private float[] up;
    private float[] down;
    private float[] right;
    private float[] left;
    private float[] back;
    private float[] front;


    private float[] ground;
    private float[] north;
    private float[] pol;

    private double delta;

    private boolean side;
    private boolean sensorMode_RotationVector;
    private boolean pointerDown;
    private float pointerDown_position[];
    private float pointerDown_directionVector[];
    private float pointerDown_baseOrientation[] = new float[3];

    public InterplanetaryCompanion(Context context, SensorManager sensorManager) throws Exception {
        System.out.println("NEW INTERPLANETARYCOMP IN"); // Debug removeme
        this.context = context;
        this.sensorManager = sensorManager;
        side = true;
        accelerometer = null;
        light = null;
        magnetic = null;
        gyroscope = null;
        tool = new Toolbox(8);
        System.out.println("NEW INTERPLANETARYCOMP A"); // Debug removeme
        accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null)
            throw new Exception("Accelerometer not found.");
        axl = new float[3];
        ground = new float[3];
        copyVec(axl, tool.axis.left);
        copyVec(ground, tool.axis.left);

        sensorMode_RotationVector = true;
        rotationVector = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVector == null) {
            sensorMode_RotationVector = false;
            System.out.println("RotationVector not found. Virtualize orientation"); // Debug removeme
        }
        System.out.println("NEW INTERPLANETARYCOMP C"); // Debug removeme
        orientation = new float[3];
        rotationMatrix = new float[16];
        rot_x = new float[3];
        rot_y = new float[3];
        rot_z = new float[3];
        copyVec(rot_x, tool.axis.right);
        copyVec(rot_y, tool.axis.up);
        copyVec(rot_z, tool.axis.front);
        axis_x = new float[3];
        axis_y = new float[3];
        axis_z = new float[3];
        copyVec(axis_x, tool.axis.right);
        copyVec(axis_y, tool.axis.up);
        copyVec(axis_z, tool.axis.front);
        swap = new float[3];
        left = new float[3];
        right = new float[3];
        up = new float[3];
        down = new float[3];
        front = new float[3];
        back = new float[3];
        copyVec(left, tool.axis.left);
        copyVec(right, tool.axis.right);
        copyVec(up, tool.axis.up);
        copyVec(down, tool.axis.down);
        copyVec(front, tool.axis.front);
        copyVec(back, tool.axis.back);
        System.out.println("NEW INTERPLANETARYCOMP D"); // Debug removeme
        light = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lux = 1f;
        lightMaximumRange = 0;
        if (light != null) {
            if (light.getMaximumRange() <= 0) {
                light = null;
                System.gc();
            } else {
                lightMaximumRange = 1f / 500;
                lux = 0.5f;
            }
        }
        lux = light == null ? 1f : 0.5f;
        /*mag = null;
        pol = null;*/
        System.out.println("NEW INTERPLANETARYCOMP E"); // Debug removeme
        // TODO
        /*
        //gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        gyroscope = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (gyroscope == null)
            throw new Exception("Gyroscope not found.");
        gyr = tool.axis.left.clone();
        */
        // TODO
        magnetic = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        north = tool.axis.left.clone();
        mag = tool.axis.left.clone();
        pol = tool.axis.left.clone();
        //
        delta = 0d;
        tmp = new float[3];
        tmpx_ = new float[3];
        tmpy_ = new float[3];
        tmpz_ = new float[3];
        tmpx = new float[3];
        tmpy = new float[3];
        tmpz = new float[3];
        System.out.println("NEW INTERPLANETARYCOMP F"); // Debug removeme
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        if (rotationVector != null)
            sensorManager.registerListener(this, rotationVector,
                    SensorManager.SENSOR_DELAY_FASTEST);
        if (light != null)
            sensorManager.registerListener(this, light,
                    SensorManager.SENSOR_DELAY_FASTEST);
        System.out.println("NEW INTERPLANETARYCOMP G"); // Debug removeme
        pointerDown = false; // TODO
        pointerDown_position = new float[3]; // TODO
        pointerDown_directionVector = new float[3]; // TODO
        pointerDown_baseOrientation = new float[3];
        resume();
        System.out.println("NEW INTERPLANETARYCOMP OUT"); // Debug removeme
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x;
        float y;
        float z;
        int eType;

        eType = event.sensor.getType();
        switch (eType) {
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, orientation);
            case Sensor.TYPE_ACCELEROMETER:
                axl = event.values.clone();
                Toolbox.normalize(ground, axl);
                side = ground[1] < 0f;
                break;
            case Sensor.TYPE_LIGHT:
                lux = event.values[0];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mag = event.values.clone();
                Toolbox.normalize(north, event.values);
                break;
            /** // GYROSCOPE
             case Sensor.TYPE_GYROSCOPE:
             x = event.values[0];
             y = event.values[1];
             z = event.values[2];
             gyr[0] = (gyr[0] + x) % Toolbox.pi;
             gyr[1] = (gyr[1] + y) % Toolbox.pi;
             gyr[2] = (gyr[2] + z) % Toolbox.pi;
             break;
             */
        }
    }

    public void pause() {
        if (accelerometer != null)
            sensorManager.unregisterListener(this, accelerometer);
        if (rotationVector != null)
            sensorManager.unregisterListener(this, rotationVector);
        if (light != null)
            sensorManager.unregisterListener(this, light);
        if (magnetic != null)
            sensorManager.unregisterListener(this, magnetic);
        /* // TODO
        if (gyroscope != null)
            sensorManager.unregisterListener(this, gyroscope);
        */
    }

    public void resume() {
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        if (rotationVector != null)
            sensorManager.registerListener(this, rotationVector,
                    SensorManager.SENSOR_DELAY_FASTEST);
        if (light != null)
            sensorManager.registerListener(this, light,
                    SensorManager.SENSOR_DELAY_FASTEST);
        if (magnetic != null)
            sensorManager.registerListener(this, magnetic,
                    SensorManager.SENSOR_DELAY_FASTEST);
        /*
        if (gyroscope != null)
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_FASTEST);
        */
    }

    public float[] getDown() {
        return (down);
    }

    public float[] getUp() {
        return (up);
    }

    public float[] getFront() {
        return (front);
    }

    public float[] getBack() {
        return (back);
    }

    public float[] getRight() {
        return (right);
    }

    public float[] getLeft() {
        return (left);
    }

    public boolean getSensorMode() {
        return (sensorMode_RotationVector);
    }

    public boolean getPortraitSide() {
        if (sensorMode_RotationVector)
            return (side);
        return (true);
    }

    private void refreshOrientation_rotationVector() {
        float dota;
        float dotb;

        //  Vertical rotation
        dota = orientation[0];
        rotVec(tmpx_, axis_x, axis_y, dota);
        rotVec(tmpz_, axis_x, axis_z, dota);
        // Horizontal rotation
        dotb = orientation[2];
        rotVec(right, axis_z, tmpx_, dotb);
        rotVec(down, axis_z, axis_x, dotb);
        rotVec(front, axis_z, tmpz_, dotb);
        revVectorCopy(left, right);
        revVectorCopy(up, down);
        revVectorCopy(back, front);
    }

    private void refreshOrientation_accelerometer() {
        SensorManager.getRotationMatrix(rotationMatrix, null, axl, mag);
        SensorManager.getOrientation(rotationMatrix, orientation);
        refreshOrientation_rotationVector();
    }

    public void touchScreen_swipeUpdate(int xRef, int yRef, int halfScreenWidth, int halfScreenHeight) {
        /*
            pointerDown_directionVector[0] = xRef - pointerDown_position[0];
            pointerDown_directionVector[1] = yRef - pointerDown_position[1];
        */
        pointerDown_directionVector[0] = (xRef - pointerDown_position[0]) / halfScreenWidth;
        pointerDown_directionVector[1] = (yRef - pointerDown_position[1]) / halfScreenHeight;
        pointerDown_directionVector[0] *= -1;
        //pointerDown_directionVector[1] *= -1;
    }

    public void touchScreen_swipeDown(int xRef, int yRef) {
        pointerDown = true;
        pointerDown_position[0] = xRef;
        pointerDown_position[1] = yRef;
        pointerDown_baseOrientation[0] = orientation[0];
        pointerDown_baseOrientation[1] = orientation[1];
        pointerDown_baseOrientation[2] = orientation[2];
    }

    public void touchScreen_swipeUp() {
        pointerDown = false;
        pointerDown_directionVector[0] = 0;
        pointerDown_directionVector[1] = 0;
        pointerDown_baseOrientation[0] = orientation[0] % rad360;
        pointerDown_baseOrientation[1] = orientation[1] % rad360;
        pointerDown_baseOrientation[2] = orientation[2] % rad360;
    }

    public void refreshOrientation_touchScreen() {
        if (pointerDown) {
            orientation[0] = pointerDown_baseOrientation[0] + rad360 * pointerDown_directionVector[1];
            orientation[2] = pointerDown_baseOrientation[2] + rad180 * pointerDown_directionVector[0];
            if (orientation[2] > rad180)
                orientation[2] = rad180;
            if (orientation[2] < 0)
                orientation[2] = 0;
            //System.out.println("BATMAN :: Sensor :: Screen :: Orientation["+orientation[0]+"]["+orientation[1]+"]["+orientation[2]+"]"); // Debug
        }
    }

    public void refreshOrientation() {
        /*
        if (!sensorMode_RotationVector)
            refreshOrientation_accelerometer();
        */
        if (!sensorMode_RotationVector)
            refreshOrientation_touchScreen();
        refreshOrientation_rotationVector();
    }

    public float[] getRot() {
        if (sensorMode_RotationVector)
            return (orientation);
        return (null); // Do not use
    }

    static private int minValueIndex(float[] array) {
        float minValue;
        int index;
        int len;

        if (array == null)
            return (-1);
        len = array.length;
        index = -1;
        minValue = Float.MAX_VALUE;
        for (int i = 0; i < len; i++) {
            if (array[i] <= minValue) {
                minValue = array[i];
                index = i;
            }
        }
        return (index);
    }

    public double getDelta() {
        return (delta);
    }

    public float[] getAxlNormalized(float[] out) {
        return (normalize(out, axl));
    }

    public float[] getAxl() {
        return (axl);
    }

    public float[] getGyr() {
        return (gyr);
    }

    public float getLux() {
        float val;

        val = (float) Math.sqrt(lux / 25);
        if (val > 1f)
            return (1f);
        // Equivalent to (dotVec(sun, axis.front) + 1f) / 2
        val = 1f - val; // <- measure 180 degree angle ratio with sun/orientation
        return (val);
    }

    public float getLuxRaw() {
        return (lux);
    }

    public float[] getMagNormalized() {
        return (normalize(pol, mag));
    }

    public float[] getMag() {
        return (mag);
    }

    public String getInhabitant() {
        float deltaTolerance;
        float[] gravity;
        String[] objectName;
        int planetId;
        float f;

        // Todo Retourne 'Plutonien' ???
        deltaTolerance = 10f;
        f = (float) magVector(axl);
        gravity = new float[13];
        objectName = new String[13];
        gravity[0] = SensorManager.GRAVITY_DEATH_STAR_I;
        gravity[1] = SensorManager.GRAVITY_EARTH;
        gravity[2] = SensorManager.GRAVITY_JUPITER;
        gravity[3] = SensorManager.GRAVITY_MARS;
        gravity[4] = SensorManager.GRAVITY_MERCURY;
        gravity[5] = SensorManager.GRAVITY_MOON;
        gravity[6] = SensorManager.GRAVITY_NEPTUNE;
        gravity[7] = SensorManager.GRAVITY_PLUTO;
        gravity[8] = SensorManager.GRAVITY_SATURN;
        gravity[9] = SensorManager.GRAVITY_SUN;
        gravity[10] = SensorManager.GRAVITY_THE_ISLAND;
        gravity[11] = SensorManager.GRAVITY_URANUS;
        gravity[12] = SensorManager.GRAVITY_VENUS;
        objectName[0] = "/!\\ DANGER ETOILE A NEUTRON DANS VOTRE QUARTIER /!\\";
        objectName[1] = "Terrien";
        objectName[2] = "Jupiterien";
        objectName[3] = "Martien";
        objectName[4] = "Mercurien";
        objectName[5] = "Selenien";
        objectName[6] = "Neptunien";
        objectName[7] = "Plutonien";
        objectName[8] = "Saturnien";
        objectName[9] = "Heliosien";
        objectName[10] = "Ilien";
        objectName[11] = "Uranusien";
        objectName[12] = "Venusien";
        if (f == 0f)
            return ("Astronaute"); // Game may not be playable
        for (int i = 0; i < gravity.length; i++)
            gravity[i] = Math.abs(gravity[i] - f);
        planetId = minValueIndex(gravity);
        return (gravity[planetId] < deltaTolerance || planetId == -1 ?
                objectName[planetId] : sendBeacon("Keep calm " + planetId + ":" + f));
    }

    public String sendBeacon(String message) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        String pkgName = context.getApplicationContext().getPackageName();
        PackageInfo pkgInfo = null;
        message += ":";
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
            if (pkgInfo == null)
                throw new Exception("uh");
            message += pkgInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            message += "error";
        }
        return ("Extraheliosien");
    }

    /** TODO
    public float[]      mySextan()
    {
        float       lat;
        float       lon;

        lat = ;
        lon = ;
        Sphere s = new sphere();
        sun.getSunlight();
        Date d = getDate(); // new Calendar();
        Hour h = getHour(); // new Calendar();
        lat = getLat(h, d, sun);
        lon = getLon(h, d, sun);
        // concours lepine panneau solaire
    }*/

    /** TODO
    public float[]      mySurfaceViscosity()
    {
        // const PhoneSurfaceViscosity
        //Surface ref; // calibration
        //
                ^
            \ \ |
             \ \| aMax ; acc ; t*
              \ \
               \
                \  x <- aChaos ; stop() t*
        //
    }*/
}
