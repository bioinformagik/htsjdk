/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Daniel Gómez-Sánchez
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

import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureEncoder;

/**
 * Abstract class for encode ASCII features
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public abstract class AsciiFeatureEncoder<H, F extends Feature> implements FeatureEncoder<H, F> {

    /**
     * Encodes the feature as a String.
     */
    public abstract String encodeAsString(F feature);

    /**
     * Encodes the header as a String. {@code null} if no header is encoded.
     */
    public abstract String encodeHeaderAsString(H header);

    /**
     * Uses {@link String#getBytes()} from the string obtained by  {@link #encodeAsString(Feature)}.
     */
    @Override
    public byte[] encode(F feature) {
        return encodeAsString(feature).getBytes();
    }

    /**
     * Uses {@link String#getBytes()} from the string obtained by {@link #encodeHeaderAsString(Object)} (Feature)}.
     */
    public byte[] encodeHeader(H header) {
        final String headerString = encodeHeaderAsString(header);
        return (headerString == null) ? NO_HEADER : headerString.getBytes();
    }

}
