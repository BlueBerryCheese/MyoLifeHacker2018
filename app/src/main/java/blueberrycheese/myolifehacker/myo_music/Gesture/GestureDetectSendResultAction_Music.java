package blueberrycheese.myolifehacker.myo_music.Gesture;

import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;
import blueberrycheese.myolifehacker.myo_music.activities.activitys.MainActivity;

public class GestureDetectSendResultAction_Music implements IGestureDetectAction {
    MainActivity activity;

    public GestureDetectSendResultAction_Music(MainActivity activity){
        this.activity=activity;
    }

    public void action(String Tag){

    }
}
