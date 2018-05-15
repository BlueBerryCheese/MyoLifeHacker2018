package blueberrycheese.myolifehacker;

import android.app.Application;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashSet;
import java.util.Set;

import blueberrycheese.myolifehacker.events.ServiceEvent;

public class MyoApp extends Application {
    final public String TAG = "MyoApp";

    private boolean unlockDefault = false;
    private boolean unlockMusic = false;

    public void unlockGesture(int g){
        Log.e(TAG, "Gesture unlocked");
        if(g == 0 && !unlockDefault){
            unlockDefault = true;
        } else if(g == 1 && !unlockMusic){
            unlockMusic = true;
        }
    }

    public void lockGesture(){
        unlockDefault = unlockMusic = false;
        Log.e(TAG, "Gesture locked");
    }

    public boolean isUnlocked(){
        if(!unlockDefault && !unlockMusic){
            return false;
        }else{
            return true;
        }
    }

    public int getUnlockedGesture(){
        if(unlockDefault){
            return 4;
        }else if(unlockMusic){
            return 5;
        }else {
            return -1;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        //<----For ImageViewr - Gllaery
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
        Set<RequestListener> listeners = new HashSet<>();
        listeners.add(new RequestLoggingListener());
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setRequestListeners(listeners)
                .build();
        Fresco.initialize(this, config);
        //For ImageViewr - Gllaery---->
    }

}
