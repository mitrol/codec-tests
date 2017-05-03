package net.mitrol.codec.g711;

import net.mitrol.codec.dsp.Codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco.decuzzi on 02/09/2016.
 */
public interface G711Codec extends Codec {


    public int process(byte[] data, int offset, int length, byte[] out);

    default List<byte[]> process(byte[] src, int dstLength) {
        byte[] encodedBytes = new byte[dstLength];
        this.process(src, 0, src.length, encodedBytes);
        List<byte[]> bytes = new ArrayList<>(1);
        bytes.add(encodedBytes);
        return bytes;
    }

    default byte[] process(int compressionRate, int dstLength, byte[]... packets) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(dstLength);
        for (byte[] data : packets) {
            int length = data.length * compressionRate;
            byte[] linearBytes = new byte[length];
            this.process(data, 0, data.length, linearBytes);
            byteBuffer.put(linearBytes);
        }
        return byteBuffer.array();
    }
}
