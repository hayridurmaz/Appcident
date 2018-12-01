package tr.edu.tedu.appcident;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class SensorActivity extends AppCompatActivity implements SensorEventListener/*, View.OnClickListener, SurfaceHolder.Callback*/ {
    private static final int INTENTCAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mHeat;
    private Sensor mAcceleration;
    private Sensor mGyroscope;
    private Sensor mLight;
    private Sensor mRotation;
    static boolean isEmergancyMode;
    static boolean shouldGoIntoEmergencyMode;
    private static String IMEINumber;
    static String  currentPath;

    private Button start, stop;

    private float accelerationCurrent, accelerationLast, acceleration;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private double rootSquare=0;
    private float lastX, lastY, lastZ;

    private float vibrateThreshold = 0;

    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;

    public Vibrator v;

    TextView output, textt;
    MediaRecorder mediaRecorder = new MediaRecorder();

    static DialogInterface currentDialogInterface;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(SensorActivity.this, SettingsActivity.class);
            intent.putExtra("IMEINumber", IMEINumber);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_sensor);



        //getSupportActionBar();
        isEmergancyMode=false;

/*
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        recorder = new MediaRecorder();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //check if permission request is necessary
        {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }

        initRecorder();
*/

       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 200);
        }
        else{
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.getDeviceId();
            IMEINumber = telephonyManager.getDeviceId();
        }
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 200);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 200);
        }*/


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child("User1");

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        output = (TextView) findViewById(R.id.label_light);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeat = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        accelerationCurrent = SensorManager.GRAVITY_EARTH;
        accelerationLast = SensorManager.GRAVITY_EARTH;
        acceleration = 0.0f;

        textt = (TextView) findViewById(R.id.label_light);




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(SensorActivity.this, VideoRecorderActivity.CAMERA_SERVICE);
                // startService()


//                final int REQUEST_VIDEO_CAPTURE = 1;
//
//
//                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//                        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//                    }

                emergencyMode();


            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try{
                    mediaRecorder.stop();
                    isEmergancyMode=false;
                    shouldGoIntoEmergencyMode=false;
                }
                catch (Exception e){

                }
                Toast.makeText(SensorActivity.this,"Kayıt durduruldu",Toast.LENGTH_LONG).show();

               /* MediaPlayer mp = new MediaPlayer();
                try{
                    mp.setDataSource(currentPath);

                }
                catch (Exception e){

                }
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // Do something. For example: playButton.setEnabled(true);
                        mp.start();
                    }
                });
                mp.prepareAsync();*/



            }
        });


    }
    public void onPrepared(MediaPlayer player) {
    }


    private void doOnEmergency(){
        Intent i = new Intent(SensorActivity.this,VideoCapture.class);
        sendSMS("+905058978796","sa");
        //startActivity(i);
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"APPCIDENT");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String AudioSavePathInDevice = folder
                .getAbsolutePath()+"/"+
                Calendar.getInstance().getTime()+ ".3gp";

        currentPath=AudioSavePathInDevice;
        mediaRecorder=new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

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

       // textt.setText(textt.getText() + " " + name1);

        switch (sensorType){
            case Sensor.TYPE_PRESSURE:
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                float x = event.values[0];
                if(x>80){
                    emergencyMode();
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

                accelerationCurrent = (float)Math.sqrt(Math.pow(X, 2)
                        + Math.pow(Y, 2)
                        + Math.pow(Z, 2));

                float delta = accelerationCurrent - accelerationLast;

                acceleration = acceleration * 0.9f + delta;

                float a = event.values[0];
                float b = event.values[1];
                float c = event.values[2];

                rootSquare=Math.sqrt(Math.pow(a,2)+Math.pow(b,2)+Math.pow(c,2));
                if(rootSquare<2.0)
                {
                    emergencyMode();
                }


                DecimalFormat precision = new DecimalFormat("0,00");// Telefona yüklerken virgül yap
                double ldAccRound = Double.parseDouble(precision.format(accelerationCurrent));

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
                textt = (TextView) findViewById(R.id.label_light);
                //textt.setText("Vay???");
                break;

            case Sensor.TYPE_LIGHT:
                break;
        }

        // Do something with this sensor data.
    }

    private Uri getOutputMediaFileUri(int type){

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraVideo");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){


                output.setText("Failed to create directory MyCameraVideo.");

                Toast.makeText(SensorActivity.this, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if(type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // After camera screen this code will excuted

        if (requestCode == INTENTCAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                output.setText("Video File : " +data.getData());

                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:" +
                        data.getData(), Toast.LENGTH_LONG).show();

            } else if (resultCode == RESULT_CANCELED) {

                output.setText("User cancelled the video capture.");

                // User cancelled the video capture
                Toast.makeText(this, "User cancelled the video capture.",
                        Toast.LENGTH_LONG).show();

            } else {

                output.setText("Video capture failed.");

                // Video capture failed, advise user
                Toast.makeText(this, "Video capture failed.",
                        Toast.LENGTH_LONG).show();
            }
        }
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



    protected void emergencyMode () {
        /*
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        */
        if(!isEmergancyMode){
            isEmergancyMode=true;
            textt.setText("Düştü");
            AlertDialog.Builder builder1 = new AlertDialog.Builder(SensorActivity.this);
            builder1.setMessage("Is there something wrong?.");
            builder1.setCancelable(false);

            shouldGoIntoEmergencyMode=true;

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();

            builder1.setPositiveButton(
                    "I'Am OKAY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            shouldGoIntoEmergencyMode=false;
                            r.stop();
                        }
                    });

            final AlertDialog alert11 = builder1.create();
            alert11.show();

            alert11.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });

            final Timer timer2 = new Timer();
            timer2.schedule(new TimerTask() {
                public void run() {

                    timer2.cancel(); //this will cancel the timer of the system


                    alert11.cancel();
                    r.stop();
                    if(shouldGoIntoEmergencyMode) {
                        doOnEmergency();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SensorActivity.this,"No response, emergency mode is started",Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    else{
                        isEmergancyMode=false;
                    }
                }
            }, 5000); // the timer will count 5 seconds....



           /* if(shouldGoIntoEmergencyMode) {
                Thread timerThread = new Thread() {
                    public void run() {
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {


                            doOnEmergency();
                            if (alert11 != null) {
                                alert11.cancel();
                            }
                            // Toast.makeText(SensorActivity.this,"No response, emerggency mode is started",Toast.LENGTH_LONG).show();

                        }
                    }
                };
                timerThread.start();
            }*/


        }
        else{
            //shouldGoIntoEmergencyMode=false;
        }

    }
    /*

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile("/sdcard/videocapture_example.mp4");
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            recorder.start();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
    }
    */
}