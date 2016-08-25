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

import htsjdk.samtools.SAMRecord;

/**
 * Utility class for working with quality scores and error probabilities.
 *
 * @author Tim Fennell
 */
public final class QualityUtil {
    private static final double[] errorProbabilityByPhredScore;

    static {
        errorProbabilityByPhredScore = new double[101];
        for (int i=0; i<errorProbabilityByPhredScore.length; ++i) {
            errorProbabilityByPhredScore[i] = 1d/Math.pow(10d, i/10d);
        }
    }

    /** Given a phred score between 0 and 100 returns the probability of error. */
    public static double getErrorProbabilityFromPhredScore(final int i) {
        return errorProbabilityByPhredScore[i];
    }

    /** Gets the phred score for any given probability of error. */
    public static int getPhredScoreFromErrorProbability(final double probability) {
        return (int) Math.round(-10 * Math.log10(probability));
    }

    /** Gets the phred score given the specified observations and errors. */
    public static int getPhredScoreFromObsAndErrors(final double observations, final double errors) {
        return getPhredScoreFromErrorProbability(errors / observations);
    }

    /**
     * Calculates the sum of error probabilities for all read bases in the SAM record. Takes
     * the SAM record as opposed to the qualities directly so that it can make sure to count
     * no-calls as 1 instead of what the quality score says.
     * */
    public static double sumOfErrorProbabilities(final SAMRecord rec) {
        final byte[] bases = rec.getReadBases();
        final byte[] quals = rec.getBaseQualities();

        double sum = 0;

        for (int i=0; i<bases.length; ++i) {
            if (SequenceUtil.isNoCall(bases[i])) ++sum;
            else sum += QualityUtil.getErrorProbabilityFromPhredScore(quals[i]);
        }

        return sum;
    }

    /**
     * Fix the quality of two bases that comes from an overlapping pair in the same way as samtools
     * does.
     *
     *
     * Setting the quality of one of the bases to 0 effectively remove the redundant base for calling.
     * In addition, if the bases overlaps we have increased confidence if they agree (or reduced if
     * they don't). Thus, the algorithm proceed as following:
     *
     * 1. If the bases are the same, the quality of the first element is the sum of both qualities
     * (capped to 200) and the quality of the second is reduced to 0.
     *
     * 2. If the bases are different, the base with the highest quality is reduced with a factor
     * of 0.8, and the quality of the lowest is reduced to 0.
     *
     * {@see tweak_overlap_quality function in <a href="https://github.com/samtools/htslib/blob/master/sam.c">samtools</a>}.
     *
     * @param firstBase the first base
     * @param secondBase the second base
     * @param firstQuality the quality for the first base
     * @param secondQuality the quality for the second base
     *
     * @return tuple with the tweak quality for the first base (a) and second base (b)
     */
    public static byte[] tweakOverlappingQualities(byte firstBase, byte secondBase, int firstQuality, int secondQuality) {
        int newFirst = 0, newSecond = 0;
        if(firstBase == secondBase) {
            // we are very confident about this base
            newFirst = firstQuality + secondQuality;
            if(newFirst > 200) {
                newFirst = (byte) 200;
            }
        } else {
            // not so confident about a_qual anymore given the mismatch
            if(firstQuality >= secondQuality) {
                newFirst  = (int) (0.8 * firstQuality);
            } else {
                newSecond = (int) (0.8 * secondQuality);
            }
        }
        return new byte[]{(byte)newFirst, (byte)newSecond};
    }

}
