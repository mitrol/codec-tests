package net.mitrol.codec;

import com.sun.jna.ptr.PointerByReference;
import net.mitrol.codec.opus.Opus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by dq on 16/08/15.
 */
public class OpusCipher implements Cipher {


    //region Constants

    /**
     * The maximum size of a packet we can create. Since we're only creating
     * packets with a single frame, that's a 1 byte TOC + the maximum frame size.
     * See http://tools.ietf.org/html/rfc6716#section-3.2
     */
    public static final int MAX_PACKET = 1 + 1275;
    private static final OpusCipherOptions DEFAULT_OPTIONS = new OpusCipherOptions();
    //endregion


    private PointerByReference encoder;
    private PointerByReference decoder;
    private int sampleRate;
    private int channels;
    private int frameSizeInMillis = 20;
    private int frameSize;

    public OpusCipher(int sampleRate, int channels) {
        this(sampleRate, channels, DEFAULT_OPTIONS);
    }

    public OpusCipher(int sampleRate, int channels, OpusCipherOptions opusCipherOptions) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.frameSize = (this.sampleRate * frameSizeInMillis) / 1000;

        IntBuffer decoderError = IntBuffer.allocate(4);

        Object[] complexityArgs = new Object[]{opusCipherOptions.getComplexity()};
        Object[] signalArgs = new Object[]{opusCipherOptions.getSignalType()};
        Object[] bitRateArgs = new Object[]{sampleRate};
        Object[] bandWidthArgs = new Object[]{opusCipherOptions.getBandwidth()};
        Object[] appArgs = new Object[]{opusCipherOptions.getApplicationType()};

        decoder = Opus.INSTANCE.opus_decoder_create(sampleRate, channels, decoderError);
        Opus.INSTANCE.opus_encoder_ctl(decoder, Opus.OPUS_SET_COMPLEXITY_REQUEST, complexityArgs);
        Opus.INSTANCE.opus_encoder_ctl(decoder, Opus.OPUS_SET_SIGNAL_REQUEST, signalArgs);
        Opus.INSTANCE.opus_encoder_ctl(decoder, Opus.OPUS_SET_BITRATE_REQUEST, bitRateArgs);
        Opus.INSTANCE.opus_encoder_ctl(decoder, Opus.OPUS_SET_MAX_BANDWIDTH_REQUEST, bandWidthArgs);
        Opus.INSTANCE.opus_encoder_ctl(decoder, Opus.OPUS_SET_APPLICATION_REQUEST, appArgs);

        IntBuffer encoderError = IntBuffer.allocate(4);

        encoder = Opus.INSTANCE.opus_encoder_create(sampleRate, channels, Opus.OPUS_APPLICATION_VOIP, encoderError);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_BITRATE_REQUEST, bitRateArgs);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_MAX_BANDWIDTH_REQUEST, bandWidthArgs);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_COMPLEXITY_REQUEST, complexityArgs);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_SIGNAL_REQUEST, signalArgs);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_APPLICATION_REQUEST, appArgs);
    }

    @Override
    public byte[] decode(byte[]... packets) {
        ShortBuffer shortBuffer = ShortBuffer.allocate(MAX_PACKET);
        for (byte[] transferedBytes : packets) {
            int decoded = Opus.INSTANCE.opus_decode(decoder, transferedBytes, transferedBytes.length, shortBuffer, frameSize, 0);
            shortBuffer.position(shortBuffer.position() + decoded);
        }
        shortBuffer.flip();

        short[] shorts = new short[shortBuffer.remaining()];
        shortBuffer.get(shorts);

        return shortToByte(shorts);
    }

    public int getFrameSize() {
        return frameSize;
    }

    @Override
    public List<byte[]> encode(byte[] data) {

        short[] shorts = new short[data.length / 2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

        ShortBuffer shortBuffer = ShortBuffer.wrap(shorts);

        shortBuffer.position(shorts.length);

        shortBuffer.flip();

        int read;

        List<byte[]> list = new ArrayList<>();
        while (shortBuffer.hasRemaining()) {
            ByteBuffer dataBuffer = ByteBuffer.allocate(MAX_PACKET);
            int toRead = Math.min(shortBuffer.remaining(), dataBuffer.remaining());
            read = Opus.INSTANCE.opus_encode(encoder, shortBuffer, frameSize, dataBuffer, toRead);
            // System.err.println("read: "+read);
            dataBuffer.position(dataBuffer.position() + read);
            dataBuffer.flip();
            byte[] encoded = new byte[dataBuffer.remaining()];
            dataBuffer.get(encoded);
            list.add(encoded);
            shortBuffer.position(shortBuffer.position() + frameSize);
        }
        // used for debugging
        shortBuffer.flip();
        return list;
    }

    @Override
    public Optional<Integer> getCompressionRate() {
        return Optional.ofNullable(null);
    }

    @Override
    public void dispose() {
        if (encoder != null) {
            Opus.INSTANCE.opus_encoder_destroy(encoder);
            encoder = null;
        }
        if (decoder != null) {
            Opus.INSTANCE.opus_decoder_destroy(decoder);
            decoder = null;
        }
    }

    private byte[] shortToByte_Twiddle_Method(final short[] input) {
        final int len = input.length;
        final byte[] buffer = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            buffer[(i * 2) + 1] = (byte) (input[i]);
            buffer[(i * 2)] = (byte) (input[i] >> 8);
        }
        return buffer;
    }

    private byte[] shortToByte(final short[] input) {
        final int len = input.length;
        final byte[] buffer = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            buffer[(i * 2)] = (byte) (input[i]);
            buffer[(i * 2) + 1] = (byte) (input[i] >> 8);
        }
        return buffer;
    }
}
