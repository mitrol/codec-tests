package net.mitrol.codec;

import net.mitrol.codec.g729.Decoder;
import net.mitrol.codec.g729.Encoder;
import net.mitrol.codec.memory.Frame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by fdecuzzi on 12/08/2015.
 */
public class G729Cipher implements Cipher {

    //region Attributes
    private Encoder g729Encoder;
    private Decoder g729Decoder;
    //endregion

    //region Constructor
    public G729Cipher() {
        this.g729Decoder = new Decoder();
        this.g729Encoder = new Encoder();

    }
    //endregion

    //region Cipher
    @Override
    public List<byte[]> encode(byte[] data) {

        int length = data.length / getCompressionRate().get();

        int step = 160;
        int index = 0;
        int stepCount = data.length / step;
        byte[] partialData;
        byte[] temp;

        ByteBuffer inputBuffer = ByteBuffer.wrap(data);
        ByteBuffer outPutBuffer = ByteBuffer.allocate(length);
        while (index < stepCount) {
            partialData = new byte[step];
            inputBuffer.get(partialData);
            temp = g729Encoder.process(new Frame(partialData)).getData();
            outPutBuffer.put(temp);
            index++;
        }
        List<byte[]> bytes = new ArrayList<>(1);
        bytes.add(outPutBuffer.array());
        return bytes;
    }

    @Override
    public byte[] decode(byte[]... packets) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(Stream.of(packets).mapToInt(x -> x.length).sum() * getCompressionRate().get());

        for (byte[] data : packets) {
            int length = data.length * getCompressionRate().get();
            int step = 10;
            int index = 0;
            int stepCount = data.length / step;
            byte[] partialData;
            byte[] temp;
            ByteBuffer inputBuffer = ByteBuffer.wrap(data);
            ByteBuffer outputBuffer = ByteBuffer.allocate(length);
            while (index < stepCount) {
                partialData = new byte[step];
                inputBuffer.get(partialData);
                temp = g729Decoder.process(new Frame(partialData)).getData();
                outputBuffer.put(temp);
                index++;
            }
            byteBuffer.put(outputBuffer.array());
        }

        return byteBuffer.array();

    }

    @Override
    public Optional<Integer> getCompressionRate() {
        return Optional.of(16);
    }

    @Override
    public void dispose() {
        g729Decoder = null;
        g729Encoder = null;
    }
    //endregion
}
