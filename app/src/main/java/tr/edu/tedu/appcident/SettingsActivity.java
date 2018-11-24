package tr.edu.tedu.appcident;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends Activity {
    String phoneNumber1;
    String phoneNumber2;
    String phoneNumber3;
    Button submitButton;
    EditText phoneInput1;
    EditText phoneInput2;
    EditText phoneInput3;

    TextView IMEITextView;
    String IMEINumber;


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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Users");
        // myRef.setValue("sa","as");


        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        IMEINumber = extras.getString("IMEINumber");

        IMEITextView = (TextView)findViewById(R.id.IMEITextView);
        IMEITextView.setText(IMEITextView.getText().toString() + " " + IMEINumber);

        phoneInput1 = (EditText) findViewById(R.id.phoneInput1);
        phoneInput2 = (EditText) findViewById(R.id.phoneInput2);
        phoneInput3 = (EditText) findViewById(R.id.phoneInput3);

        submitButton = (Button) findViewById(R.id.submit);

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
                    User user = new User(phoneNumber1, phoneNumber2, phoneNumber3);
                    myRef.child("User1").setValue(user);
                    Intent intent = new Intent(SettingsActivity.this, SensorActivity.class);
                    startActivity(intent);
                }




            }
        });


    }

    private void showToast(String text){
        Toast.makeText(SettingsActivity.this,text,Toast.LENGTH_LONG).show();
    }
}
