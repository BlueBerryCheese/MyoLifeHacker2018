package blueberrycheese.myolifehacker.myo_manage;

/**
 * Created by pc on 2018-03-10.
 */

public class GestureDetectModelManager {
    private static final Object LOCK = new Object();
    private static IGestureDetectModel currentModel = new NopModel();

    public static IGestureDetectModel getCurrentModel(){
        synchronized (LOCK) {
            return currentModel;
        }
    }

    public static void setCurrentModel(IGestureDetectModel model){
        synchronized (LOCK) {
            currentModel = model;
        }
    }

}

