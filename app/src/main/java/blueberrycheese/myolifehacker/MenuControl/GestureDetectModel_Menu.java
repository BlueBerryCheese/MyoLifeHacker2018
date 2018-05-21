package blueberrycheese.myolifehacker.MenuControl;

import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;

/**
 * Created by pc on 2018-03-10.
 */

public class GestureDetectModel_Menu implements IGestureDetectModel {
    private final static Object LOCK = new Object();

    private String name = "";
    private IGestureDetectAction action;
    private GestureDetectMethod_Menu detectMethod;

    public GestureDetectModel_Menu(GestureDetectMethod_Menu method) {
        detectMethod = method;
    }

    @Override
    public void event(long time, byte[] data) {
        synchronized (LOCK) {
            GestureDetectMethod_Menu.GestureState gestureState = detectMethod.getDetectGesture(data);
            action(gestureState.name());
        }
    }


    @Override
    public void setAction(IGestureDetectAction action) {
        this.action = action;
    }

    @Override
    public void action() {
        action.action("DETECT");
    }

    public void action(String message) {
        action.action(message);
    }

}
