/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package net.mitrol.codec.g711.alaw;


import net.mitrol.codec.format.Format;
import net.mitrol.codec.format.FormatFactory;
import net.mitrol.codec.g711.G711Codec;
import net.mitrol.codec.memory.Frame;

/**
 * Implements G.711 A-law compressor.
 *
 * @author Oleg Kulikov
 */
public class Encoder implements G711Codec {

    private final static Format alaw = FormatFactory.createAudioFormat("pcma", 8000, 8, 1);
    private final static Format linear = FormatFactory.createAudioFormat("linear", 8000, 16, 1);

    private final static int cClip = 32635;
    private static byte aLawCompressTable[] = new byte[]{
            1, 1, 2, 2, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7
    };

    /**
     * (Non Java-doc)
     */
    public Format getSupportedInputFormat() {
        return linear;
    }

    /**
     * (Non Java-doc)
     */
    public Format getSupportedOutputFormat() {
        return alaw;
    }

    /**
     * (Non Java-doc)
     */
    public Frame process(Frame frame) {
        Frame res = new Frame(new byte[frame.getLength() / 2]);
        int len = process(frame.getData(), 0, frame.getLength(), res.getData());

        res.setOffset(0);
        res.setLength(len);
        res.setFormat(alaw);
        res.setTimestamp(frame.getTimestamp());
        res.setDuration(frame.getDuration());
        res.setEOM(frame.isEOM());
        res.setSequenceNumber(frame.getSequenceNumber());

        return res;
    }

    public int process(byte[] src, int offset, int len, byte[] res) {
        int j = offset;
        int count = len / 2;
        short sample = 0;

        for (int i = 0; i < count; i++) {
            sample = (short) (((src[j++] & 0xff) | (src[j++]) << 8));
            res[i] = linearToALawSample(sample);
        }
        return count;
    }

    /**
     * Compress 16bit value to 8bit value
     *
     * @param sample 16-bit sample
     * @return compressed 8-bit value.
     */
    private byte linearToALawSample(short sample) {
        int sign;
        int exponent;
        int mantissa;
        int s;

        sign = ((~sample) >> 8) & 0x80;
        if (!(sign == 0x80)) {
            sample = (short) -sample;
        }
        if (sample > cClip) {
            sample = cClip;
        }
        if (sample >= 256) {
            exponent = (int) aLawCompressTable[(sample >> 8) & 0x7F];
            mantissa = (sample >> (exponent + 3)) & 0x0F;
            s = (exponent << 4) | mantissa;
        } else {
            s = sample >> 4;
        }
        s ^= (sign ^ 0x55);
        return (byte) s;
    }
}
