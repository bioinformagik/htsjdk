/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Daniel Gomez-Sanchez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package htsjdk.tribble;

import htsjdk.tribble.index.tabix.TabixFormat;

/**
 * Encoder for features to use in writers.
 *
 * @param <FEATURE_TYPE> a feature type
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public interface FeatureEncoder<HEADER_TYPE, FEATURE_TYPE> {

    /** No header encoded is represented as an empty byte array. */
    public static final byte[] NO_HEADER = new byte[0];

    /**
     * Encodes the header into bytes[] to write.
     *
     * @return the header encoded as an array of bytes to write; {@link #NO_HEADER} if the header is not encoded or no header is supported.
     */
    public byte[] encodeHeader(HEADER_TYPE header);

    /**
     * Encodes a feature into bytes[] to write.
     *
     * @return the feature encoded as an array of bytes to write.
     */
    public byte[] encode(FEATURE_TYPE feature);

    /**
     * Define the tabix format for the feature, used for indexing. Default implementation throws an exception.
     *
     * Note that only {@link AsciiFeatureCodec} could read tabix files as defined in
     * {@link AbstractFeatureReader#getFeatureReader(String, String, FeatureCodec, boolean)}
     *
     * @return the format to use with tabix
     * @throws TribbleException if the format is not defined
     */
    default public TabixFormat getTabixFormat() {
        throw new TribbleException(this.getClass().getSimpleName() + "does not have defined tabix format");
    }

}
