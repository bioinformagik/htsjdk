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

package htsjdk.samtools;

import junit.framework.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class SAMSortOrderCheckerUnitTest {

    @DataProvider(name = "querynameSortedRecords")
    public Object[][] querynameSortedRecord() {
        final SAMRecord record999 = new SAMRecord(null);
        record999.setReadName("HXXX:999");
        final SAMRecord record1000 = new SAMRecord(null);
        record1000.setReadName("HXXX:1000");
        return new Object[][] {
                {Arrays.asList(record999, record1000), false},
                {Arrays.asList(record1000, record999), true},
                {Arrays.asList(record999, record999), true},
                {Arrays.asList(record999, record999, record1000), false},
                {Arrays.asList(record1000, record999, record999, record999), true},
                {Arrays.asList(record999, record999, record1000, record1000), false},
                {Arrays.asList(record1000, record1000, record999, record999), true}
        };
    }

    @Test(dataProvider = "querynameSortedRecords")
    public void testSAMSortOrderByPairsCheckerSorted(final List<SAMRecord> sortedRecords, final boolean htsjdkSorted) throws Exception {
        final SAMSortOrderChecker sortOrderChecker = new SAMSortOrderChecker.SAMSortOrderByPairsChecker(sortedRecords.size());
        sortedRecords.forEach(record -> Assert.assertTrue(sortOrderChecker.isSorted(record)));
    }

    @Test(dataProvider = "querynameSortedRecords")
    public void testQuerynameSortedOrder(final List<SAMRecord> recordList, final boolean htsjdkQueryNameSorted) throws Exception {
        final SAMSortOrderChecker sortOrderChecker = new SAMSortOrderChecker(SAMFileHeader.SortOrder.queryname);
        if (htsjdkQueryNameSorted) {
            recordList.forEach(record -> Assert.assertTrue(sortOrderChecker.isSorted(record)));
        } else {
            Assert.assertTrue(recordList.stream().filter(record -> !sortOrderChecker.isSorted(record)).findAny().isPresent());
        }
    }


}