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

package htsjdk.tribble.bed;

import htsjdk.tribble.AsciiFeatureEncoder;
import htsjdk.tribble.index.tabix.TabixFormat;

import java.util.Arrays;

/**
 * Encoder for a simple BED feature. BED are 0-based and this will be represented in the encoded.
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class SimpleBEDEncoder extends AsciiFeatureEncoder<Object, BEDFeature> {

    private static final String DEFAULT_BED_SEPARATOR = "\t";

    /**
     * Only contig, start and end will be encoded.
     */
    @Override
    public String encodeAsString(final BEDFeature feature) {
        return String.join(DEFAULT_BED_SEPARATOR, Arrays.asList(
                feature.getContig(), String.valueOf(feature.getStart() - 1),
                String.valueOf(feature.getEnd())
        ));
    }

    /**
     * The simple BED feature does not allow encoding of the string.
     * @return {@code null}
     */
    @Override
    public String encodeHeaderAsString(Object header) {
        return null;
    }

    @Override
    public TabixFormat getTabixFormat() {
        return TabixFormat.BED;
    }
}
