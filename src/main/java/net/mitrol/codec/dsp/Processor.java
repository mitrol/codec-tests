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

package net.mitrol.codec.dsp;


import net.mitrol.codec.format.Formats;
import net.mitrol.codec.memory.Frame;

/**
 * Digital signaling processor.
 * <p>
 * DSP transforms media from its original format to one of the specified
 * output format. Output formats are specified as array where order of the
 * formats defines format's priority. If frame has format matching to output
 * format the frame won't be changed.
 *
 * @author kulikov
 */
public interface Processor {
    /**
     * Gets the list of supported codec.
     *
     * @return array of codec
     */
    public Codec[] getCodecs();

    /**
     * Sets the list of supported output formats.
     *
     * @param formats the list of formats sorted by format priority.
     */
    public void setFormats(Formats formats);

    /**
     * Transforms supplied frame if frame's format does not match to any
     * of the supported output formats and such transcoding is possible.
     *
     * @param frame the frame for transcoding
     * @return transcoded frame
     */
    public Frame process(Frame frame);
}
