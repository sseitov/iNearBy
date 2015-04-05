package com.vchannel.iNearby;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import de.hdodenhof.circleimageview.CircleImageView;

import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends Activity {

    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_PICTURE = 2;

    private CircleImageView photoView;
    private EditText jabberLogin;
    private EditText jabberPassword;

    private ParseUser parseUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        photoView = (CircleImageView) findViewById(R.id.imageView);
        jabberLogin = (EditText)findViewById(R.id.login);
        jabberPassword = (EditText)findViewById(R.id.password);


        String login = (String)parseUser.get("jabber");
        jabberLogin.setText(login);
        Boolean storePassword = (Boolean)parseUser.get("storePassword");

        Switch s = (Switch)findViewById(R.id.storePassword);
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

        byte[] imageBytes = (byte[])parseUser.get("photo");
        if (imageBytes != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            photoView.setImageBitmap(bMap);
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
}
