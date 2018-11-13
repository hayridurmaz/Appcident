package tr.edu.tedu.appcident;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String phoneNumber1;
    String phoneNumber2;
    String phoneNumber3;
    Button submitButton;
    EditText phoneInput1;
    EditText phoneInput2;
    EditText phoneInput3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneInput1 = (EditText) findViewById(R.id.phoneInput1);
        phoneInput2 = (EditText) findViewById(R.id.phoneInput2);
        phoneInput3 = (EditText) findViewById(R.id.phoneInput3);

        submitButton = (Button) findViewById(R.id.submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber1 = phoneInput1.getText().toString();
                if(phoneInput1.getText().length()!=11){
                    phoneInput1.setError("Please enter valid number");
                }else {
                    phoneNumber1 = phoneInput1.getText().toString();
                }
                phoneNumber2 =phoneInput2.getText().toString();
                if(phoneInput2.getText().length()!=11){
                    phoneInput2.setError("Please enter valid number");
                }else {
                    phoneNumber2 = phoneInput2.getText().toString();
                }
                phoneNumber3 = phoneInput3.getText().toString();
                if(phoneInput3.getText().length()!=11){
                    phoneInput3.setError("Please enter valid number");
                }else {
                    phoneNumber3 = phoneInput3.getText().toString();
                }
                if(phoneNumber3.length()==11&&phoneNumber2.length()==11&&phoneNumber1.length()==11){
                    showToast("Now your emergency list is ready!!");
                }
            }
        });


    }

    private void showToast(String text){
        Toast.makeText(MainActivity.this,text,Toast.LENGTH_LONG).show();
    }
}

