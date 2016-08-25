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

package htsjdk.samtools.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class QualityUtilTest {

    @DataProvider(name = "TweakOverlappingQualities")
    public Object[][] overlappingQualities() {
        final byte aBase = 'A';
        final byte tBase = 'T';
        return new Object[][] {
                {aBase, aBase, 60, 10, new byte[]{70,  0}}, // same base, sum of qualities
                {aBase, tBase, 60, 10, new byte[]{48,  0}}, // different base, 80% of quality for higher
                {aBase, tBase, 60, 60, new byte[]{48,  0}},
                {aBase, tBase, 10, 60, new byte[]{ 0, 48}}
        };
    }

    @Test(dataProvider = "TweakOverlappingQualities")
    public void testTweakOverlappingQualities(final byte base1, final byte base2, final int qual1, final int qual2, final byte[] expectedQuals) throws Exception {
        Assert.assertEquals(QualityUtil.tweakOverlappingQualities(base1, base2, qual1, qual2), expectedQuals);
    }

}