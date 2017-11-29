package com.techjany.automaticcallrecorder.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.onedrivesdk.saver.ISaver;
import com.microsoft.onedrivesdk.saver.Saver;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.techjany.automaticcallrecorder.BuildConfig;
import com.techjany.automaticcallrecorder.MainActivity;
import com.techjany.automaticcallrecorder.R;
import com.techjany.automaticcallrecorder.SqliteDatabase.ContactsDatabase;
import com.techjany.automaticcallrecorder.SqliteDatabase.DatabaseHelper;
import com.techjany.automaticcallrecorder.pojo_classes.Contacts;
import com.techjany.automaticcallrecorder.utils.StringUtils;

/**
 * Created by sandhya on 23-Aug-17.
 */

public class ContactProvider {
    static refresh itemrefresh;
    static deleterefresh itemdelete;
    public static  void deletelistener(deleterefresh list){ itemdelete=list;}
    public static void setItemrefresh(refresh listener){
        itemrefresh=listener;
    }

    public static ArrayList<Contacts> getContacts(final Context ctx) {
         ArrayList<Contacts> list = new ArrayList<>();
         ContentResolver contentResolver = ctx.getContentResolver();
         Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                            Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                            Bitmap photo = null;
                            if (inputStream != null) {
                                photo = BitmapFactory.decodeStream(inputStream);
                            }
                            while (cursorInfo.moveToNext()) {
                                Contacts info = new Contacts();
                                info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                                info.setNumber(cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                info.setPhoto(photo);
                                info.setPhotoUri(pURI.toString());
                                list.add(info);
                            }
                            cursorInfo.close();
                        }
                    }
                }
                cursor.close();
        return list;
    }

    public static String getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        return ts;
    }

    public static String getrelative(long time) {
        long d = (System.currentTimeMillis() / 1000) - time;
        String remainingTime ="";
        if (d < 60) {
            //seconds
            remainingTime = ((((d % 31536000) % 86400) % 3600) % 60) + "s";
        } else if (d > 60 && d < 3600) {
            //in minutes
            remainingTime = Math.round((((d % 31536000) % 86400) % 3600) / 60) + "m";
        } else if (d > 3600 && d < 86400) {
            //in hours
            remainingTime = Math.round(((d % 31536000) % 86400) / 3600) + "h";
        } else if (d > 86400 && d < 31536000) {
            //in days
            remainingTime = Math.round((d % 31536000) / 86400) + "d";
        } else {
            //in years
            remainingTime = Math.round(d / 31536000) + "y";
        }
        return remainingTime;
    }
    public static long getDaileyTime(long time){
        long d = (System.currentTimeMillis() / 1000) - time;
        long returntime;
        if(d<=86400){
            //today
            returntime=1;
        }else if(d>86400&&d<172800){
            //yesterday
            returntime=2;
        }else {
            //
            returntime=time*1000; //in milisecondd
        }
        return returntime;
    }
    public static ArrayList<Contacts> getCallList(Context ctx, ArrayList<String> recording, String type) {
        ArrayList<Contacts> allContactList = new ArrayList<>();
        ContactsDatabase database=new ContactsDatabase(ctx);
        allContactList=database.AllContacts();
        ArrayList<Contacts> recordedContacts = new ArrayList<>();
        boolean hascontact = false;
        if (type.equals("IN")) {
            //incoming list
            recordedContacts.clear();
            for (String filename : recording) {
                String recordedfilearray[] = filename.split("__");      //recorded file_array
                if (recordedfilearray[2].equals("IN")) {
                    //incoming
                    for (Contacts people : allContactList) {
                        if (StringUtils.prepareContacts(ctx, people.getNumber()).equalsIgnoreCase(recordedfilearray[0])) {
                            long timestamp = new Long(recordedfilearray[1]).longValue();
                            String relative_time = ContactProvider.getrelative(timestamp);
                            Contacts contacts=new Contacts();
                            contacts.setName(people.getName());
                            contacts.setNumber(people.getNumber());
                            contacts.setTime(relative_time);
                            contacts.setPhoto(people.getPhoto());
                            contacts.setPhotoUri(people.getPhotoUri());
                            if(getDaileyTime(timestamp)==1){
                                //today
                                contacts.setView(1);
                                contacts.setDate(getDate(timestamp));
                                contacts.setTimestamp(timestamp);
                                recordedContacts.add(contacts);
                            }else if(getDaileyTime(timestamp)==2){
                                //yesterday
                                contacts.setView(2);
                                contacts.setDate(getDate(timestamp));
                                contacts.setTimestamp(timestamp);
                                recordedContacts.add(contacts);
                            }else{
                                //provide date
                                contacts.setView(3);
                                contacts.setDate(getDate(timestamp));
                                contacts.setTimestamp(timestamp);
                                recordedContacts.add(contacts);
                            }
                            hascontact = true;
                            break;
                        }
                    }
                    if (!hascontact) {
                        //no contact show them
                        long timestamp = new Long(recordedfilearray[1]).longValue();
                        String relative_time = ContactProvider.getrelative(timestamp);
                        Contacts nocontact = new Contacts();
                        nocontact.setNumber(recordedfilearray[0]);
                        nocontact.setTime(relative_time);
                        nocontact.setDate(getDate(timestamp));
                        if(getDaileyTime(timestamp)==1){
                            //today
                            nocontact.setView(1);
                            nocontact.setTimestamp(timestamp);

                            recordedContacts.add(nocontact);
                        }else if(getDaileyTime(timestamp)==2){
                            //yesterday
                            nocontact.setView(2);
                            nocontact.setTimestamp(timestamp);
                            recordedContacts.add(nocontact);
                        }else{
                            //provide date
                            nocontact.setView(3);
                            nocontact.setTimestamp(timestamp);
                            recordedContacts.add(nocontact);
                        }
                    } else {
                        hascontact = false;
                    }
                }
            }
        } else if (type.equals("OUT")) {
            recordedContacts.clear();
            for (String filename : recording) {
                String recordedfilearray[] = filename.split("__");      //recorded file_array
                if (recordedfilearray[2].equals("OUT")) {
                    //incoming
                    for (Contacts people : allContactList) {
                        if (StringUtils.prepareContacts(ctx, people.getNumber()).equalsIgnoreCase(recordedfilearray[0])) {
                            long timestamp = new Long(recordedfilearray[1]).longValue();
                            String relative_time = ContactProvider.getrelative(timestamp);
                            Contacts contacts=new Contacts();
                            contacts.setName(people.getName());
                            contacts.setNumber(people.getNumber());
                            contacts.setTime(relative_time);
                            contacts.setPhoto(people.getPhoto());
                            contacts.setDate(getDate(timestamp));
                            contacts.setPhotoUri(people.getPhotoUri());
                            if(getDaileyTime(timestamp)==1){
                                //today
                                contacts.setView(1);
                                contacts.setTimestamp(timestamp);

                                recordedContacts.add(contacts);
                            }else if(getDaileyTime(timestamp)==2){
                                //yesterday
                                contacts.setView(2);
                                contacts.setTimestamp(timestamp);
                                recordedContacts.add(contacts);
                            }else{
                                //provide date
                                contacts.setView(3);
                                contacts.setTimestamp(timestamp);
                                recordedContacts.add(contacts);
                            }
                            hascontact = true;
                            break;
                        }
                    }

                    if (!hascontact) {
                        //no contact show them
                        long timestamp = new Long(recordedfilearray[1]).longValue();
                        ContactProvider.getrelative(timestamp);
                        String relative_time = ContactProvider.getrelative(timestamp);
                        Contacts nocontact = new Contacts();
                        nocontact.setNumber(recordedfilearray[0]);
                        nocontact.setTime(relative_time);
                        nocontact.setDate(getDate(timestamp));
                        if(getDaileyTime(timestamp)==1){
                            //today
                            nocontact.setView(1);
                            nocontact.setTimestamp(timestamp);
                            recordedContacts.add(nocontact);
                        }else if(getDaileyTime(timestamp)==2){
                            //yesterday
                            nocontact.setView(2);
                            nocontact.setTimestamp(timestamp);
                            recordedContacts.add(nocontact);
                        }else{
                            //provide date
                            nocontact.setView(3);
                            nocontact.setTimestamp(timestamp);
                            recordedContacts.add(nocontact);
                        }
                    } else {
                        hascontact = false;
                    }
                }
            }
        } else {
            recordedContacts.clear();
            for (String filename : recording) {
                String recordedfilearray[] = filename.split("__");      //recorded file_array
                for (Contacts people : allContactList) {
                    if (StringUtils.prepareContacts(ctx, people.getNumber()).equalsIgnoreCase(recordedfilearray[0])) {
                        long timestamp = new Long(recordedfilearray[1]).longValue();
                        String relative_time = ContactProvider.getrelative(timestamp);
                        Contacts contacts=new Contacts();
                        contacts.setName(people.getName());
                        contacts.setNumber(people.getNumber());
                        contacts.setTime(relative_time);
                        contacts.setPhoto(people.getPhoto());
                        contacts.setDate(getDate(timestamp));
                        contacts.setPhotoUri(people.getPhotoUri());
                        if(getDaileyTime(timestamp)==1){
                            //today
                            contacts.setView(1);
                            contacts.setTimestamp(timestamp);
                            recordedContacts.add(contacts);
                        }else if(getDaileyTime(timestamp)==2){
                            //yesterday
                            contacts.setView(2);
                            contacts.setTimestamp(timestamp);
                            recordedContacts.add(contacts);
                        }else{
                            //provide date
                            contacts.setView(3);
                            contacts.setTimestamp(timestamp);
                            recordedContacts.add(contacts);
                        }
                        hascontact = true;
                        break;
                    }
                }

                if (!hascontact) {
                    //no contact show them
                    long timestamp = new Long(recordedfilearray[1]).longValue();//huge error chanceshere fix itbefore its too late
                    ContactProvider.getrelative(timestamp);
                    String relative_time = ContactProvider.getrelative(timestamp);
                    Contacts nocontact = new Contacts();
                    nocontact.setNumber(recordedfilearray[0]);
                    nocontact.setTime(relative_time);
                    nocontact.setDate(getDate(timestamp));
                    if(getDaileyTime(timestamp)==1){
                        //today
                        nocontact.setView(1);
                        nocontact.setTimestamp(timestamp);
                        recordedContacts.add(nocontact);
                    }else if(getDaileyTime(timestamp)==2){
                        //yesterday
                        nocontact.setView(2);
                        nocontact.setTimestamp(timestamp);
                        recordedContacts.add(nocontact);
                    }else{
                        //provide date
                        nocontact.setView(3);
                        nocontact.setTimestamp(timestamp);
                        recordedContacts.add(nocontact);
                    }
                } else {
                    hascontact = false;
                }
            }
        }
        if(!recordedContacts.isEmpty()){
            addToDatabase(ctx,recordedContacts); //error lies here
        }
        return recordedContacts;
    }


    public static void sendnotification(Context ctx) {
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(ctx);
        notifyBuilder.setContentTitle("Call recording in progress...");
        notifyBuilder.setSmallIcon(R.drawable.record);
        notifyBuilder.setTicker("New message");
        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notifyBuilder.build());
    }

    public static void openMaterialSheetDialog(LayoutInflater inflater, final int position, final String recording, final String contacts) {

        View view = inflater.inflate(R.layout.bottom_menu, null);
        DatabaseHelper db = new DatabaseHelper(view.getContext());
        TextView play = view.findViewById(R.id.play);
        TextView favorite = view.findViewById(R.id.fav);
        TextView delete = view.findViewById(R.id.delete);
        TextView turnoff = view.findViewById(R.id.turn_off);
        TextView upload = view.findViewById(R.id.upload);
        TextView share=view.findViewById(R.id.share);
        final Dialog materialSheet = new Dialog(view.getContext(), R.style.MaterialDialogSheet);
            materialSheet.setContentView(view);
            materialSheet.setCancelable(true);
            materialSheet.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            materialSheet.getWindow().setGravity(Gravity.BOTTOM);
            materialSheet.show();
        if (checkFav(view.getContext(), contacts)) {
            //set text remove
            favorite.setText("Add to favourite");
        } else {
            //set text add
            favorite.setText("Remove from favourtie");
        }
        if (checkContactToRecord(view.getContext(), contacts)) {
            turnoff.setText("Turn off recording");
        } else {
            turnoff.setText("Turn on recording");
        }
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(),position, Toast.LENGTH_SHORT).show();
                playmusic(v.getContext(),getFolderPath(v.getContext())+"/" + recording);
                    materialSheet.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(getFolderPath(view.getContext())+"/" + recording);
                if (file.delete()) {
                    //deleted
                    itemdelete.deleterefreshList(true);
                    Toast.makeText(view.getContext(), "File deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    //not deleted
                    itemdelete.deleterefreshList(true);
                    Toast.makeText(view.getContext(), "Deletion failed", Toast.LENGTH_SHORT).show();
                }
                materialSheet.dismiss();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(getFolderPath(view.getContext())+"/" +recording);
                Uri fileuri = FileProvider.getUriForFile(view.getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file);
                Intent sendintent=new Intent(Intent.ACTION_SEND);
                sendintent.putExtra(Intent.EXTRA_STREAM,fileuri);
                sendintent.setType("audio/*");
                view.getContext().startActivity(sendintent);
            }
        });
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFavourite(view.getContext(), contacts)) {
                    Toast.makeText(view.getContext(), "added to favourite", Toast.LENGTH_SHORT).show();
                    itemrefresh.refreshList(true);
                } else {
                    itemrefresh.refreshList(true);
                    Toast.makeText(view.getContext(), "removed from fvourite", Toast.LENGTH_SHORT).show();
                }
                materialSheet.dismiss();
            }
        });
        turnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //turn off recording
                if (checkContactToRecord(view.getContext(), contacts)) {
                    // recording enabled turn it off
                    if (!togglestate(view.getContext(), contacts)) {
                        //off
                        Toast.makeText(view.getContext(), "turned off", Toast.LENGTH_SHORT).show();
                        itemrefresh.refreshList(true);
                    }
                } else {
                    if (togglestate(view.getContext(), contacts)) {
                        Toast.makeText(view.getContext(), "turned on", Toast.LENGTH_SHORT).show();
                        itemrefresh.refreshList(true);
                    }
                    //recording disabled turn it on
                }
                materialSheet.dismiss();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ISaver mSaver;
                String ONEDRIVE_APP_ID = "6c8188dc-e1fa-4a21-a4a4-6e355d3a7620";
                final String filename = getFolderPath(view.getContext())+"/" + recording;
                final File f = new File(getFolderPath(view.getContext())+"/", recording);
                mSaver = Saver.createSaver(ONEDRIVE_APP_ID);
                Uri fileuri = FileProvider.getUriForFile(view.getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        f);
                mSaver.startSaving((Activity) view.getContext(), recording, fileuri);
            }
        });

    }
    public static void playmusic(Context ctx,String path){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(path);
        Uri fileuri = FileProvider.getUriForFile(ctx,
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        intent.setDataAndType(fileuri, "audio/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ctx.startActivity(intent);
    }

    //SQL Lite Database
    public static boolean checkFavourite(Context context,String number){
        DatabaseHelper db=new DatabaseHelper(context);
        Contacts contacts1=db.isContact(number);
        if(contacts1.getFav()==0){
            contacts1.setFav(1);
            int a= db.updateContact(contacts1);
            return true;
        }else if(contacts1.getFav()==1){
            contacts1.setFav(0);
            int a=db.updateContact(contacts1);
            return false;
        }else{
            return false;
        }
    }
    public static boolean checkFav(Context context,String number){
        DatabaseHelper db=new DatabaseHelper(context);
        Contacts contacts1=db.isContact(number);
        if(contacts1.getFav()==0){
            return true;
        }else if(contacts1.getFav()==1){
            return false;
        }else{
            return true;
        }
    }
    public static void addToDatabase(Context ctx,ArrayList<Contacts> recordedContacts) {
        DatabaseHelper db=new DatabaseHelper(ctx);
        for (Contacts cont:recordedContacts){
            Contacts s=db.isContact(StringUtils.prepareContacts(ctx,cont.getNumber()));
            if(s.getNumber()!=null){
                //has contaact
            }else{
                Contacts sd=new Contacts();
                sd.setFav(0);
                sd.setState(0);
                sd.setNumber(StringUtils.prepareContacts(ctx,cont.getNumber()));
                db.addContact(sd);
            }
        }
    }
    public static boolean checkContactToRecord(Context ctx,String number){
        DatabaseHelper db=new DatabaseHelper(ctx);
        Contacts newcontacts=db.isContact(number);
        if(newcontacts.getNumber()!=null){
            if(newcontacts.getState()==0){
                //recording on
                return true;
            }else if(newcontacts.getState()==1){
                return false;
                //dont wanna record
            }else{
                return true;
            }
        }
        return true;
    }

    public static boolean togglestate(Context ctx,String number){
        DatabaseHelper db=new DatabaseHelper(ctx);
            Contacts s=db.isContact(number);
            if(s.getNumber()!=null) {
                //has contanct
                if(s.getState()==0) {
                    s.setState(1);
                    db.updateContact(s);
                    return false;
                }else if(s.getState()==1) {
                    s.setState(0);
                    db.updateContact(s);
                    return  true;
                }
            }
        return true;
    }
    public interface refresh{
        public void refreshList(boolean var);
    }
    public interface deleterefresh{
        public void deleterefreshList(boolean var);
    }

    public static String getFolderPath(Context context){
        SharedPreferences directorypreference=context.getSharedPreferences("DIRECTORY",Context.MODE_PRIVATE);
        String s=directorypreference.getString("DIR",Environment.getExternalStorageDirectory().getAbsolutePath()+"/CallRecorder");
        return s;
    }

    public static void showDialog(Context ctx1, final String recording, final Contacts contacts) {
        final Dialog dialog=new Dialog(ctx1);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_lyout);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        TextView play = dialog.findViewById(R.id.play);
        TextView favorite = dialog.findViewById(R.id.fav);
        TextView delete = dialog.findViewById(R.id.delete);
        TextView turnoff = dialog.findViewById(R.id.turn_off);
        TextView upload = dialog.findViewById(R.id.upload);
        if (checkFav(ctx1, contacts.getNumber())) {
            //set text remove
            favorite.setText("Add to favourite");
        } else {
            //set text add
            favorite.setText("Remove from favourtie");
        }
        if (checkContactToRecord(ctx1, contacts.getNumber())) {
            turnoff.setText("Turn off recording");
        } else {
            turnoff.setText("Turn on recording");
        }
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(),position, Toast.LENGTH_SHORT).show();
                playmusic(v.getContext(),getFolderPath(v.getContext())+"/" + recording);
               dialog.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(getFolderPath(view.getContext())+"/" + recording);
                if (file.delete()) {
                    //deleted
                    itemdelete.deleterefreshList(true);
                    Toast.makeText(view.getContext(), "File deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    //not deleted
                    itemdelete.deleterefreshList(true);
                    Toast.makeText(view.getContext(), "Deletion failed", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFavourite(view.getContext(), contacts.getNumber())) {
                    Toast.makeText(view.getContext(), "added to favourite", Toast.LENGTH_SHORT).show();
                    itemrefresh.refreshList(true);
                } else {
                    itemrefresh.refreshList(true);
                    Toast.makeText(view.getContext(), "removed from fvourite", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        turnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //turn off recording
                if (checkContactToRecord(view.getContext(), contacts.getNumber())) {
                    // recording enabled turn it off
                    if (!togglestate(view.getContext(), contacts.getNumber())) {
                        //off
                        Toast.makeText(view.getContext(), "turned off", Toast.LENGTH_SHORT).show();
                        itemrefresh.refreshList(true);
                    }
                } else {
                    if (togglestate(view.getContext(), contacts.getNumber())) {
                        Toast.makeText(view.getContext(), "turned on", Toast.LENGTH_SHORT).show();
                        itemrefresh.refreshList(true);
                    }
                    //recording disabled turn it on
                }
                dialog.dismiss();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ISaver mSaver;
                String ONEDRIVE_APP_ID = "6c8188dc-e1fa-4a21-a4a4-6e355d3a7620";
                final String filename = getFolderPath(view.getContext())+"/" + recording;
                final File f = new File(getFolderPath(view.getContext())+"/", recording);
                mSaver = Saver.createSaver(ONEDRIVE_APP_ID);
                mSaver.startSaving((Activity) view.getContext(), filename, Uri.fromFile(f));
            }
        });
        dialog.show();
    }
    private static String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp*1000));
            return sdf.format(netDate).toString();
        }
        catch(Exception ex){
            return "xx";
        }
    }

    public static String getRecordsList(Context ctx,ArrayList<String> recordings,String type,Contacts contacts){
       String newRecordings="";
        String number=StringUtils.prepareContacts(ctx,contacts.getNumber());
        if (type.equals("IN")) {
            //incoming list
            for (String filename : recordings) {
                String recordedfilearray[] = filename.split("__");      //recorded file_array
                if(recordedfilearray[2].equals("IN")){
                    long timestamp= Long.valueOf(recordedfilearray[1]);
                    if(recordedfilearray[0].equals(number)&&timestamp==contacts.getTimestamp()){
                        return filename;
                    }
                }
            }
        } else if (type.equals("OUT")) {
            for (String filename : recordings) {
                String recordedfilearray[] = filename.split("__");      //recorded file_array
                if(recordedfilearray[2].equals("OUT")){
                    long timestamp= Long.valueOf(recordedfilearray[1]);
                    if(recordedfilearray[0].equals(number)&&timestamp==contacts.getTimestamp()){
                        return filename;
                    }
                }
            }
        } else {
            for (String filename : recordings) {

                String recordedfilearray[] = filename.split("__");      //recorded file_array
                long timestamp= Long.valueOf(recordedfilearray[1]);
                if(recordedfilearray[0].equals(number)&&timestamp==contacts.getTimestamp()){
                    return filename;
                }

            }
        }
        return newRecordings;
    }
    public static ArrayList<String> showlistfiles(Context ctx){
        ArrayList<String> recordedfiles=new ArrayList<>();
        String path=ContactProvider.getFolderPath(ctx);
        File file=new File(path);
        if(!file.exists()){
            //no folder empty data
            file.mkdirs();
        }
        File listfiles[]=file.listFiles();
        if(listfiles!=null){
            for(File list:listfiles){
                recordedfiles.add(list.getName());
            }
        }
        return recordedfiles;
    }
}
