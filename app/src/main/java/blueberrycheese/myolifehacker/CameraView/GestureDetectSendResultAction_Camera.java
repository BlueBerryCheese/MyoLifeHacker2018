package blueberrycheese.myolifehacker.CameraView;

import blueberrycheese.myolifehacker.CameraView.CameraActivity;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectAction;

public class GestureDetectSendResultAction_Camera implements IGestureDetectAction {
    CameraActivity activity;

    public GestureDetectSendResultAction_Camera(CameraActivity activity) {
        this.activity = activity;
    }


    @Override
    public void action(String Tag) {
//        switch (Tag) {
//            case "SAVE":
//                activity.setGestureText("Teach Me Another");
//                activity.startNopModel();
//                break;
//            case "SAVED":
//                activity.setGestureText("Detect Ready");
//                activity.startNopModel();
//                break;
//            default:
//                activity.setGestureText(Tag);
//                break;
//        }
    }
}