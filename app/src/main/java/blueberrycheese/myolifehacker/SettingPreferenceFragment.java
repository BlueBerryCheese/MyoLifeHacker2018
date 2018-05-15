package blueberrycheese.myolifehacker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.BaseAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.annotation.Nullable;

import blueberrycheese.myolifehacker.events.ServiceEvent;

public class SettingPreferenceFragment extends PreferenceFragment {
    SharedPreferences prefs;

    SwitchPreference vibratePreference;
    ListPreference vibratePowerPreference;
    ListPreference recognizing_count_Preference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_preference);
        vibratePreference = (SwitchPreference)findPreference("vibrate");
        vibratePowerPreference  = (ListPreference)findPreference("vibrate_power");
        recognizing_count_Preference = (ListPreference)findPreference("recognizing_count");

        prefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (prefs.getBoolean("vibrate",true)) {
            vibratePreference.setSummary("사용");

        }

        if (!prefs.getString("vibrate_power", "").equals("")) {
            vibratePowerPreference.setSummary(prefs.getString("vibrate_power", "강하게"));

        }
        if (!prefs.getString("recognizing_count", "").equals("")) {
            recognizing_count_Preference.setSummary(prefs.getString("recognizing_count", "30"));

        }
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onStop() {
        try{
            EventBus.getDefault().unregister(this);
        }catch (Exception e){}
        super.onStop();
    }


    @Override
    public void onResume(){
        super.onResume();
        try {
            EventBus.getDefault().register(this);           //이벤트 버스 다시 키는 역활
        }catch (Exception e){}
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("vibrate")){
                if(prefs.getBoolean("vibrate",true)){
                    vibratePreference.setSummary("사용");
                }else{
                    vibratePreference.setSummary("사용 안함");
                }
            }

            if(key.equals("vibrate_power")){
                vibratePowerPreference.setSummary(prefs.getString("vibrate_power","강하게"));
            }

            if(key.equals("recognizing_count")){
                recognizing_count_Preference.setSummary(prefs.getString("recognizing_count","30"));
            }

            String vp = prefs.getString("vibrate_power","강하게");
            int vpp;
            int rc = Integer.parseInt(prefs.getString("recognizing_count","30"));
            boolean iv = prefs.getBoolean("vibrate",true);
            if(vp.equals("강하게"))
                vpp=3;
            else if(vp.equals("보통"))
                vpp=2;
            else if(vp.equals("약하게"))
                vpp=1;
            else
                vpp=3;
            Log.d("SettingPreference","setting_event : " + vp+" , " + rc+" , " + iv);
            EventBus.getDefault().postSticky(new ServiceEvent.SettingEvent(vpp,iv,rc));

            //2뎁스 PreferenceScreen 내부에서 발생한 환경설정 내용을 2뎁스 PreferenceScreen에 적용하기 위한 소스
            ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();

        }
    };

}
