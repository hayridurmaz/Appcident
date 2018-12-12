package tr.edu.tedu.appcident;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.*;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class SensorActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback /*, View.OnClickListener, SurfaceHolder.Callback*/ {
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
    static String currentPath;

    private Button start, stop, startService;
    String mUserLocation = "Cannot find address";


    private float accelerationCurrent, accelerationLast, acceleration;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private double rootSquare = 0;
    private float lastX, lastY, lastZ;

    private float vibrateThreshold = 0;

    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;

    public Vibrator v;
    double LAT, LON;
    int seconds;

    TextView output, textt;
    MediaRecorder mediaRecorder = new MediaRecorder();

    private FusedLocationProviderClient mFusedLocationClient;
    String currentAddress;
    static DialogInterface currentDialogInterface;
    LocationManager locationManager;

    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera ;
    public static boolean mPreviewRunning;

    @SuppressLint("MissingPermission")
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_sensor);


        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.e("NOTNULLLOCC",location.toString());
                        LAT=location.getLatitude();
                        LON=location.getLongitude();
                        currentAddress=getCompleteAddress(LAT,LON);
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(SensorActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //Intent intent = new Intent(SensorActivity.this,LocationUpdateService.class);
        //startService(intent);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(SensorActivity.this);


        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(SensorActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        Log.e(" GELDİ AMA NULL","olabilir");
                        if (location != null) {
                            Log.e("LOCATION",location.toString());
                            LAT=location.getLatitude();
                            LON=location.getLongitude();
                            currentAddress=getCompleteAddress(LAT,LON);
                        }
                        else{
                            currentAddress="We could not determine address.";
                        }
                    }
                });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        //getSupportActionBar();
        isEmergancyMode = false;

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
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.getDeviceId();
            IMEINumber = telephonyManager.getDeviceId();
        }

        if (IMEINumber == null) {
            IMEINumber = "1";
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


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(IMEINumber);

        myRef.child("recordTime").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    seconds=Integer.parseInt(dataSnapshot.getValue().toString());
                    Log.d("dataSnapshot.child: ",dataSnapshot.getValue().toString());
                }
                else {
                    seconds=5;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.setVisibility(View.INVISIBLE);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        startService = (Button)findViewById(R.id.buttonService);

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

                mSurfaceView.setVisibility(View.INVISIBLE);
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
                startService.setVisibility(View.VISIBLE);

                stopService(new Intent(getApplicationContext(), CameraService.class));


                try {
                    mediaRecorder.stop();
                    isEmergancyMode = false;
                    shouldGoIntoEmergencyMode = false;
                } catch (Exception e) {

                }
                Toast.makeText(SensorActivity.this, "Kayıt durduruldu", Toast.LENGTH_LONG).show();

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

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), BackService.class));
            }
        });

        if (getIntent().getExtras() != null)
            Log.v("Sıkıntı", getIntent().getExtras().getString("isBacked1"));

        if (getIntent().getExtras() != null && getIntent().getExtras().getString("isBacked1").equals("trueee")){
            start.performClick();
        }


    }

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



    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        //Intent intent = new Intent(SensorActivity.this,LocationUpdateService.class);
        //stopService(intent);
        super.onStop();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }



    public void onPrepared(MediaPlayer player) {
    }

    private void doOnEmergency() {

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(SensorActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        Log.e(" GELDİ AMA NULL","olabilir");
                        if (location != null) {
                            Log.e("LOCATION",location.toString());
                            LAT=location.getLatitude();
                            LON=location.getLongitude();
                            currentAddress=getCompleteAddress(LAT,LON);
                        }
                        else{
                            currentAddress="We could not determine address.";
                        }
                    }
                });

        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), CameraService.class));

                mSurfaceView.setVisibility(View.VISIBLE);
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.INVISIBLE);
                startService.setVisibility(View.INVISIBLE);
            }
        };
        mainHandler.post(myRunnable);

        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> geoAddresses = new ArrayList<>();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textt.setText("We send an sms to your friends: \"Your friend may be in trouble in  " + LAT+", "+ LON+" , "+currentAddress+"\"");
            }
        });

        /*try {
            geoAddresses = gcd.getFromLocation(LAT, LON, 1);
            if (geoAddresses.size() > 0) {
                for (int in = 0; in < 4; in++) { //Since it return only four value we declare this as static.
                    // mUserLocation = mUserLocation + geoAddresses.get(0).getAddressLine(in).replace(",", "") + ", ";
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textt.setText("sa in " + LAT+", "+ LON+" , "+mUserLocation);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        sendSMS("+905058978796", "Your friend may be in trouble in " + LAT+", "+ LON+" , "+currentAddress);
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "APPCIDENT");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String AudioSavePathInDevice = folder
                .getAbsolutePath() + "/" +
                Calendar.getInstance().getTime() + ".3gp";

        /*
        currentPath = AudioSavePathInDevice;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
        */
        try {
            //mediaRecorder.prepare();
            //mediaRecorder.start();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doOnStoppingEmergency();
                        }
                    }, seconds*1000);
                }
            });
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public void doOnStoppingEmergency(){
        stop.performClick();
    }


    public String getCompleteAddress(double latitude, double longitude) {
        String location = "";
        try {
            Geocoder geocoder = new Geocoder(SensorActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String state, city, zip, street;
                if (address.getAdminArea() != null) {
                    state = address.getAdminArea();
                } else {
                    state = "";
                }
                if (address.getLocality() != null) {
                    city = address.getLocality();
                } else {
                    city = "";
                }
                if (address.getPostalCode() != null) {
                    zip = address.getPostalCode();
                } else {
                    zip = "";
                }

                if (address.getThoroughfare() != null) {
                    street = address.getSubLocality() + "," + address.getThoroughfare();
                } else {
                    street = address.getSubLocality() + "," + address.getFeatureName();
                }
                location = street + "," + city + "," + zip + "," + state;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("LOCATION:",location);
        return location;
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

           switch (sensorType) {
               case Sensor.TYPE_PRESSURE:
                   break;

               case Sensor.TYPE_AMBIENT_TEMPERATURE:
                   float x = event.values[0];
                   if (x > 80) {
                       emergencyMode();
                   }
                   break;

               case Sensor.TYPE_ACCELEROMETER:

                /*
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

                rootSquare = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2));
                if (rootSquare < 2.0) {
                    emergencyMode();
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
                textt = (TextView) findViewById(R.id.label_light);
                //textt.setText("Vay???");
                break;

            case Sensor.TYPE_LIGHT:
                break;
        }

        // Do something with this sensor data.
    }

    private Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraVideo");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {


                output.setText("Failed to create directory MyCameraVideo.");

                Toast.makeText(SensorActivity.this, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");

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

                output.setText("Video File : " + data.getData());

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

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
/*
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(SensorActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        Log.e(" GELDİ AMA NULL","olabilir");
                        if (location != null) {
                            Log.e("LOCATION",location.toString());
                            LAT=location.getLatitude();
                            LON=location.getLongitude();
                            currentAddress=getCompleteAddress(LAT,LON);
                        }
                        else{
                            currentAddress="We could not determine address.";
                        }
                    }
                });
*/

       // Intent intent = new Intent(SensorActivity.this,LocationUpdateService.class);
        //startService(intent);

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

       // Intent intent = new Intent(SensorActivity.this,LocationUpdateService.class);
        //stopService(intent);

        mSensorManager.unregisterListener(this);
    }


    protected void emergencyMode() {
        /*
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        */
        if (!isEmergancyMode) {
            isEmergancyMode = true;
            textt.setText("Something's wrong?");
            AlertDialog.Builder builder1 = new AlertDialog.Builder(SensorActivity.this);
            builder1.setMessage("Is there something wrong?.");
            builder1.setCancelable(false);

            shouldGoIntoEmergencyMode = true;

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();

            builder1.setPositiveButton(
                    "I'Am OKAY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            textt.setText("Everything's okay!");
                            shouldGoIntoEmergencyMode = false;
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
                    if (shouldGoIntoEmergencyMode) {
                        doOnEmergency();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SensorActivity.this, "No response, emergency mode is started", Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        isEmergancyMode = false;
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


        } else {
            //shouldGoIntoEmergencyMode=false;
        }

    }
/*
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            Log.w("location.getLatitude",location.getLatitude()+"");
            Log.v("location.getLongitude",location.getLongitude()+"");
            Log.v("location.mUserLocation",getCompleteAddress(LAT,LON));
            if(location.getLatitude()!=0 &&location.getLongitude()!=0){
                LAT=location.getLatitude();
                LON=location.getLongitude();
                mUserLocation=getCompleteAddress(LAT,LON);
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/
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
*/
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
/*
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
    }
    */
}