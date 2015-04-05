package com.vchannel.iNearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {

    public static final String TAG = "iNearBy";

    private static final int REQUEST_SIGNUP    = 0;
    private static final int REQUEST_PROFILE   = 1;
    private static final int REQUEST_ADD_CONTACT   = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_contact:
            {
                Intent intent = new Intent(this, AddContactActivity.class);
                startActivityForResult(intent, REQUEST_ADD_CONTACT);
            }
                return true;
            case R.id.action_settings:
            {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);

            }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
