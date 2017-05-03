package net.mitrol.codec;

import java.util.List;
import java.util.Optional;

/**
 * Created by fdecuzzi on 12/08/2015.
 */
public interface Cipher {
    List<byte[]> encode(byte[] data);
    byte[] decode(byte[]... packets);
    Optional<Integer> getCompressionRate();
    void dispose();

}
