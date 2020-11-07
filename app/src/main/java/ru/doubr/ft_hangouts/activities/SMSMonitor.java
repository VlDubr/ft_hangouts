package ru.doubr.ft_hangouts.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSMonitor extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle myBundle = intent.getExtras();
        SmsMessage [] messages = null;
        StringBuilder strNumber = new StringBuilder();
        StringBuilder strMessage = new StringBuilder();

        if (myBundle != null)
        {
            Object [] pdus = (Object[]) myBundle.get("pdus");

            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++)
            {
                String format = myBundle.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                strNumber.append(messages[i].getOriginatingAddress());
                strMessage.append(messages[i].getMessageBody());
            }

            Toast.makeText(context, "Message from: " + strNumber, Toast.LENGTH_SHORT).show();
            Intent broadcastReceiver = new Intent();
            broadcastReceiver.setAction("SMS_RECEIVED_ACTION");
            broadcastReceiver.putExtra("number", strNumber.toString());
            broadcastReceiver.putExtra("message", strMessage.toString());
            context.sendBroadcast(broadcastReceiver);
            abortBroadcast();
        }
    }
}