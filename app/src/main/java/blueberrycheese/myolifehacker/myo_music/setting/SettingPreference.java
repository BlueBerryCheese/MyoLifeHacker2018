package blueberrycheese.myolifehacker.myo_music.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.Toast;

import blueberrycheese.myolifehacker.R;

/**
 * Created by park on 2018-02-21.
 */
/*
public class SettingPreference extends PreferenceFragment{
    //private SwitchPreference switchPreference;
    private Context mContext;
    private Activity mActivity;
    public void SettingPreference(){};
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.settings_preference);

        mActivity=this.getActivity();
        mContext=this.getActivity();
        final SwitchPreference switchPreference = (SwitchPreference) findPreference(this.getResources().getString(R.string.vibrate));
        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (switchPreference.isChecked()) {
                    Toast.makeText(mActivity,"Unchecked",Toast.LENGTH_SHORT).show();
                    switchPreference.setChecked(false);
                } else {
                    Toast.makeText(mActivity,"Checked",Toast.LENGTH_SHORT).show();
                    switchPreference.setChecked(true);
                }
                return false;
            }


        });
        //  SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        //public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        //});
        //}
    }
}*/