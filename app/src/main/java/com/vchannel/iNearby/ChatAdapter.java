package com.vchannel.iNearby;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sseitov on 07.04.15.
 */
public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private LinearLayout singleMessageContainer;

    public ChatAdapter(Context context, int resourceId) {
        super(context, resourceId);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatMessage chatMessageObj = getItem(position);
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatMessageObj.fromMe) {
                row = inflater.inflate(R.layout.message_layout_right, parent, false);
            } else {
                row = inflater.inflate(R.layout.message_layout_left, parent, false);
            }
        }
        singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);
        chatText = (TextView) row.findViewById(R.id.singleMessage);
        chatText.setText(chatMessageObj.message);
        chatText.setBackgroundResource(chatMessageObj.fromMe ? R.drawable.bubble_right : R.drawable.bubble_left);
        singleMessageContainer.setGravity(chatMessageObj.fromMe ? Gravity.RIGHT : Gravity.LEFT);
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
