package com.techjany.automaticcallrecorder.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import com.techjany.automaticcallrecorder.MainActivity;
import com.techjany.automaticcallrecorder.R;
import com.techjany.automaticcallrecorder.adapter.IncommingAdapter;
import com.techjany.automaticcallrecorder.contacts.ContactProvider;
import com.techjany.automaticcallrecorder.pojo_classes.Contacts;
import com.techjany.automaticcallrecorder.utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class Incomming extends Fragment {
   private IncommingAdapter recyclerAdapter;
    RecyclerView recyclerView;
    Context ctx;
    boolean mensu=false;
    ArrayList<Object> searchPeople=new ArrayList<>();
    ArrayList<String> recordings=new ArrayList<>();
    ArrayList<Contacts> recordedContacts=new ArrayList<>();
    ArrayList<Object> realrecordingcontacts=new ArrayList<>();
    TreeMap<String ,ArrayList<Contacts>> headerevent=new TreeMap<>();
    SwipeRefreshLayout swipeRefreshLayout;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public Incomming() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.incoming_fragment,container,false);
        ctx=view.getContext();
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .color(Color.parseColor("#dadde2"))
                        .sizeResId(R.dimen.divider)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter=new IncommingAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        Bundle bundle;
        bundle=getArguments();
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
        recordings=bundle.getStringArrayList("RECORDING");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            showContacts();
        }
        recyclerAdapter.setContacts(realrecordingcontacts); //fix this
        recyclerAdapter.setListener(new IncommingAdapter.itemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(mensu){
                    Contacts contacts1= (Contacts) searchPeople.get(position);
                    String records=ContactProvider.getRecordsList(v.getContext(),recordings,"IN",contacts1);
                    if(Build.VERSION.SDK_INT>18){
                        ContactProvider.openMaterialSheetDialog(getLayoutInflater(),position,records, StringUtils.prepareContacts(ctx,contacts1.getNumber()));
                    }else{
                        ContactProvider.showDialog(v.getContext(),records,contacts1);
                    }
                }else {
                    Contacts contacts= (Contacts) realrecordingcontacts.get(position);
                    String records=ContactProvider.getRecordsList(v.getContext(),recordings,"IN",contacts);
                    if(Build.VERSION.SDK_INT>18){
                        ContactProvider.openMaterialSheetDialog(getLayoutInflater(),position,records,StringUtils.prepareContacts(ctx,contacts.getNumber()));
                    }else{
                        ContactProvider.showDialog(v.getContext(),records,contacts);
                    }
                }
                ContactProvider.setItemrefresh(new ContactProvider.refresh() {
                    @Override
                    public void refreshList(boolean var) {
                        if(var)
                            recyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        MainActivity.setQueylistener2(new MainActivity.querySearch2() {
            @Override
            public void Search_name2(String name) {
                if(name.length()>2){
                    mensu=true;
                    searchPeople.clear();
                    for(Contacts contacts:recordedContacts){
                        if(contacts.getNumber().contains(name)){
                            //dsd
                            searchPeople.add(contacts);
                            continue;
                        }
                        if(contacts.getName()!=null&&contacts.getName().toLowerCase().contains(name.toLowerCase())){
                            searchPeople.add(contacts);
                        }
                    }
                            recyclerAdapter.setContacts(searchPeople);
                            recyclerAdapter.notifyDataSetChanged();

                }else{
                    mensu=false;
                            recyclerAdapter.setContacts(realrecordingcontacts);
                            recyclerAdapter.notifyDataSetChanged();

                }

            }
        });
        return view;
    }

    private void refreshItems() {
        recordings=ContactProvider.showlistfiles(ctx);
        showContacts();
        recyclerAdapter.setContacts(realrecordingcontacts);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(getContext(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showContacts() {
        headerevent.clear();
        ArrayList<Contacts> contactses = new ArrayList<>();
        if(!realrecordingcontacts.isEmpty()){
            realrecordingcontacts.clear();
        }
        if(!recordedContacts.isEmpty()){
            recordedContacts.clear();
        }
        recordedContacts=ContactProvider.getCallList(getContext(),recordings,"IN");
        for (Contacts contacts:recordedContacts){
            if(contacts.getView()==1){
                if(!headerevent.containsKey("1")){
                    headerevent.put("1",new ArrayList<Contacts>());
                }
                headerevent.get("1").add(contacts);
            }else if(contacts.getView()==2){
                if(!headerevent.containsKey("2")){
                    headerevent.put("2",new ArrayList<Contacts>());
                }
                headerevent.get("2").add(contacts);
            }else {
                if(!headerevent.containsKey(contacts.getDate())){
                    headerevent.put(contacts.getDate(),new ArrayList<Contacts>());
                }
                headerevent.get(contacts.getDate()).add(contacts);
            }
        }
        for (String date:headerevent.keySet()){
            if(date.equals("1")){
                if(headerevent.keySet().contains("2")){
                    date="2";
                }
            }else if(date.equals("2")){
                if(headerevent.keySet().contains("1")){
                    date="1";
                }
            }
            contactses.clear();
            for (Contacts contacts : headerevent.get(date)) {
                contactses.add(contacts);
            }
            for (Contacts contacts : sorts(contactses)) {
                realrecordingcontacts.add(contacts);
            }
            realrecordingcontacts.add(date);
        }
        recyclerAdapter.notifyDataSetChanged();
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    private ArrayList<Contacts> sorts(ArrayList<Contacts> contactses) {
        Collections.sort(contactses);
        return contactses;
    }
}
