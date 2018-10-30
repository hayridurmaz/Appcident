package tr.edu.tedu.appcident;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Vibrator;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.Console;

public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mHeat;
    private Sensor mAcceleration;
    private Sensor mGyroscope;
    private Sensor mLight;
    private Sensor mRotation;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private float lastX, lastY, lastZ;

    private float vibrateThreshold = 0;



    public Vibrator v;



    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeat = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);



    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public final void onSensorChanged(SensorEvent event) {
        float currentValue = event.values[0];

        int sensorType = event.sensor.getType();

        String name1 = event.sensor.getName();
        TextView textt = (TextView) findViewById(R.id.label_light);
        textt.setText(textt.getText() + " " + name1);

        switch (sensorType){
            case Sensor.TYPE_PRESSURE:
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                break;

            /*case Sensor.TYPE_ACCELEROMETER:
                String name1 = event.sensor.getName();
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
            }

                break;
*/
            case Sensor.TYPE_GYROSCOPE:
                name1 = event.sensor.getName();
                textt = (TextView) findViewById(R.id.label_light);
                textt = (TextView) findViewById(R.id.label_light);
                textt.setText("Vay aq???");
                break;

            case Sensor.TYPE_LIGHT:
                break;
        }

        // Do something with this sensor data.
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();

        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeat, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}