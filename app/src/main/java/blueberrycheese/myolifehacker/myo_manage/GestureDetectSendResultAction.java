package blueberrycheese.myolifehacker.myo_manage;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import blueberrycheese.myolifehacker.MainActivity;
import blueberrycheese.myolifehacker.TabFragment3;


/**
 * Created by pc on 2018-03-10.
 */
//  이것의 용도가 뭐지?
public class GestureDetectSendResultAction implements IGestureDetectAction{
    Activity activity;
    TabFragment3 tabFragment3;

    public GestureDetectSendResultAction(Activity activity){
        this.activity = activity;
    }

    public GestureDetectSendResultAction(Activity activity,TabFragment3 tabFragment3){
        this.activity = activity;
        this.tabFragment3 = tabFragment3;

    }

    @Override
    public void action(String Tag ) {
        switch (Tag) {
            case "SAVE":
                tabFragment3.setGestureText("Teach Me Another");
                tabFragment3.startNopModel();
                break;
            case "SAVED":
                tabFragment3.setGestureText("Detect Ready");
                tabFragment3.startNopModel();
                break;
            default:
                tabFragment3.setGestureText(Tag);
                break;
        }
    }
}
