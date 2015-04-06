package com.vchannel.iNearby;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends Activity {

    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_PICTURE = 2;

    private CircleImageView photoView;
    private EditText jabberLogin;
    private EditText jabberPassword;
    private ProgressDialog progress;

    private ParseUser parseUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        photoView = (CircleImageView) findViewById(R.id.imageView);
        byte[] imageBytes = (byte[])parseUser.get("photo");
        if (imageBytes != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            photoView.setImageBitmap(bMap);
        }

        jabberLogin = (EditText)findViewById(R.id.login);
        Button connectButton = (Button)findViewById(R.id.loginButton);
        Button disconnectButton = (Button)findViewById(R.id.disconnectButton);
        jabberPassword = (EditText)findViewById(R.id.password);
        Switch s = (Switch)findViewById(R.id.storePassword);

        if (MainActivity.connection != null) {
            ((LinearLayout)jabberPassword.getParent()).removeView(jabberPassword);
            ((LinearLayout)s.getParent()).removeView(s);

            String login = (String)parseUser.get("displayName");
            jabberLogin.setHint("Display Name");
            jabberLogin.setText(login);

            connectButton.setText("Update vCard");
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadVCard();
                }
            });
            disconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DisconnectFromXmpp().execute();
                }
            });
        } else {
            ((LinearLayout)disconnectButton.getParent()).removeView(disconnectButton);

            String login = (String)parseUser.get("jabber");
            jabberLogin.setText(login);
            Boolean storePassword = (Boolean)parseUser.get("storePassword");

            s.setChecked(storePassword);
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    parseUser.put("storePassword", isChecked);
                }
            });

            if (storePassword) {
                String password = (String)parseUser.get("jabberPassword");
                jabberPassword.setText(password);
            }


            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.photo:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_PHOTO);
                return true;
            case R.id.picture:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void printMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void login()
    {
        progress = ProgressDialog.show(this, "Connect to Jabber", "Please wait...", true, false);
        new ConnectToXmpp().execute();
    }

    private void errorLogin()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error connect");
        builder.setMessage("Check your login/password.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void uploadVCard()
    {
        progress = ProgressDialog.show(this, "Upload vCard", "Please wait...", true, false);
        new UploadCard().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_PHOTO) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_PICTURE) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(filePath);
            }
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();
                parseUser.put("photo", byteArray);
                parseUser.saveInBackground();
            }
        }
    }

    private class UploadCard  extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            VCard myCard = new VCard();
            try {
                myCard.load(MainActivity.connection);
                String nickName = jabberLogin.getText().toString();
                if (nickName != null) {
                    myCard.setNickName(nickName);
                    parseUser.put("displayName", nickName);
                }
                byte[] imageBytes = (byte[])parseUser.get("photo");
                if (imageBytes != null) {
                    myCard.setAvatar(imageBytes);
                }
                myCard.save(MainActivity.connection);
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            parseUser.saveInBackground();
            progress.dismiss();
        }

    }

    private class DisconnectFromXmpp extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                MainActivity.connection.disconnect();
            } catch (Exception e) {
            } finally {
                MainActivity.connection = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finish();
        }
    }

    private class ConnectToXmpp extends AsyncTask<Void, Void, Void> {

        private String[] jid;
        private String password;

        @Override
        protected void onPreExecute() {
            jid = jabberLogin.getText().toString().split("@");
            password = jabberPassword.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            MainActivity.connection = new XMPPTCPConnection(jid[1]);
            try {
                MainActivity.connection.connect();
                MainActivity.connection.login(jid[0], password);
                Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
            } catch (Exception e) {
                MainActivity.connection = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            if (MainActivity.connection != null) {
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                installation.put("jabber", jid[0]+"@"+jid[1]);
                installation.saveInBackground();
                finish();
            } else {
                errorLogin();
            }
        }
    };
}
