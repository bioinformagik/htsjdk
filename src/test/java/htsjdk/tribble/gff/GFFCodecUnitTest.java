/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Daniel Gomez-Sanchez
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

package htsjdk.tribble.gff;

import htsjdk.HtsjdkTest;
import htsjdk.tribble.annotation.Strand;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class GFFCodecUnitTest extends HtsjdkTest {

    private final static GFFCodec codec = new GFFCodec();

    @DataProvider
    public Object[][] featureProvider() {
        final Map<String, Object> myExon = new HashMap<>(2);
        myExon.put("ID", "2");
        myExon.put("Name", "my_exon");
        return new Object[][]{
                {"chr1\t.\t.\t1\t100\t.\t.\t.\tID=1",
                        new GFFFeature("chr1", 1, 100, null, null, null, null, null, Collections.singletonMap("ID", "1"))},
                {"chr1\tmy_source\texon\t10\t100\t10.4\t+\t1\tID=2;Name=my_exon",
                        new GFFFeature("chr1", 10, 100, "my_source", "exon", 10.4, Strand.POSITIVE, 1, myExon)}
        };
    }

    @Test(dataProvider = "featureProvider")
    public void testDecodeFeature(final String featureLine, final GFFFeature expected) throws Exception {
        final GFFFeature actual = codec.decode(featureLine);
        Assert.assertEquals(actual, expected);
        Assert.assertEquals(actual.asGff3String(), featureLine);
    }

}