package blueberrycheese.myolifehacker.myo_manage;

/**
 * Created by pc on 2018-03-10.
 */

public interface IGestureDetectModel {
    public void event(long eventTime, byte[] data);
    public void setAction(IGestureDetectAction action);
    public void action();
}
