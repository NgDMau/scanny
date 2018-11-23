package com.example.andjm.scanny;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private TextView noti_top;
    private TextView noti_bottom;
    private EditText username_field;
    private EditText password_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        noti_top       = findViewById(R.id.status_bar);
        noti_bottom    = findViewById(R.id.noti_bottom);
        username_field = findViewById(R.id.username_field);
        password_field = findViewById(R.id.password_field);


    }

    public void loginButtonPressed(View view){
        noti_top.setText("");
        noti_bottom.setText("");
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        if(username.equals("") && password.equals("")){
            //do something here for correct information.
            //Intent mIntent = new Intent(this, CentralActivity.class);
            Intent mIntent = new Intent(this, CentralActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putString("username","doixanh");
            mIntent.putExtras(mBundle);
            this.startActivity(mIntent);

        }else{
            noti_top.setText("Wrong password or username, try again!");
        }
    }

    public void signupButtonPressed(View view){
        //do something here...

    }
}
