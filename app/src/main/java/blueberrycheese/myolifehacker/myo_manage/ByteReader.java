package blueberrycheese.myolifehacker.myo_manage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Seongho on 2017-12-01.
 */

public class ByteReader {
    private byte[] byteData;
    private ByteBuffer byteBuffer;

    public void setByteData(byte[] data){
        this.byteData = data;
        this.byteBuffer = ByteBuffer.wrap(this.byteData);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] getByteData() {
        return byteData;
    }

    public short getShort() {
        return this.byteBuffer.getShort();
    }

    public byte getByte(){
        return this.byteBuffer.get();
    }

    public int getInt(){
        return this.byteBuffer.getInt();
    }

    public String getByteDataString() {
        final StringBuilder stringBuilder = new StringBuilder(byteData.length);
        for (byte byteChar : byteData) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        return stringBuilder.toString();
    }

    public String getIntDataString() {
        final StringBuilder stringBuilder = new StringBuilder(byteData.length);
        for (byte byteChar : byteData) {
            stringBuilder.append(String.format("%5d,", byteChar));
        }
        return stringBuilder.toString();
    }
}
