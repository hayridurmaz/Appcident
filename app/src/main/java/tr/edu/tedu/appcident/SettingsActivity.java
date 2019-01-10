package tr.edu.tedu.appcident;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
 * Class Name: SettingsActivity
 * Created:10.01.1019
 * Author: Mina Ekin İnal, Hayri Durmaz
 *
 * It is an activity which is for user to manage settings about
 * the application.
 *
 * */

public class SettingsActivity extends Activity {
    String phoneNumber1;
    String phoneNumber2;
    String phoneNumber3;
    Button submitButton;
    EditText phoneInput1;
    EditText phoneInput2;
    EditText phoneInput3;
    ToggleButton workOnBackground;
    User user;

    TextView seconds;
    SeekBar seekBar;
    ProgressBar progressBar;
    int recordSeconds;


    TextView IMEITextView;
    TextView AddedPhones;
    String IMEINumber;
    public boolean shouldWorkOnBackgroun;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Firebase initialization.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Users");

        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Handling IMEI number operations.
        Bundle extras = getIntent().getExtras();
        IMEINumber = extras.getString("IMEINumber");

        IMEITextView = (TextView) findViewById(R.id.IMEITextView);
        IMEITextView.setText(IMEITextView.getText().toString() + " " + IMEINumber);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        seconds = (TextView) findViewById(R.id.Seconds);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        workOnBackground = (ToggleButton) findViewById(R.id.work_background);



        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean settingBackground;
                if(dataSnapshot.child(IMEINumber).child("workOnBackground").getValue()!=null){
                    settingBackground=dataSnapshot.child(IMEINumber).child("workOnBackground").getValue().toString().equalsIgnoreCase("true");
                }
                else{
                    settingBackground=true;
                }
                workOnBackground.setChecked(settingBackground);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });




        //workOnBackground.setChecked(true);
        workOnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldWorkOnBackgroun=workOnBackground.isChecked();

            }
        });
        progressBar.setProgress(50);
        seconds.setText("50");

        //Handling record duration seekbar object.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                progressBar.setProgress(i);
                seconds.setText("" + i + "");
                recordSeconds = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Connection between layout and the activity.
        phoneInput1 = (EditText) findViewById(R.id.phoneInput1);
        phoneInput2 = (EditText) findViewById(R.id.phoneInput2);
        phoneInput3 = (EditText) findViewById(R.id.phoneInput3);

        submitButton = (Button) findViewById(R.id.submit);
        AddedPhones = (TextView) findViewById(R.id.addedPhones);


        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.child(IMEINumber + "").exists()) {
                    //AddedPhones.setText("Numbers that you have added: \n-"+dataSnapshot.child(IMEINumber).child("number1").getValue()+"\n-"+dataSnapshot.child(IMEINumber).child("number2").getValue()+"\n-"+dataSnapshot.child(IMEINumber).child("number3").getValue());
                    phoneInput1.setText(dataSnapshot.child(IMEINumber).child("number1").getValue().toString());
                    phoneInput2.setText(dataSnapshot.child(IMEINumber).child("number2").getValue().toString());
                    phoneInput3.setText(dataSnapshot.child(IMEINumber).child("number3").getValue().toString());
                    seconds.setText(dataSnapshot.child(IMEINumber).child("recordTime").getValue().toString());
                    progressBar.setProgress(Integer.parseInt(dataSnapshot.child(IMEINumber).child("recordTime").getValue().toString()));

                    boolean settingBackground;
                    if(dataSnapshot.child(IMEINumber).child("workOnBackground").getValue()==null){
                        settingBackground=workOnBackground.isChecked();
                    }
                   else{
                        settingBackground=dataSnapshot.child(IMEINumber).child("workOnBackground").getValue().toString().equalsIgnoreCase("true");
                    }
                    workOnBackground.setChecked(settingBackground);
                    user = new User(dataSnapshot.child(IMEINumber).child("number1").getValue().toString(), dataSnapshot.child(IMEINumber).child("number2").getValue().toString(), dataSnapshot.child(IMEINumber).child("number3").getValue().toString(), IMEINumber, progressBar.getProgress(),settingBackground);
                    //myRef.child(IMEINumber).setValue(user);
                }

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        myRef.addValueEventListener(postListener);

        //Click listener for submit button of settings menu. This section also handles phone number inputs.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber1 = phoneInput1.getText().toString();
                if (phoneInput1.getText().length() != 11) {
                    phoneInput1.setError("Please enter valid number");
                } else {
                    phoneNumber1 = phoneInput1.getText().toString();
                }

                phoneNumber2 = phoneInput2.getText().toString();

                if (phoneInput2.getText().length() != 11) {
                    phoneInput2.setError("Please enter valid number");
                } else {
                    phoneNumber2 = phoneInput2.getText().toString();
                }

                phoneNumber3 = phoneInput3.getText().toString();

                if (phoneInput3.getText().length() != 11) {
                    phoneInput3.setError("Please enter valid number");
                } else {
                    phoneNumber3 = phoneInput3.getText().toString();
                }

                if (phoneNumber3.length() == 11 && phoneNumber2.length() == 11 && phoneNumber1.length() == 11) {
                    showToast("Now your emergency list is ready!!");

                    //Putting needed variables into firebase database.
                    user = new User(phoneNumber1, phoneNumber2, phoneNumber3, IMEINumber, progressBar.getProgress(), workOnBackground.isChecked());
                    myRef.child(IMEINumber).setValue(user);

                    Intent intent = new Intent(SettingsActivity.this, SensorActivity.class);
                    startActivity(intent);
                }


            }
        });
    }

    public void showToast(String text) {
        Toast.makeText(SettingsActivity.this, text, Toast.LENGTH_LONG).show();
    }
}

