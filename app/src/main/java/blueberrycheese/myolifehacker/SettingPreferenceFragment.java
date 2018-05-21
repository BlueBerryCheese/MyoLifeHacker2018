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
    ListPreference lock_vibratePowerPreference;
    ListPreference recog_vibratePowerPreference;
    ListPreference conn_vibratePowerPreference;
    ListPreference recognizing_count_Preference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_preference);
        vibratePreference = (SwitchPreference)findPreference("vibrate");
        lock_vibratePowerPreference  = (ListPreference)findPreference("lock_vibrate_power");
        recog_vibratePowerPreference  = (ListPreference)findPreference("recog_vibrate_power");
        conn_vibratePowerPreference  = (ListPreference)findPreference("conn_vibrate_power");
        recognizing_count_Preference = (ListPreference)findPreference("recognizing_count");

        prefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (prefs.getBoolean("vibrate",true)) {
            vibratePreference.setSummary("사용");

        }

        if (!prefs.getString("lock_vibrate_power", "").equals("")) {
            lock_vibratePowerPreference.setSummary(prefs.getString("lock_vibrate_power", "강하게"));
        }
        if (!prefs.getString("recog_vibrate_power", "").equals("")) {
            recog_vibratePowerPreference.setSummary(prefs.getString("recog_vibrate_power", "강하게"));
        }
        if (!prefs.getString("conn_vibrate_power", "").equals("")) {
            conn_vibratePowerPreference.setSummary(prefs.getString("conn_vibrate_power", "강하게"));
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

            if(key.equals("lock_vibrate_power")){
                lock_vibratePowerPreference.setSummary(prefs.getString("lock_vibrate_power","강하게"));
            }
            if(key.equals("recog_vibrate_power")){
                recog_vibratePowerPreference.setSummary(prefs.getString("recog_vibrate_power","강하게"));
            }
            if(key.equals("conn_vibrate_power")){
                conn_vibratePowerPreference.setSummary(prefs.getString("conn_vibrate_power","강하게"));
            }

            if(key.equals("recognizing_count")){
                recognizing_count_Preference.setSummary(prefs.getString("recognizing_count","30"));
                EventBus.getDefault().post(new ServiceEvent.reCreateDetectM_Event());
            }

            String lock_vp = prefs.getString("lock_vibrate_power","강하게");
            String recog_vp = prefs.getString("recog_vibrate_power","강하게");
            String conn_vp = prefs.getString("conn_vibrate_power","강하게");
            int lock_vpp,recog_vpp,conn_vpp;
            int rc = Integer.parseInt(prefs.getString("recognizing_count","30"));
            boolean iv = prefs.getBoolean("vibrate",true);
            if(lock_vp.equals("강하게"))
                lock_vpp=3;
            else if(lock_vp.equals("보통"))
                lock_vpp=2;
            else if(lock_vp.equals("약하게"))
                lock_vpp=1;
            else
                lock_vpp=3;

            if(recog_vp.equals("강하게"))
                recog_vpp=3;
            else if(recog_vp.equals("보통"))
                recog_vpp=2;
            else if(recog_vp.equals("약하게"))
                recog_vpp=1;
            else
                recog_vpp=3;

            if(conn_vp.equals("강하게"))
                conn_vpp=3;
            else if(conn_vp.equals("보통"))
                conn_vpp=2;
            else if(conn_vp.equals("약하게"))
                conn_vpp=1;
            else
                conn_vpp=3;

            Log.d("SettingPreference","setting_event : " + lock_vp+" , "+ recog_vp+" , "+ conn_vp+" , " + rc+" , " + iv);
            EventBus.getDefault().postSticky(new ServiceEvent.SettingEvent(lock_vpp,recog_vpp,conn_vpp,iv,rc));

            //2뎁스 PreferenceScreen 내부에서 발생한 환경설정 내용을 2뎁스 PreferenceScreen에 적용하기 위한 소스
            ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();

        }
    };

}
