package net.mitrol.codec;

import net.mitrol.codec.g711.ulaw.Decoder;
import net.mitrol.codec.g711.ulaw.Encoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by fdecuzzi on 12/08/2015.
 */
public class PCMUCipher implements Cipher {


    //region Attributes
    private Encoder g711UlawEncoder;
    private Decoder g711UlawDecoder;
    //endregion

    //region Constructor

    public PCMUCipher() {
        this.g711UlawDecoder = new Decoder();
        this.g711UlawEncoder = new Encoder();
    }

    //endregion

    //region Cipher
    @Override
    public List<byte[]> encode(byte[] data) {
        int length = data.length / getCompressionRate().get();
        return g711UlawEncoder.process(data, length);
    }

    @Override
    public byte[] decode(byte[]... packets) {
        int compressionRate = getCompressionRate().get();
        int dstLength = packets.length == 1 ? packets[0].length * compressionRate : Stream.of(packets).mapToInt(x -> x.length).sum() * compressionRate;
        return g711UlawDecoder.process(compressionRate, dstLength, packets);
    }

    @Override
    public Optional<Integer> getCompressionRate() {
        return Optional.of(2);
    }

    @Override
    public void dispose() {
        g711UlawDecoder = null;
        g711UlawEncoder = null;
    }
    //endregion


}
