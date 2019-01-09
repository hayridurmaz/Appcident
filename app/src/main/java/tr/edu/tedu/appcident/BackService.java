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
    private float Timer;

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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeat = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeat, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

        accelerationCurrent = SensorManager.GRAVITY_EARTH;
        accelerationLast = SensorManager.GRAVITY_EARTH;
        acceleration = 0.0f;


        accData1 = new float[60];
        accData2 = new float[60];
        accData3 = new float[60];

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
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void sendDataToHeatArray(float x){
        if(currentHeatArrayIndex<30){
            currentHeatArrayIndex++;
        }
        else{
            currentHeatArrayIndex=0;
        }
        Heats[currentHeatArrayIndex]=x;


        if((Math.abs(Heats[0]-Heats[29])>4)&& (Heats[0]!=0) && Heats[29]!=0){
            onEmergency();
        }

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float currentValue = event.values[0];

        int sensorType = event.sensor.getType();

        String name1 = event.sensor.getName();

        // textt.setText(textt.getText() + " " + name1);
        //Log.w("SENSOR NAME",name1);
        switch (sensorType) {
            case Sensor.TYPE_PRESSURE:
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:

                float x = event.values[0];

                //Toast toast = Toast.makeText(this, x+"", Toast.LENGTH_SHORT);
                //toast.show();
                if((Timer==0)||(System.currentTimeMillis()-Timer>=1)){
                    Log.e("qqqqqqqqqqqq", "SIKINTIIIII");
                    sendDataToHeatArray(x);
                    Timer=System.currentTimeMillis();
                }
                if (x > 60) {
                    onEmergency();
                }
                break;

            case Sensor.TYPE_ACCELEROMETER:
                /*String name1 = event.sensor.getName();
                TextView textt = (TextView) findViewById(R.id.label_light);
                textt.setText(textt.getText() + " " + name1);

                deltaX = Math.abs(lastX - event.values[0]);
                deltaY = Math.abs(lastY - event.values[1]);
                deltaZ = Math.abs(lastZ - event.values[2]);

                if (deltaX < 2)
                deltaX = 0;
                if (deltaY < 2)
                deltaY = 0;
                if ((deltaX > vibrateThreshold) || (deltaY > 9.81f) || (deltaZ > vibrateThreshold)) {
                // textt.setText("Oluyor mu acaba???");
            }*/
                float X = event.values[0];
                float Y = event.values[1];
                float Z = event.values[2];

                accelerationLast = accelerationCurrent;

                accelerationCurrent = (float) Math.sqrt(Math.pow(X, 2)
                        + Math.pow(Y, 2)
                        + Math.pow(Z, 2));

                float delta = accelerationCurrent - accelerationLast;

                acceleration = acceleration * 0.9f + delta;

                float a = event.values[0];
                float b = event.values[1];
                float c = event.values[2];

                //Checking 30secs
                if (ppp){
                    if (startListenTime == 0.0f){
                        startListenTime = System.currentTimeMillis();
                    }
                    currentListenTime = System.currentTimeMillis() - startListenTime;

                    if ((int)(currentListenTime / 1000) == secondSent + 1){
                        //Toast.makeText(BackService.this, (int)(currentListenTime / 1000) + "!!!" + secondSent, Toast.LENGTH_LONG).show();
                        secondSent++;
                        //Toast.makeText(BackService.this, a + "!!!", Toast.LENGTH_SHORT).show();
                        accelerometerData(a, b, c);
                    }
                }

                rootSquare = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2));
                if (rootSquare < 2.0 && !ppp) {
                    ppp = true;
                    Toast.makeText(BackService.this, "KOYDUK", Toast.LENGTH_SHORT).show();
                }


                // DecimalFormat precision = new DecimalFormat("0.00");// Telefona yüklerken virgül yap
                //double ldAccRound = Double.parseDouble(precision.format(accelerationCurrent));

/*
                if (ldAccRound > 0.3d && ldAccRound < 0.5d) {
                    emergencyMode();
                }

                if (acceleration > 51) {
                    emergencyMode();
                }
*/
                break;

            case Sensor.TYPE_GYROSCOPE:
                name1 = event.sensor.getName();

                //textt.setText("Vay???");
                break;

            case Sensor.TYPE_LIGHT:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void accelerometerData (float a, float b, float c){
        accData1[secondSent] = a;
        accData2[secondSent] = b;
        accData3[secondSent] = c;
        boolean isOK = false;

        //Toast.makeText(BackService.this, secondSent + "!!!" + a, Toast.LENGTH_LONG).show();


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

        if (isOK){
            //Toast.makeText(BackService.this,"SAFEEE" + a, Toast.LENGTH_LONG).show();
            ppp = false;
            secondSent = -1;
            startListenTime = 0;
            accData1 = new float[60];
            accData2 = new float[60];
            accData3 = new float[60];
            stillMoving = true;
            isStopped = false;
        }

        if (secondSent == 10){
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