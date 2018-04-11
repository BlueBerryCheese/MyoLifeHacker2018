package blueberrycheese.myolifehacker.SystemControl;

import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;

/**
 * Created by pc on 2018-03-10.
 */

public class GestureDetectModel_System implements IGestureDetectModel {
    private final static Object LOCK = new Object();

    private String name = "";
    private IGestureDetectAction action;
    private GestureDetectMethod_System detectMethod;

    public GestureDetectModel_System(GestureDetectMethod_System method) {
        detectMethod = method;
    }

    @Override
    public void event(long time, byte[] data) {
        synchronized (LOCK) {
            GestureDetectMethod_System.GestureState gestureState = detectMethod.getDetectGesture(data);
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
