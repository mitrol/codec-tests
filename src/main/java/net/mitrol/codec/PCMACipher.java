package net.mitrol.codec;

import net.mitrol.codec.g711.alaw.Decoder;
import net.mitrol.codec.g711.alaw.Encoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by fdecuzzi on 12/08/2015.
 */
public class PCMACipher implements Cipher {

    //region Attributes
    private Encoder g711AlawEncoder;
    private Decoder g711AlawDecoder;
    //endregion

    //region Constructor
    public PCMACipher() {
        this.g711AlawDecoder = new Decoder();
        this.g711AlawEncoder = new Encoder();
    }
    //endregion

    //region Cipher
    @Override
    public List<byte[]> encode(byte[] data) {
        int length = data.length / getCompressionRate().get();
        return g711AlawEncoder.process(data, length);
    }

    @Override
    public byte[] decode(byte[]... packets) {
        int compressionRate = getCompressionRate().get();
        int dstLength = packets.length == 1 ? packets[0].length * compressionRate : Stream.of(packets).mapToInt(x -> x.length).sum() * compressionRate;
        return g711AlawDecoder.process(compressionRate, dstLength, packets);
    }

    @Override
    public Optional<Integer> getCompressionRate() {
        return Optional.of(2);
    }

    @Override
    public void dispose() {
        g711AlawDecoder = null;
        g711AlawDecoder = null;
    }
    //endregion
}
