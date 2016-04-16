package com.mesutgolcuk.rehberuygulamasi;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides contact informations
 */
public class ContactsProvider {

    private Uri QUERY_URI = ContactsContract.Contacts.CONTENT_URI;
    private String CONTACT_ID = ContactsContract.Contacts._ID;
    private String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private Uri EMAIL_CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    private String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private ContentResolver contentResolver;
    private Context c;

    List<Contact> contactList;

    public ContactsProvider(Context c) {
        this.c=c;
        contentResolver = MainActivity.getContext().getContentResolver();
    }

    /**
     * get All contact informations
     * @return
     */
    public List<Contact> getContacts() {
        contactList = new ArrayList<Contact>();
        String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        String selection = null;
        Cursor cursor = contentResolver.query(QUERY_URI, projection, selection, null, null);

        while (cursor.moveToNext()) {
            // find all contacts
            Contact contact = getContact(cursor);
            // if contact has a name add to list
            if(contact.getName() != null && contact.getPhoneNumbers().size() != 0)
                contactList.add(contact);
        }

        cursor.close();

        return contactList;
    }

    /**
     * Get each contact by ıne by with all informations
     * @param cursor
     * @return contact
     */
    private Contact getContact(Cursor cursor) {
        String contactId = cursor.getString(cursor.getColumnIndex(CONTACT_ID));
        String name = (cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));

        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName(name);
        // get phone and e mail of contact
        getPhone(cursor, contactId, contact);
        getEmail(contactId, contact);
        // get photo of contact if it s null assign default photo
        Bitmap photo = loadContactPhoto(contentResolver, contactId);
        if(photo!=null)
            contact.setPhoto(photo);
        else{
            Bitmap bitmap= BitmapFactory.decodeResource(c.getResources(),
                    R.drawable.contact_icon);
            contact.setPhoto(bitmap);
        }
        // get locations of contact
        getAddress(contactId, contact);

        return contact;
    }


    /**
     * get e mail information of contact
     * @param contactId id of contact
     * @param contact
     */
    private void getEmail(String contactId, Contact contact) {
        Cursor emailCursor = contentResolver.query(EMAIL_CONTENT_URI, null, EMAIL_CONTACT_ID +
                " = ?", new String[]{contactId}, null);
        while (emailCursor.moveToNext()) {
            String email = emailCursor.getString(emailCursor.getColumnIndex(EMAIL_DATA));
            if (!TextUtils.isEmpty(email)) {
                contact.setEmail(email);
            }
        }
        emailCursor.close();
    }

    /**
     * get phone number of contact
     * @param cursor
     * @param contactId
     * @param contact
     */
    private void getPhone(Cursor cursor, String contactId, Contact contact) {
        int hasPhoneNumber = Integer.parseInt(cursor.getString(
                cursor.getColumnIndex(HAS_PHONE_NUMBER)));
        // if contact has phone number
        if (hasPhoneNumber > 0) {
            Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID +
                                                            " = ?", new String[]{contactId}, null);
            // if contact has multiple phone number
            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_NUMBER));
                if(contact.getPhoneNumbers().size() == 0 ||
                                !contact.getPhoneNumbers().get(0).equalsIgnoreCase(phoneNumber) ){
                    contact.addPhoneNumber(phoneNumber);
                }
            }
            phoneCursor.close();
        }
    }

    /**
     * get photo of contact
     * @param cr content resolver
     * @param id id of contact
     * @return bitmap photo
     */
    public static Bitmap loadContactPhoto(ContentResolver cr, String id) {
        Uri uri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, Long.valueOf(id));
        // open input stream to get photo
        InputStream input = ContactsContract.Contacts
                .openContactPhotoInputStream(cr, uri);

        if (input == null) {
            return null;
        }
        Bitmap b = BitmapFactory.decodeStream(input);
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * get location information of contact
     * @param contactId contact id
     * @param contact
     */
    public void getAddress(String contactId,Contact contact){
        String postalCat="";
        Cursor postals = contentResolver.query(
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = "
                        + contactId, null, null);

        if( postals != null ){
            int postFormattedNdx = postals.getColumnIndex(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
            // if has one address info
            if(!postals.moveToNext()) {
                contact.addLocation(postalCat);
            }
            postals.moveToPrevious();
            while (postals.moveToNext()) {
                String postalData = postals.getString(postFormattedNdx);
                postalCat = postalData;
                contact.addLocation(postalCat);
            }

        }
        //if cursor == null
        else{
            contact.addLocation(postalCat);
        }
        postals.close();

    }

    /**
     * delete contact from the phone
     * @param name name of the contact
     */
    public void deleteContact(String name){
        ContentResolver cr = contentResolver;
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
        String[] params = new String[] {name};

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
    /**
     * Read call log and use log to get call information
     */
    public void assignCallInformations() {
        // if app has permission to read call log
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.READ_CALL_LOG)
                                              == PackageManager.PERMISSION_GRANTED) {

            Cursor managedCursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null,
                    null, null);
            int number = managedCursor.getColumnIndex(CallLog.Calls.CACHED_MATCHED_NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            // resets each contact
            for (Contact each : contactList) {
                each.getRelation().reset();
            }
            // read call log
            while (managedCursor.moveToNext()) {
                String phoneNumber = managedCursor.getString(number);
                String callType = managedCursor.getString(type);
                String callDuration = managedCursor.getString(duration);
                String callName = managedCursor.getString(name);
                // find position of name in the list
                int position = findPosition(callName);
                // if call has contact information
                if (callName != null && position != -1) {
                    switch (Integer.parseInt(callType)) {
                        case CallLog.Calls.OUTGOING_TYPE: {
                            contactList.get(position).getRelation().incrementOutgingCalls();
                            contactList.get(position).getRelation().addOutGoingDuration(
                                    Double.parseDouble(callDuration) / 60);
                            break;
                        }
                        case CallLog.Calls.INCOMING_TYPE: {
                            contactList.get(position).getRelation().incrementIncomingCalls();
                            contactList.get(position).getRelation().addIncomingDuration(
                                    Double.parseDouble(callDuration) / 60);
                            break;
                        }
                        case CallLog.Calls.MISSED_TYPE: {
                            contactList.get(position).getRelation().incrementMissCalls();
                            break;
                        }
                    }
                    Log.i(LOGGER.LOG_BUTTONS, contactList.get(position).getName());
                }

            }
            managedCursor.close();

        }
    }

    /**
     * find position of given name in arraylist
     * @param name
     * @return position
     */
    public int findPosition( String name){
        for(int i=0; i<contactList.size();i++){
            if(contactList.get(i).equals(name))
                return i;
        }
        return -1;
    }

    public List<Contact> getContactList() {
        return contactList;
    }
}