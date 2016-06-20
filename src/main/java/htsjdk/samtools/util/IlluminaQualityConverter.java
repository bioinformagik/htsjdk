/*
 * The MIT License
 *
 * Copyright (c) 2009 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package htsjdk.samtools.util;

/**
 * Methods for converting Illumina ASCI/PHRED qualities into Standard Phred scores.
 */
public class IlluminaQualityConverter {

    /**
     * This value is substract to a ASCII char to make it printable ASCII
     */
    public static final int ILLUMINA_SUBTRACT = 64;

    /**
     * This value is subtract to a Phred score to make it printable ASCII
     */
    public static final int PHRED_SUBTRACT = 31;

    private static IlluminaQualityConverter singleton = null;

    public static synchronized IlluminaQualityConverter getSingleton()  {
        if (singleton == null) {
            singleton = new IlluminaQualityConverter();
        }
        return singleton;
    }

    // TODO: add a cache for normal range qualities?
    private IlluminaQualityConverter() {
    }

    /**
     * Convert a illumina quality ASCII character into a phred score.
     */
    public byte illuminaCharToPhredBinary(final byte illuminaQuality) {
        final byte converted = (byte)(illuminaQuality - ILLUMINA_SUBTRACT);
        if(converted < 33) {
            throw new IllegalArgumentException("Invalid Illumina character: " + (char) illuminaQuality);
        }
        return (byte)(converted - 33);
    }

    /**
     * Decodes an array of illumina quality ASCII chars into phred numeric space.
     * Decode in place in order to avoid extra object allocation.
     */
    public void convertIlluminaQualityCharsToPhredBinary(final byte[] illuminaQuals) {
        for (int i=0; i<illuminaQuals.length; ++i) {
            illuminaQuals[i] = illuminaCharToPhredBinary(illuminaQuals[i]);
        }
    }

    /**
     * Convert a illumina quality (binary phred scores, not ASCII) to standard phred score
     */
    public byte illuminaPhredToStandard(final byte illuminaQuality) {
        final byte standardQuality = (byte) (illuminaQuality - PHRED_SUBTRACT);
        if (standardQuality < 0) {
            throw new IllegalArgumentException("Not Illumina encoded quality PHRED score: "+illuminaQuality);
        }
        return (byte) (illuminaQuality - PHRED_SUBTRACT);
    }

    /**
     * Decodes an array of illumina quality (binary phred scores, not ASCII) to standard phred score
     * Decode in place in order to avoid extra object allocation.
     */
    public void convertIlluminaQualitiesToStandard(final byte[] illuminaQuals) {
        for(int i = 0; i < illuminaQuals.length; i++) {
            illuminaQuals[i] = illuminaPhredToStandard(illuminaQuals[i]);
        }
    }
}
