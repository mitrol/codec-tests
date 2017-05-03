package net.mitrol.codec;

import net.mitrol.codec.*;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by francisco.decuzzi on 02/09/2016.
 */
@BenchmarkMode(Mode.All)
@State(Scope.Benchmark)
public class CipherPerformanceBenchMark {


    public static final String OPUS = "OPUS";
    public static final String G729 = "G729";
    public static final String PCMA = "PCMA";
    public static final String PCMU = "PCMU";

    private static byte[] pcmData = new byte[320];
    @Param({PCMU, PCMA, G729, OPUS})
    private String cipherName;

    public static <T extends Cipher> byte[] encode(byte[] pcmData, T cipher) {
        return cipher.encode(pcmData).get(0);
    }

    public static <T extends Cipher> byte[] decode(byte[] encriptedData, T cipher) {
        return cipher.decode(encriptedData);
    }

    @Benchmark
    @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public void encode() {
        Cipher cipher = initCipher(cipherName);
        cipher.encode(pcmData);
    }

    @Benchmark
    @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public void decode() {
        Cipher cipher = initCipher(cipherName);
        byte[] toDecode = new byte[pcmData.length / (cipher.getCompressionRate().isPresent() ? cipher.getCompressionRate().get() : 8)];
        cipher.decode(toDecode);
    }

    private Cipher initCipher(String cipherName) {
        Cipher cipher = null;
        switch (cipherName) {
            case PCMU:
                cipher = new PCMUCipher();
                break;
            case PCMA:
                cipher = new PCMACipher();
                break;
            case G729:
                cipher = new G729Cipher();
                break;
            case OPUS:
                cipher = new OpusCipher(8000, 1);
                break;
        }
        return cipher;
    }

}
