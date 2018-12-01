package tr.edu.tedu.appcident;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ServiceCompat;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LocationUpdateService extends Service {
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final IBinder mBinder = new LocalBinder();
    private static double distanceInMeters;
    public static Location lastLocation = null;
    public static double lat,lon;


    public class LocalBinder extends Binder {
        LocationUpdateService getService() { // Return this instance of LocalService so clients can call public methods
            return LocationUpdateService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (lastLocation == null) {
                    lastLocation = location;
                }
                distanceInMeters += location.distanceTo(lastLocation);
                lastLocation = location;
                lat=location.getLatitude();
                lat=location.getLongitude();
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onProviderEnabled(String arg0) {
            }

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle bundle) {
            }
        };
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);




    }

    public Date getCurrentDate(){
        return  Calendar.getInstance().getTime();
    }


    public double getMiles() {
        return this.distanceInMeters;
    }
}