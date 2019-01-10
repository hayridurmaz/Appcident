package tr.edu.tedu.appcident;
import java.io.IOException;
import java.util.List;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Parcelable;

import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.location.DetectedActivity;

/*
 * Class Name: BackService
 * Created:10.01.1019
 * Author:  Batuhan Mert Karabulut
 *
 * It is the main back service that listens the sensors and opens Sensor Activity
 * if necessary.
 *
 * */

public class BackService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mHeat;
    private Sensor mAcceleration;
    private Sensor mGyroscope;
    private Sensor mLight;
    private Sensor mRotation;
    private static float Heats[];
    private static int currentHeatArrayIndex;
    private long Timer;

    private double rootSquare = 0;

    public float[] accData1;
    public float[] accData2;
    public float[] accData3;
    public long startListenTime = 0;
    public long currentListenTime = 0;
    public int currSeconds = 0;
    public int secondSent = -1;

    public boolean ppp = false;
    public boolean isStopped = false;
    public boolean stillMoving = true;
    public boolean moved = true;

    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 10;
    private long lastUpdate = 0;

    private float accelerationCurrent, accelerationLast, acceleration;

    @Override
    public void onCreate() {

        //Sensor initializiaton.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeat = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Sensor registers.
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeat, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

        //Required variables for accelerometer sensor data.
        accelerationCurrent = SensorManager.GRAVITY_EARTH;
        accelerationLast = SensorManager.GRAVITY_EARTH;
        acceleration = 0.0f;

        accData1 = new float[60];
        accData2 = new float[60];
        accData3 = new float[60];

        //Required variables for ambient temperature sensor data.
        Heats= new float[30];
        Timer=0;


        super.onCreate();

        Toast toast = Toast.makeText(this, "Service is active!!", Toast.LENGTH_SHORT);
        toast.show();
    }

    void onEmergency(){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setClassName("tr.edu.tedu.appcident",
                "tr.edu.tedu.appcident.SensorActivity");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("isBacked", true);
        i.putExtra("isBacked1", "trueee");

        startActivity(i);
    }

    //Required method for service class.
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    //Required method for service class.
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void sendDataToHeatArray(float x){
        if(currentHeatArrayIndex<29){
            currentHeatArrayIndex++;
        }
        else{
            currentHeatArrayIndex=0;
        }
        Heats[currentHeatArrayIndex]=x;
        Log.w("Current",currentHeatArrayIndex+"");

        Log.w("Heats[0]",Heats[0]+"");
        Log.w("Heats[29]",Heats[29]+"");

        if((Math.abs(Heats[0]-Heats[29])>4)&& (Heats[0]!=0) && Heats[29]!=0){
            onEmergency();
        }

    }

    //The method that handles and deals sensor data.
    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        switch (sensorType) {

            //This section handles ambient temperature sensor data.
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                float x = event.values[0];


                Log.w("Milis-Timer",System.currentTimeMillis()-Timer+"");
                Log.w("Timer",Timer+"");
                if((Timer==0.0)||((System.currentTimeMillis()-Timer)/1000>=1)){
                    sendDataToHeatArray(x);
                    Timer=System.currentTimeMillis();
                }

                if (x > 60) {
                    onEmergency();
                }

                break;

            //This section handles accelerometer sensor data.
            case Sensor.TYPE_ACCELEROMETER:

                float X = event.values[0];
                float Y = event.values[1];
                float Z = event.values[2];

                //Calculations for accident detection with accelerometer data.
                accelerationLast = accelerationCurrent;

                accelerationCurrent = (float) Math.sqrt(Math.pow(X, 2)
                        + Math.pow(Y, 2)
                        + Math.pow(Z, 2));

                float delta = accelerationCurrent - accelerationLast;

                acceleration = acceleration * 0.9f + delta;

                float a = event.values[0];
                float b = event.values[1];
                float c = event.values[2];

                //After an accident occurs, the application checks for any moment in a certain amount of time which is set by us.
                if (ppp){
                    if (startListenTime == 0.0f){
                        startListenTime = System.currentTimeMillis();
                    }
                    currentListenTime = System.currentTimeMillis() - startListenTime;

                    if ((int)(currentListenTime / 1000) == secondSent + 1){
                        secondSent++;
                        accelerometerData(a, b, c);
                    }
                }

                //This section handles the detection of an accident with accelerometer data.
                rootSquare = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2));
                if (rootSquare < 2.0 && !ppp) {
                    ppp = true;
                    Toast.makeText(BackService.this, "KOYDUK", Toast.LENGTH_SHORT).show();
                }

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //In this method, the data that come from accelerometer sensor are handled.
    public void accelerometerData (float a, float b, float c){
        accData1[secondSent] = a;
        accData2[secondSent] = b;
        accData3[secondSent] = c;
        boolean isOK = false;

        if (secondSent > 0){

            if (shakeData(a, b, c)){
                if (isStopped){
                    isOK = true;
                }

            }
            else {
                stillMoving = false;
                isStopped = true;
            }
        }

        //When the user is safe, this section resets the required parameters.
        if (isOK){
            ppp = false;
            secondSent = -1;
            startListenTime = 0;
            accData1 = new float[60];
            accData2 = new float[60];
            accData3 = new float[60];
            stillMoving = true;
            isStopped = false;
        }

        //After amount of time, if the user is not safe, emergency mode will be triggered.
        if (secondSent == 10){
            //If the user is still moving after amount of time, required parameters will be reset.
            if (stillMoving){
                ppp = false;
                secondSent = -1;
                startListenTime = 0;
                accData1 = new float[60];
                accData2 = new float[60];
                accData3 = new float[60];
                stillMoving = true;
                isStopped = false;
            }

            else {
                onEmergency();
            }

        }

    }

    //We are using shake detection method for any movement during the time that application checks if the user is safe or not.
    public boolean shakeData(float x1, float y1, float z1){
        long curTime = System.currentTimeMillis();
        // Only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            x = x1;
            y = y1;
            z = z1;

            float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                return true;
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
        return false;
    }

}