package com.techjany.automaticcallrecorder;

import android.Manifest;
import android.app.SearchManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.techjany.automaticcallrecorder.DeviceAdmin.DeviceAdmin;
import com.techjany.automaticcallrecorder.SqliteDatabase.ContactsDatabase;
import com.techjany.automaticcallrecorder.Transformer.ZoomOutPageTransformer;
import com.techjany.automaticcallrecorder.adapter.ScreenSlidePagerAdapter;
import com.techjany.automaticcallrecorder.contacts.ContactProvider;
import com.techjany.automaticcallrecorder.fragments.AllFragment;
import com.techjany.automaticcallrecorder.fragments.Incomming;
import com.techjany.automaticcallrecorder.fragments.Outgoing;
import com.techjany.automaticcallrecorder.pojo_classes.Contacts;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private  ViewPager viewPager;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ScreenSlidePagerAdapter adapter;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    static querySearch queylistener;
    static querySearch2 queylistener2;
    static querySearch3 queylistener3;
    ArrayList<Contacts> phoneContacts=new ArrayList<>();
    ArrayList<String> recordinglist=new ArrayList<>();
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        boolean Auth=getIntent().getBooleanExtra("AUTH",false);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean b1=SP1.getBoolean("LOCK",false);
        if(b1&&!Auth){
            Intent intent=new Intent(getApplicationContext(),PinLock.class);
            finish();
            startActivity(intent);
        }
//        initAdmin();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkAndRequestPermissions();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager=findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true,new ZoomOutPageTransformer());
        adapter=new ScreenSlidePagerAdapter(getSupportFragmentManager());
        showlistfile();
        viewPager.setAdapter(adapter);
        tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            tabLayout.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.ic_record_voice_over_black_24dp));
            tabLayout.getTabAt(1).setIcon(getResources().getDrawable(R.drawable.ic_002_incoming_phone_call_symbol));
            tabLayout.getTabAt(2).setIcon(getResources().getDrawable(R.drawable.ic_001_outgoing_call));
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            tabLayout.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.ic_action_name));
            tabLayout.getTabAt(1).setIcon(getResources().getDrawable(R.drawable.ic_recvied));
            tabLayout.getTabAt(2).setIcon(getResources().getDrawable(R.drawable.ic_outgoing));
        }
        toolbar.setTitle("Call Recorder");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            phoneContacts=ContactProvider.getContacts(getApplicationContext());//ask permission here
            storeToDatabase(phoneContacts);
        }
               //ask permission
        //navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    changeColorOfStatusAndActionBar();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void storeToDatabase(ArrayList<Contacts> phoneContacts) {
        ContactsDatabase datbaseObj=new ContactsDatabase(this);
        for (Contacts con:phoneContacts){
           //photo uri got here
            if(datbaseObj.isContact(con.getNumber()).getNumber()!=null){
               datbaseObj.updateContact(con);
            }else{
                datbaseObj.addContact(con);
            }
        }
    }

    private void showlistfile() {
        Bundle bundles=new Bundle();
        String path=ContactProvider.getFolderPath(this);
        File file=new File(path);
        if(!file.exists()){
            //no folder empty data
            file.mkdirs();
        }
        File listfiles[]=file.listFiles();
        if(listfiles!=null){
            for(File list:listfiles){
                recordinglist.add(list.getName());
            }
        }
        bundles.putStringArrayList("RECORDING",recordinglist);
        AllFragment allFragment=new AllFragment();
        allFragment.setArguments(bundles);
        Incomming fr=new Incomming();
        fr.setArguments(bundles);
        Outgoing outgoing=new Outgoing();
        outgoing.setArguments(bundles);
        adapter.addFrag(allFragment,"All");
        adapter.addFrag(fr,"Recieved");
        adapter.addFrag(outgoing,"Outgoing");
        adapter.notifyDataSetChanged();
    }

    private void initAdmin() {
        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdmin.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click Activate to activate device admin");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // mDPM.lockNow();
                // Intent intent = new Intent(MainActivity.this,
                // TrackDeviceService.class);
                // startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ContactProvider.deletelistener(new ContactProvider.deleterefresh() {
            @Override
            public void deleterefreshList(boolean var) {
                if(var){
                    Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }
    private void changeColorOfStatusAndActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
             switch (viewPager.getCurrentItem()) {
                case 0:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case 1:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.cyan));
                    window.setStatusBarColor(getResources().getColor(R.color.cyan_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.cyan));
                    break;
                case 2:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    window.setStatusBarColor(getResources().getColor(R.color.smooth_red_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    break;
                default:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_resourse_file,menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
                searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    queylistener.Search_name(newText+"");
                    queylistener2.Search_name2(newText+"");
                try {
                    queylistener3.Search_name3(newText+"");
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                if(!newText.isEmpty()){
                    tabLayout.setVisibility(View.GONE);
                }else{
                    tabLayout.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.setting){
            Intent intent= new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem()==0){
           super.onBackPressed();
        }else{
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting) {
            // Handle the setting action
            Intent intent= new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        } else if(id==R.id.pin_lock){
//                SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                boolean b1=SP1.getBoolean("LOCK",false);
//                if(!b1){
//                    Toast.makeText(getApplicationContext(),"Set Enable pin in Setting to set up pin lock",Toast.LENGTH_SHORT).show();
//                }else {
                    Intent intent=new Intent(MainActivity.this,PinLock.class);
                    intent.putExtra("SET",true);
                    startActivity(intent);
//                }

        } else if (id == R.id.fav) {
            //open favourite activity
            Intent intent= new Intent(MainActivity.this,Favourite.class);
            intent.putStringArrayListExtra("RECORD",recordinglist);
            startActivity(intent);
        }else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Best Call recorder app download now.https://play.google.com/store/apps/details?id=com.techjany.automaticcallrecorder&hl=en";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Share App");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }else if (id == R.id.rate_us) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.techjany.automaticcallrecorder")));
        }else if(id==R.id.recording_issue){
            Intent intent= new Intent(MainActivity.this,Recording_issue.class);
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public static  void setQueylistener(querySearch quey){
        queylistener=quey;
    }
    public interface querySearch{
        public void Search_name(String name1);
    }
    public static  void setQueylistener2(querySearch2 quey1){
        queylistener2=quey1;
    }
    public interface querySearch2{
        public void Search_name2(String name1);
    }
    public static  void setQueylistener3(querySearch3 quey3){
        queylistener3=quey3;
    }
    public interface querySearch3{
        public void Search_name3(String name1);
    }

    private  boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.clear();
        int recordaudio=ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);//
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//
        int call= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);//
        int read_phonestate= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);//
        int Capture_audio_output= ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        int process_outgoing_call= ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS);//
        int modify_audio_setting= ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);//
        int read_contacts= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);//

        if (read_contacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (modify_audio_setting != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (process_outgoing_call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }

        if (read_phonestate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (recordaudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Capture_audio_output!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Please Allow All Permission To Continue..", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                    // Permission is granted
                    phoneContacts=ContactProvider.getContacts(getApplicationContext());//ask permission here
                    storeToDatabase(phoneContacts);
        }
    }
}
