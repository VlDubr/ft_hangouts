package ru.doubr.ft_hangouts.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ru.doubr.ft_hangouts.R;
import ru.doubr.ft_hangouts.data.Contact;
import ru.doubr.ft_hangouts.data.Message;
import ru.doubr.ft_hangouts.data.messagesDB.DateTypeConverter;

import static ru.doubr.ft_hangouts.activities.MainActivity.COLOR;
import static ru.doubr.ft_hangouts.activities.MainActivity.messagesDAO;

public class SendMessagesActivity extends AppCompatActivity {

    private LinearLayout messagesLL;
    private EditText messageText;
    private int contactID;
    private Contact contact;
    private ArrayList<Message> dialog;

    public static boolean onPause = false;
    public static String pauseDate = null;

    SmsManager smsManager;
    private static final int PERMISSION_SEND_AND_RECEIVE_SMS = 1;

    private IntentFilter intentFilter;

    private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String number = extras.getString("number");
            Contact c = MainActivity.getContactByNumber(number);
            if (c == null) {
                c = new Contact(number, "", number);
                MainActivity.contactsArrayList.add(c);
                MainActivity.contactDAO.insert(c);
            }
            Message m = new Message(c.getId(), extras.getString("message"), new Date(), 1);
            messagesDAO.insert(m);
            dialog.add(m);
            displayMessage(m);
            onResume();
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(intentReceiver, intentFilter);

        MainActivity.onPause = false;
        DisplayContactActivity.onPause = false;
        CreateContactActivity.onPause = false;
        if (onPause) {
            Toast.makeText(getApplicationContext(), pauseDate, Toast.LENGTH_LONG).show();
            onPause = false;
        }

        dialog = getAllMessagesFromDBSorted();
        messagesLL.removeAllViews();
        displayAllMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_messages);
        setTitle(R.string.messages);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(COLOR));

        messagesLL = findViewById(R.id.messagesLinearLayout);
        messageText = findViewById(R.id.messageEditText);

        Bundle extras = getIntent().getExtras();
        contactID = extras.getInt("contactID");
        contact = MainActivity.getContactById(contactID);
        if (contact == null)
            finish();
        dialog = getAllMessagesFromDBSorted();
        smsManager = SmsManager.getDefault();
        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");
        displayAllMessages();
    }


    @Override
    public void onPause() {
        super.onPause();
        onPause = true;
        pauseDate = DateTypeConverter.fromDate(new Date());
        unregisterReceiver(intentReceiver);
    }


    private void displayAllMessages() {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (190 * scale + 0.5f); //190dp

        for (Message m : dialog) {
            if (m.getIsFrom() == 1) {
                messagesLL.addView(getMessageFromTextView(pixels, m.getMessage()));
            }
            else {
                messagesLL.addView(getMessageToTextView(pixels, m.getMessage()));
            }
        }
    }

    private void displayMessage(Message m) {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (190 * scale + 0.5f); //190dp
        if (m.getIsFrom() == 1) {
            messagesLL.addView(getMessageFromTextView(pixels, m.getMessage()));
        }
        else {
            messagesLL.addView(getMessageToTextView(pixels, m.getMessage()));
        }
    }

    private ArrayList<Message> getAllMessagesFromDBSorted(){
        ArrayList<Message> messages = (ArrayList<Message>) messagesDAO.getDialogByContactId(contactID);
        Collections.sort(messages);
        return messages;
    }



    private TextView getMessageFromTextView(int pixels, String text) {
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(pixels, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageParams.gravity = Gravity.LEFT;
        TextView messageFrom = new TextView(this);
        messageFrom.setLayoutParams(messageParams);
        messageFrom.setText(text);
        messageFrom.setTextSize(14);
        messageFrom.setBackgroundColor(Color.parseColor("#00BCD4"));
        return messageFrom;
    }

    private TextView getMessageToTextView(int pixels, String text) {
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(pixels, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageParams.gravity = Gravity.RIGHT;
        TextView messageTo = new TextView(this);
        messageTo.setLayoutParams(messageParams);
        messageTo.setText(text);
        messageTo.setTextSize(14);
        messageTo.setBackgroundColor(Color.parseColor("#8BC34A"));
        return messageTo;
    }

    public void sendMessage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == -1) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, PERMISSION_SEND_AND_RECEIVE_SMS);
            return;
        }
        String SENT="SMS_SENT";
        String DELIVERED="SMS_DELIVERED";

        PendingIntent sentPI= PendingIntent.getBroadcast(this,0,
                new Intent(SENT),0);

        PendingIntent deliveredPI= PendingIntent.getBroadcast(this,0,
                new Intent(DELIVERED),0);

//---когда SMS отправлено---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1){
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(),R.string.sms_sent,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(),R.string.generic_failure,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(),R.string.no_service,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(),R.string.null_pdu,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(),R.string.radio_off,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        },new IntentFilter(SENT));

//---когда SMS доставлено---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1){
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(),R.string.sms_delivered,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(),R.string.sms_not_delivered,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        },new IntentFilter(DELIVERED));


        smsManager.sendTextMessage(contact.getPhone(), null, messageText.getText().toString(), sentPI, deliveredPI);
        Message m = new Message(contactID, messageText.getText().toString(), new Date(), 0);
        dialog.add(m);
        messagesDAO.insert(m);
        displayMessage(m);
        messageText.setText("");
    }
}