package tr.edu.tedu.appcident;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;



/*
 * Class Name: SensorActivity
 * Created:10.01.1019
 * Author: Hayri Durmaz, Batuhan Mert Karabulut, Mina Ekin İnal
 *
 * It is the main activity which opens when the emergency occurs or user opens the app.
 * This activity does everything related to the applications main purposes.
 *
 * */

public class SensorActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback /*, View.OnClickListener, SurfaceHolder.Callback*/ {

    /*
     *  Main activity's variables
     */
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

    private Button start, stop, startService;


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


    DatabaseReference myRef;

    /*
        Oncreate method. In this method, we initialize everything in this activity.
     */
    @SuppressLint("MissingPermission")
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_sensor);

        //Location operations are handled in this section.
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
                        Log.e("NOTNULLLOCC", location.toString());
                        LAT = location.getLatitude();
                        LON = location.getLongitude();
                        currentAddress = getCompleteAddress(LAT, LON);
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
                        Log.e(" GELDİ AMA NULL", "olabilir");
                        if (location != null) {
                            Log.e("LOCATION", location.toString());
                            LAT = location.getLatitude();
                            LON = location.getLongitude();
                            currentAddress = getCompleteAddress(LAT, LON);
                        } else {
                            currentAddress = "We could not determine address.";
                        }
                    }
                });

        //Checking permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        //getSupportActionBar();
        isEmergancyMode = false;

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


        //Connecting to the firebase database.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child(IMEINumber);


        //Getting user's record time setting.
        myRef.child("recordTime").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    seconds = Integer.parseInt(dataSnapshot.getValue().toString());
                    Log.d("dataSnapshot.child: ", dataSnapshot.getValue().toString());
                } else {
                    Intent intent = new Intent(SensorActivity.this, SettingsActivity.class);
                    intent.putExtra("IMEINumber", IMEINumber);
                    startActivity(intent);
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

        //Connection between layout and activity.
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        startService = (Button) findViewById(R.id.buttonService);

        output = (TextView) findViewById(R.id.label_light);

        //Initializations for sensors.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHeat = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        float accelerationCurrent = SensorManager.GRAVITY_EARTH;
        float accelerationLast = SensorManager.GRAVITY_EARTH;
        float acceleration = 0.0f;

        textt = (TextView) findViewById(R.id.label_light);

        //Click listener of start emergency mode button.
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShouldEmergencyMode();
            }
        });

        //Click listener of stop emergency mode button.
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

            }
        });

        //Click listener of start service button. Starts sensor service of the application.
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), BackService.class));
            }
        });

        startService.performClick();

        //Checking if the sensor service sends emergency mode message through Extras.
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("isBacked1").equals("trueee")) {
            start.performClick();
        }

    }


    //Creating toolbar menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    //Items selection in toolbar menu
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


    //This method send an sms with Telephone's SMS Manager.
    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    public void onPrepared(MediaPlayer player) {
    }


    //This method includes every tasks in emergency moda.
    private void doOnEmergency() {

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(SensorActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        Log.e(" GELDİ AMA NULL", "olabilir");
                        if (location != null) {
                            Log.e("LOCATION", location.toString());
                            LAT = location.getLatitude();
                            LON = location.getLongitude();
                            currentAddress = getCompleteAddress(LAT, LON);
                        } else {
                            currentAddress = "We could not determine address.";
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
                textt.setText("We send an sms to your friends: \"Your friend may be in trouble in  (LAT = " + LAT + " , LON = " + LON + ") , " + currentAddress + "\"");
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

        if (currentAddress == null || currentAddress.equalsIgnoreCase("null")) {
            currentAddress = "Cannot determined";
        }
        sendSMS("+905058978796", "Your friend may be in trouble in  (LAT = " + LAT + " , LON = " + LON + ") , " + currentAddress);
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "APPCIDENT");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String AudioSavePathInDevice = folder
                .getAbsolutePath() + "/" +
                Calendar.getInstance().getTime() + ".3gp";

        try {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doOnStoppingEmergency();
                        }
                    }, seconds * 1000);
                }
            });
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void doOnStoppingEmergency() {
        stop.performClick();
    }



    //This method connects to the Google Geocoder service and converts taken lat and lot info into string address information.
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
        Log.d("LOCATION:", location);
        return location;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public final void onSensorChanged(SensorEvent event) {

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


        //Registering sensors in order to get data from them.
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeat, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Unregistering sensor manager.
        mSensorManager.unregisterListener(this);
    }


    //This method asks user if s/he is okay, if not goes into emergency mode.
    protected void ShouldEmergencyMode() {

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
                            textt.setText(R.string.im_okay);
                            shouldGoIntoEmergencyMode = false;
                            isEmergancyMode = false;
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
            }, 20000); // the timer will count 5 seconds....


        } else {
            //shouldGoIntoEmergencyMode=false;
        }

    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void getInfo(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SensorActivity.this);
        builder1.setMessage("This app is prepared to help you in moments of accident and emergency. Please make sure you have adjusted your settings correctly before you start using the appcident.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Understand!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}