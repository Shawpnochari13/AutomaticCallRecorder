package com.techjany.automaticcallrecorder;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class SettingsActivity extends AppCompatPreferenceActivity {
    static Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this;
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        EditTextPreference editTextPreference;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            Preference button = findPreference("DIRECTORY");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent chooserIntent = new Intent(ctx, DirectoryChooserActivity.class);
                    DirectoryChooserConfig config = DirectoryChooserConfig.builder().newDirectoryName("CallRecorder")
                            .allowNewDirectoryNameModification(true)
                            .build();
                    chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
                    startActivityForResult(chooserIntent, 1001);
                    return true;
                }
            });
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1001) {
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                    SharedPreferences filepreference=ctx.getSharedPreferences("DIRECTORY",MODE_PRIVATE);
                    SharedPreferences.Editor editor=filepreference.edit();
                    editor.putString("DIR",data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                    editor.apply();
                    Intent intent=new Intent(ctx,MainActivity.class);
                    startActivity(intent);
                } else {
                    // Nothing selected
                }
            }
        }
    }

}
