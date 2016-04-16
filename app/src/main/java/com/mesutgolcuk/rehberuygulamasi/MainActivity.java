package com.mesutgolcuk.rehberuygulamasi;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    private List<Contact> contacts;
    private ListView contactList;
    private EditText inputSearch;
    private Button informationButton;
    // permission variables
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=0;
    private static final int MY_PERMISSIONS_CALL=1;
    private static final int MY_PERMISSIONS_SMS=2;
    private static final int MY_PERMISSIONS_WRITE_CONTACTS =3 ;
    private static final int MY_PERMISSIONS_SMS_READ =4 ;
    private static final int MY_PERMISSIONS_PHONE_STATE =5 ;
    private static final int MY_PERMISSIONS_CALL_LOG =6 ;
    private static final int MY_PERMISSIONS_RECEIVE_SMS =7 ;

    ContactAdapter adapter;
    ContactAdapter tmpAdapter;
    ContactsProvider cp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        contactList = (ListView) findViewById(R.id.contactList);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        informationButton = (Button) findViewById(R.id.informationButton);

        readContacts();

    }

    /**
     * Contact read request granted
     */
    public void requestGranted(){
        // ask sms permission
        permissionReceiveSms(this);

        cp = new ContactsProvider(this);
        contacts.addAll(cp.getContacts());
        Collections.sort(contacts);
        // list view on click
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                permissionCallLog(instance);
                Log.i(LOGGER.LOG_BUTTONS, contacts.get(position).getName() + "->" + contacts.get(position).getPhoneNumbers());
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_CALL_LOG)
                        == PackageManager.PERMISSION_GRANTED) {
                    // get contact call data
                    cp.assignCallInformations();
                    contacts = new ArrayList<>();
                    contacts.addAll(cp.getContactList());
                    Collections.sort(contacts);
                    // read receive sms
                    SharedPreferences prefs = getSharedPreferences("smsPref",
                            Context.MODE_PRIVATE);
                    contacts.get(position).getRelation().setReceivedSms(
                            prefs.getInt(contacts.get(position).getName(),0));

                    String s0 = "Incoming call : " +tmpAdapter.getItem(position).getRelation().getIncomingCalls();
                    String s1 = "Incoming call duration : " + String.format("%.2f", tmpAdapter.getItem(position).getRelation().getIncomingDuration()) + " min";
                    String s2 = "Outgoing call : " + tmpAdapter.getItem(position).getRelation().getOutgoingCalls();
                    String s3 = "Outgoing call duration : " + String.format("%.2f", tmpAdapter.getItem(position).getRelation().getOutgoingDuration()) + " min";
                    String s4 = "Miss call : " + tmpAdapter.getItem(position).getRelation().getMissCalls();
                    String s5 = "Receive sms : " + tmpAdapter.getItem(position).getRelation().getReceivedSms();
                    CharSequence[] cs = {s0, s1, s2, s3, s4, s5};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("INFORMATION");
                    builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }


            }
        });
        // list view initialize
        adapter = new ContactAdapter(this, contacts);
        contactList.setAdapter(adapter);
        tmpAdapter = adapter;
        // list view on long click listenr
        contactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i(LOGGER.LOG_BUTTONS, "Long Clicked : " + contacts.get(position).getName());

                CharSequence options[] = new CharSequence[]{"Call", "Send SMS", "Edit", "Delete"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose an option");

                final ContactAdapter.ViewHolder finalHolder = (ContactAdapter.ViewHolder) view.getTag();
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                finalHolder.call.callOnClick();
                                break;
                            }
                            case 1: {
                                finalHolder.sms.callOnClick();
                                break;
                            }
                            case 2: {
                                Intent i = new Intent(Intent.ACTION_EDIT);
                                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                                        Long.parseLong(contacts.get(position).getId()));
                                i.setData(contactUri);
                                startActivity(i);
                                break;
                            }
                            case 3: {
                                permissionDeleteContact(getContext());
                                cp.deleteContact(finalHolder.name.getText().toString());

                                adapter.deleteContact(position);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
                builder.show();

                return true;
            }
        });
        // Search via edit text
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = inputSearch.getText().toString().toLowerCase(Locale.getDefault());
                contacts = adapter.getFilter(text);
                tmpAdapter = new ContactAdapter(instance, contacts);
                contactList.setAdapter(tmpAdapter);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
            }
        });

    }

    /**
     * read contact ask for permision
     */
    public void readContacts(){
        contacts = new ArrayList<>();
        requestPermission();
    }

    /**
     * get context of main activity
     * @return
     */
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    /**
     * floating button clicked
     * @param v
     */
    public void onClickFloating(View v) {
        Intent intent = new Intent(Intent.ACTION_INSERT,
                ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivity(intent);
        //finish();
    }
    /**
     * Information Button Clicked
     * @param v
     */
    public void informationClicked(View v) {
        permissionCallLog(this);
        Log.i(LOGGER.LOG_BUTTONS, "info clicked");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED){
            cp.assignCallInformations();
            contacts = new ArrayList<>();
            contacts.addAll(cp.getContactList());
            Collections.sort(contacts);
            String s0 = "Total incoming call duration : " + String.format("%.2f", findTotalIncoming());
            String s1 = "Total outgoing call duration : " + String.format("%.2f", findTotalOutgoing());
            String s2 = "Max missed call person : " + findMaxMissed();
            String s3 = "Max incoming call person : " + findMaxIncoming();
            String s4 = "Max outgoing call person : " + findMaxOutgoing();
            String s5 = "Max total call person : " + findMaxTotal();
            CharSequence[] cs = {s0, s1, s2, s3, s4, s5};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("INFORMATION");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setItems(cs, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
        else{
            Toast.makeText(this,"Needs permission",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * finds total incoming call duration
     * @return total incoming call duration
     */
    public double findTotalIncoming(){
        double total=0;
        for (Contact each : contacts){
            total += each.getRelation().getIncomingDuration();
        }
        return total;
    }

    /**
     * finds total outgoing call duration
     * @return total outgoing call duration
     */
    public double findTotalOutgoing(){
        double total=0;
        for (Contact each : contacts){
            total += each.getRelation().getOutgoingDuration();
        }
        return total;
    }

    /**
     * find the person who has max missed call
     * @return person who has max missed  call
     */
    public String findMaxMissed(){
        int max;
        int maxIndex;
        max = contacts.get(0).getRelation().getMissCalls();
        maxIndex = 0;
        for( int i=1;i<contacts.size();i++){
            if(contacts.get(i).getRelation().getMissCalls() > max ){
                max = contacts.get(i).getRelation().getMissCalls();
                maxIndex = i;
            }
        }
        return contacts.get(maxIndex).getName();
    }

    /**
     * find the person who has max incoming call
     * @return person who has max incoming call
     */
    public String findMaxIncoming(){
        double max;
        int maxIndex;
        max = contacts.get(0).getRelation().getIncomingDuration();
        maxIndex = 0;
        for( int i=1;i<contacts.size();i++){
            if(contacts.get(i).getRelation().getIncomingDuration() > max ){
                max = contacts.get(i).getRelation().getIncomingDuration();
                maxIndex = i;
            }
        }
        return contacts.get(maxIndex).getName();
    }

    /**
     * find the person who has max outgoing call
     * @return person who has max outgoing call
     */
    public String findMaxOutgoing(){
        double max;
        int maxIndex;
        max = contacts.get(0).getRelation().getOutgoingDuration();
        maxIndex = 0;
        for( int i=1;i<contacts.size();i++){
            if(contacts.get(i).getRelation().getOutgoingDuration() > max ){
                max = contacts.get(i).getRelation().getOutgoingDuration();
                maxIndex = i;
            }
        }
        return contacts.get(maxIndex).getName();
    }

    /**
     * find the person max call
     * @return max call
     */
    public String findMaxTotal(){
        double max;
        int maxIndex;
        max = contacts.get(0).getRelation().getOutgoingDuration()+contacts.get(0).getRelation().getIncomingDuration();
        maxIndex = 0;
        for( int i=1;i<contacts.size();i++){
            double n = contacts.get(i).getRelation().getOutgoingDuration()+contacts.get(i).getRelation().getIncomingDuration();
            if(n > max ){
                max = n;
                maxIndex = i;
            }
        }
        return contacts.get(maxIndex).getName();
    }

    /**
     * ask for call permission
     * @param c
     */
    public static void permissionCall(Context c){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_CALL);
        }

    }

    /**
     * ask for sms permission
     * @param c
     */
    public static void permissionSms(Context c){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_SMS);
        }
    }

    /**
     * ask for write contact permission
     * @param c
     */
    public static void permissionDeleteContact(Context c){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.WRITE_CONTACTS},
                    MY_PERMISSIONS_WRITE_CONTACTS);
        }
    }

    /**
     * ask sms read permission
     * @param c
     */
    public static void permissionSmsRead(Context c ){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.READ_SMS},
                    MY_PERMISSIONS_SMS_READ);
        }
    }

    /**
     * ask call permission
     * @param c
     */
    public static void permissionCallLog(Context c){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    MY_PERMISSIONS_CALL_LOG);

        }

    }

    /**
     * ask receive sms permission
     * @param c
     */
    public static void permissionReceiveSms(Context c){
        if (ContextCompat.checkSelfPermission(c,
                Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions((Activity) c,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_CALL_LOG);

        }
    }

    /**
     * Permission not granted
     */
    public void requestDenial(){
        Toast.makeText(this,"Some features of the app may not work",Toast.LENGTH_SHORT).show();
    }

    /**
     * Ask contact read permission
     */
    public void requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }
        else{
            requestGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            // contact read permisison
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    requestGranted();

                } else {
                    requestDenial();
                }
                break;
            }
            // call permission
            case MY_PERMISSIONS_CALL : {
                // granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Call permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"SMS permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_SMS_READ: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_CALL_LOG: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_WRITE_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Contacts permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }
            case MY_PERMISSIONS_RECEIVE_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Contacts permission granted",Toast.LENGTH_SHORT).show();

                } else {
                    requestDenial();
                }
                break;
            }

        }
    }


}