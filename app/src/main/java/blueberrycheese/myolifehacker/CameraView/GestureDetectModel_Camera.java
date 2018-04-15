package blueberrycheese.myolifehacker.CameraView;

import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;

public class GestureDetectModel_Camera implements IGestureDetectModel {
    private final static Object LOCK = new Object();

    private String name = "";
    private IGestureDetectAction action;
    private GestureDetectMethod_Camera detectMethod;

    public GestureDetectModel_Camera(GestureDetectMethod_Camera method) {
        detectMethod = method;
    }

    @Override
    public void event(long time, byte[] data) {
        synchronized (LOCK) {
            GestureDetectMethod_Camera.GestureState gestureState = detectMethod.getDetectGesture(data);
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
