package com.mesutgolcuk.rehberuygulamasi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Catches the new sms
 */
public class SmsReceiver extends BroadcastReceiver {

    final SmsManager sms = SmsManager.getDefault();
    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i(LOGGER.LOG_BROADCAST, "senderNum: " + phoneNumber + "; message: " + message);
                    // finds the contact name associated with that phone number
                    Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    Cursor c = context.getContentResolver().query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME},null,null,null);
                    try {
                        c.moveToFirst();
                        String  name = c.getString(0);
                        Toast.makeText(context,"Sender: "+ name+ " m: "+message, Toast.LENGTH_LONG).show();
                        // gets received sms number for name
                        SharedPreferences prefs = context.getSharedPreferences("smsPref",
                                Context.MODE_PRIVATE);
                        int smsNum = prefs.getInt(name, 0);
                        // increase received sms number
                        smsNum++;
                        SharedPreferences.Editor editor = prefs.edit();
                        // write back
                        editor.putInt(name, smsNum);
                        editor.commit();

                    } catch (Exception e) {
                        Log.e(LOGGER.LOG_BROADCAST,e.getStackTrace().toString());
                    }finally{
                        c.close();
                    }

                }
            }
        } catch (Exception e) {
            Log.e(LOGGER.LOG_BROADCAST, "Exception smsReceiver" +e);

        }
    }
}
