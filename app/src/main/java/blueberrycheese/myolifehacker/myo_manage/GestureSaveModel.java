package blueberrycheese.myolifehacker.myo_manage;

/**
 * Created by Seongho on 2017-12-01.
 */

public class GestureSaveModel implements IGestureDetectModel {
    private final static Object LOCK = new Object();

    private String name = "";
    private IGestureDetectAction action=null;
    private int num = -1;
    private GestureSaveMethod saveMethod;

    public GestureSaveModel(GestureSaveMethod method) {
        saveMethod = method;
    }

    public GestureSaveModel(GestureSaveMethod method,int _num) {
        saveMethod = method;
        num = _num;
    }
    @Override
    public void event(long time, byte[] data) {
        synchronized (LOCK) {
         //   if(num==-1)
         //       saveMethod.addData(data);
         //   else
                saveMethod.addData(data,num);
            if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
                action("SAVE");
//                action(String.valueOf(gesture.getGestureCounter()));
            } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                action("SAVED");
            }
        }
    }


    @Override
    public void setAction(IGestureDetectAction action) {
        this.action = action;
    }

    @Override
    public void action() {
        action.action("SAVE");
    }

    public void action(String message) {
        action.action(message);
    }

}
