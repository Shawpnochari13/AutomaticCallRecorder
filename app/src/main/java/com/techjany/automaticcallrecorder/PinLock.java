package com.techjany.automaticcallrecorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alimuzaffar.lib.pin.PinEntryEditText;

/**
 * Created by sandhya on 31-Aug-17.
 */

public class PinLock extends AppCompatActivity {
    TextView pin,confirm,setup;
    Button set,cancel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_lock);
        final PinEntryEditText pinEntry = (PinEntryEditText) findViewById(R.id.txt_pin_entry);
        final PinEntryEditText pinEntry2 = (PinEntryEditText) findViewById(R.id.txt_pin_entry2);
        pin=(TextView)findViewById(R.id.pin);
        confirm=(TextView)findViewById(R.id.confirm);
        set=(Button)findViewById(R.id.set);
        setup=findViewById(R.id.setup);
        boolean sets=getIntent().getBooleanExtra("SET",false);
        final SharedPreferences sharedPreferences=getSharedPreferences("LOCK",MODE_PRIVATE);
        final String pin=sharedPreferences.getString("PIN","");
        cancel=findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinEntry.setText(null);
                pinEntry2.setText(null);
            }
        });

        if(sets){
            pinEntry2.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(pinEntry.getText().toString().equals(pinEntry2.getText().toString())){
                        if(pinEntry.length()==4){
                            //write to shared prefrence
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putString("PIN",pinEntry.getText().toString());
                            editor.apply();
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("AUTH",true);
                            finish();
                            startActivity(intent);
                        }else {
                            pinEntry.setError("Enter 4 digit pin");
                        }
                    }else{
                        pinEntry2.setError("pin not match");
                        pinEntry2.setText(null);
                    }
                }
            });
        }else{
            if(pin.isEmpty()){
                pinEntry2.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.VISIBLE);
                set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(pinEntry.getText().toString().equals(pinEntry2.getText().toString())){
                            if(pinEntry.length()==4){
                                //write to shared prefrence
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString("PIN",pinEntry.getText().toString());
                                editor.apply();
                                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                intent.putExtra("AUTH",true);
                                finish();
                                startActivity(intent);
                            }else {
                                pinEntry.setError("Enter 4 digit pin");
                            }
                        }else{
                            pinEntry2.setError("pin not match");
                            pinEntry2.setText(null);
                        }
                    }
                });
                //set password
            }else{
                pinEntry2.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                setup.setVisibility(View.GONE);
                set.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean b=SP.getBoolean("LOCK",false);
                if(b){
                    //authenticate user
                    if (pinEntry != null) {
                        pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                            @Override
                            public void onPinEntered(CharSequence str) {
                                if (str.toString().equals(pin)) {
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    intent.putExtra("AUTH",true);
                                    finish();
                                    startActivity(intent);
                                } else {
                                    pinEntry.setText(null);
                                    pinEntry.setError("Wrong Pin number");
                                }
                            }
                        });
                    }
                }else{
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("AUTH",true);
                    finish();
                    startActivity(intent);
                }
            }
        }

        }


}
