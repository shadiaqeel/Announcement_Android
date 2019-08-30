package com.pa.announcement;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {


    private  AutoCompleteTextView username ;
    private EditText pass;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



         username = (AutoCompleteTextView)findViewById(R.id.email);
         pass =(EditText) findViewById(R.id.password);
        final Button sign_in = (Button) findViewById(R.id.email_sign_in_button);



        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!(username.getText().toString().equals("admin")))
                    Toast.makeText(LoginActivity.this,"this username is incorrect ", Toast.LENGTH_SHORT).show();
                else
                {if(!(pass.getText().toString().equals("123")))
                    Toast.makeText(LoginActivity.this,"this password is incorrect ", Toast.LENGTH_SHORT).show();
                    else
                        {
                            Intent intent = new Intent( LoginActivity.this , MainActivity.class);
                            startActivity(intent);
                            finish();


                        }





                }



            }
        });







    }
}
