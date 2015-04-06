package com.vchannel.iNearby;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {

    private EditText email;
    private EditText password;
    private ProgressDialog progress;

    private void printMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void startProgress(String message)
    {
        progress = ProgressDialog.show(this, message, "Please wait...", true, false);
    }

    private void successSignUp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SignUp success!");
        builder.setMessage("Check your mail and confirm registration. After confirmation you can connect to Jabber.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = (EditText)findViewById(R.id.iNearLogin);
        password = (EditText)findViewById(R.id.iNearPassword);


        Button signUpButton = (Button)findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgress("SignUp");
                ParseUser.logInInBackground(email.getText().toString(), password.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            progress.dismiss();
                            finish();
                        } else {
                            parseUser = new ParseUser();
                            parseUser.setUsername(email.getText().toString());
                            parseUser.setPassword(password.getText().toString());
                            parseUser.setEmail(email.getText().toString());
                            parseUser.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    progress.dismiss();
                                    if (e == null) {
                                        successSignUp();
                                    } else {
                                        printMessage("Error SignUp: " + e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        Button resetPasswordButton = (Button)findViewById(R.id.resetPasswordButton);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgress("Request password");
                ParseUser.requestPasswordResetInBackground(email.getText().toString(), new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        progress.dismiss();
                        if (e == null) {
                            printMessage("An email was successfully sent with reset instructions.");
                        } else {
                            printMessage("Error: "+e.getLocalizedMessage());
                        }
                    }
                });
            }
        });
    }
}
