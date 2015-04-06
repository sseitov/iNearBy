package com.vchannel.iNearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseUser;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;

import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Activity {

    public static final String TAG = "iNear";

    private static final int REQUEST_SIGNUP    = 0;
    private static final int REQUEST_PROFILE   = 1;
    private static final int REQUEST_ADD_CONTACT   = 2;

    public static XMPPConnection connection;
    public static Roster roster;

    ListView contactListView;
    RosterAdapter rosterAdapter;

    public class RosterAdapter extends ArrayAdapter<VCard> {
        private final Context context;

        public RosterAdapter(Context context) {
            super(context, R.layout.list_layout);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_layout, parent, false);
            VCard entry = getItem(position);

            TextView title = (TextView) rowView.findViewById(R.id.nickName);
            title.setText(entry.getNickName());
            TextView details = (TextView) rowView.findViewById(R.id.userJID);
            details.setText(entry.getJabberId());

            CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.userIcon);
            byte[] imageBytes = entry.getAvatar();
            if (imageBytes != null) {
                Bitmap bMap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bMap);
            }

            return rowView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactListView = (ListView)findViewById(R.id.contactList);
        rosterAdapter = new RosterAdapter((Context)this);
        contactListView.setAdapter(rosterAdapter);

        Parse.initialize(this, "Azz7OQsCDOQNp1Fjw7JbzXRxg1qhOcnWgFxUzYty", "utsSMDqCgOy8IPgTIaL0OefzBtrz8ajMNRPtlSHL");

        SmackAndroid.init(this);
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
        } else if (connection == null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivityForResult(intent, REQUEST_PROFILE);
        }
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
                startActivityForResult(intent, REQUEST_PROFILE);

            }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROFILE) {
            if (connection != null) {
                roster = connection.getRoster();
                rosterAdapter.clear();
                for (Iterator iterator = roster.getEntries().iterator(); iterator.hasNext();) {
                    RosterEntry entry = (RosterEntry) iterator.next();
                    VCard vCard = new VCard();
                    try {
                        vCard.load(connection, entry.getUser());
                        vCard.setJabberId(entry.getUser());
                        rosterAdapter.add(vCard);
                    } catch (Exception e) {
                    }
                }
            } else {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivityForResult(intent, REQUEST_PROFILE);
            }
        }
    }

}
