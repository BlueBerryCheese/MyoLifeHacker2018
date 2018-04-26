package blueberrycheese.myolifehacker.SystemControl;

import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;


/**
 * Created by pc on 2018-03-10.
 */
//  이것의 용도가 뭐지?
public class GestureDetectSendResultAction_System implements IGestureDetectAction{
    SystemControlActivity activity;

    public GestureDetectSendResultAction_System(SystemControlActivity activity){
        this.activity = activity;
    }


    @Override
    public void action(String Tag ) {
        switch (Tag) {
            case "SAVE":
                activity.setGestureText("Teach Me Another");
                activity.startNopModel();
                break;
            case "SAVED":
                activity.setGestureText("Detect Ready");
                activity.startNopModel();
                break;
            default:
                activity.setGestureText(Tag);
                break;
        }
    }
}
