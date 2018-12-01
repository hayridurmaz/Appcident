package tr.edu.tedu.appcident;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import tr.edu.tedu.appcident.R;

import static java.lang.Thread.sleep;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


        try{

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.ACCESS_FINE_LOCATION

            };

            while(!hasPermissions(SplashScreen.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(SplashScreen.this, PERMISSIONS, PERMISSION_ALL);
                sleep(3000);
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }finally {
            Intent intent = new Intent(SplashScreen.this, SensorActivity.class);
            startActivity(intent);

        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
