package com.vchannel.iNearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class ChatActivity extends Activity {

    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_PICTURE = 2;

    private String peer;
    private EditText messageView;

    private ParseUser parseUser = ParseUser.getCurrentUser();

    private class DateMessage extends Message {

        @Override
        protected void addCommonAttributes(XmlStringBuilder xml) {
            super.addCommonAttributes(xml);
            long t = System.currentTimeMillis() / 1000;
            xml.optAttribute("date", ""+t);
            xml.optAttribute("to", peer);
            xml.optAttribute("from", (String)parseUser.get("jabber"));
            xml.optAttribute("xmlns", "jabber:client");
            xml.optAttribute("type", "chat");
        }
    };

    void receiveMessage(final Message message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message.getBody(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra("name"));
        peer = intent.getStringExtra("address");

        ChatManager chatManager = ChatManager.getInstanceFor(MainActivity.connection);
        chatManager.createChat(peer, new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                receiveMessage(message);
            }
        });
        messageView = (EditText)findViewById(R.id.messageText);
        Button btn = (Button)findViewById(R.id.sendButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    DateMessage message = new DateMessage();
                    message.setBody(messageView.getText().toString());
                    MainActivity.connection.sendPacket(message);
                } catch (Exception e) {

                } finally {
                    messageView.getText().clear();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_photo:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_PHOTO);
                return true;
            case R.id.action_send_picture:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
