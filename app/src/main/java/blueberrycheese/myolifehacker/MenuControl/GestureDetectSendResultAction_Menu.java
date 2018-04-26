package blueberrycheese.myolifehacker.MenuControl;

import blueberrycheese.myolifehacker.SystemControl.SystemControlActivity;
import blueberrycheese.myolifehacker.TabFragment1;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;


/**
 * Created by pc on 2018-03-10.
 */
//  이것의 용도가 뭐지?
public class GestureDetectSendResultAction_Menu implements IGestureDetectAction{
    TabFragment1 activity;

    public GestureDetectSendResultAction_Menu(TabFragment1 activity){
        this.activity = activity;
    }


    @Override
    public void action(String Tag ) {
        switch (Tag) {
            default:
                //activity.setGestureText(Tag);
                break;
        }
    }
}
