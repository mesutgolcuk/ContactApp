package com.mesutgolcuk.rehberuygulamasi;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Customized list view adapter
 */
public class ContactAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;

    private List<Contact> list;
    private ArrayList<Contact> al;

    public ContactAdapter(Context context, List<Contact> list) {
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        this.list = list;
        this.al = new ArrayList<>();
        al.addAll(list);
    }


    /**
     * References of widgets
     */
    public static class ViewHolder {
        ImageView pic;
        TextView name;
        ImageView location;
        ImageView sms;
        ImageView call;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Contact getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        holder = new ViewHolder();

        if (convertView == null) {
            // matching
            convertView = inflater.inflate(R.layout.contact_layout, null);
            holder.name = (TextView) convertView.findViewById(R.id.contactName);
            holder.pic = (ImageView) convertView.findViewById(R.id.contactPicture);
            holder.location = (ImageView) convertView.findViewById(R.id.locationButton);
            holder.sms = (ImageView) convertView.findViewById(R.id.smsButton);
            holder.call = (ImageView) convertView.findViewById(R.id.callButton);
            convertView.setTag(holder);

            // location button onClick
            holder.location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    // multiple addresses
                    if( list.get(position).getLocations().size() > 1 ){
                        CharSequence locations[] = list.get(position).getLocations().toArray(
                                new CharSequence[list.get(position).getLocations().size()]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Pick a location");
                        builder.setItems(locations, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onClickLocation(list.get(position).getLocations().get(which));
                            }
                        });
                        builder.show();
                    }
                    else{
                        onClickLocation(list.get(position).getLocations().get(0));
                    }

                }
            });
            // sms button on click
            holder.sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    // multiple numbers
                    if( list.get(position).getPhoneNumbers().size() > 1) {
                        CharSequence locations[] = list.get(position).getPhoneNumbers().toArray(
                                new CharSequence[list.get(position).getPhoneNumbers().size()]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Pick a number");
                        builder.setItems(locations, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onClickSms(list.get(position).getPhoneNumbers().get(which));
                            }
                        });
                        builder.show();
                    }
                    else{
                        onClickSms(list.get(position).getPhoneNumbers().get(0));
                    }

                }
            });
            // call button click
            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    // multiple numbers
                    if( list.get(position).getPhoneNumbers().size() > 1) {
                        CharSequence locations[] = list.get(position).getPhoneNumbers().toArray(
                                new CharSequence[list.get(position).getPhoneNumbers().size()]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Pick a number");
                        builder.setItems(locations, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onClickCall(list.get(position).getPhoneNumbers().get(which));
                            }
                        });
                        builder.show();
                    }
                    else{
                        onClickCall(list.get(position).getPhoneNumbers().get(0));
                    }
                }
            });

        }
        // viewHolder != null
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        // filling list
        holder.name.setText(al.get(position).getName());
        holder.pic.setImageBitmap(al.get(position).getPhoto());

        return convertView;
    }

    /**
     * filtering listview according to name and phone number
     * @param charText
     * @return
     */
    public List<Contact> getFilter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        list.clear();
        if (charText.length() == 0) {
            list.addAll(al);
        } else {
            for (Contact wp : al) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getPhoneNumbers().get(0).contains(charText)) {
                    list.add(wp);
                }
            }
        }
        return list;
    }


    /**
     * Location button clicked
     * @param addr location of person
     */
    public void onClickLocation(String addr) {
        Log.i(LOGGER.LOG_BUTTONS, "loc->");
        // Contact doesnt have location
        if( addr.equalsIgnoreCase("") ){
            Toast.makeText(mContext,"Contact doesn't have a location information",Toast.LENGTH_SHORT).show();
        }
        // Get route to location
        else{
            String map = "http://maps.google.co.in/maps?daddr=" + addr;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);

        }

    }

    /**
     * Send sms button clicked
     * @param number phone number
     */
    public void onClickSms(String number) {
        Log.i(LOGGER.LOG_BUTTONS, "sms->" + number);
        // ask sms permission to user
        MainActivity.permissionSms(mContext);
        Uri uri = Uri.parse("smsto:"+number);
        Intent smsIntent = new Intent(Intent.ACTION_VIEW,uri);
        // if permission granted start sms app
        if(ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED){
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(smsIntent);
        }
    }

    /**
     * CAll button clicked
     * @param number phone number
     */
    public void onClickCall(String number) {
        Log.i(LOGGER.LOG_BUTTONS, "call->" + number);
        // ask call permission
        MainActivity.permissionCall(mContext);
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        // if permission granted call phone number
        if(ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED){
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(callIntent);

        }
    }

    /**
     * delete contact from list
     * @param position
     */
    public void deleteContact(int position){
        list.remove(position);
    }

}