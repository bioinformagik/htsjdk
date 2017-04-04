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
package htsjdk.samtools;

import htsjdk.samtools.util.Murmur3;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates simple check for SAMRecord order.
 * @author alecw@broadinstitute.org
 */
public class SAMSortOrderChecker {
    private final SAMFileHeader.SortOrder sortOrder;
    protected SAMRecord prev;
    private final SAMRecordComparator comparator;

    public SAMSortOrderChecker(final SAMFileHeader.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        switch (sortOrder) {
            case coordinate:
                comparator = new SAMRecordCoordinateComparator();
                break;
            case queryname:
                comparator = new SAMRecordQueryNameComparator();
                break;
            case duplicate:
                comparator = new SAMRecordDuplicateComparator();
                break;
            case unsorted:
            default:
                comparator = null;
                break;
        }
    }

    /**
     * Check if given SAMRecord violates sort order relative to previous SAMRecord.
     * @return True if sort order is unsorted, if this is the first record, or if previous <= rec.
     */
    public boolean isSorted(final SAMRecord rec) {
        if (comparator == null) {
            return true;
        }
        boolean ret = true;
        if (prev != null) {
            ret = comparator.fileOrderCompare(prev, rec) <= 0;
        }
        prev = rec;
        return ret;
    }

    public SAMRecord getPreviousRecord() {
        return prev;
    }

    /**
     * Return the sort key used for the given sort order.  Useful in error messages.
     */
    public String getSortKey(final SAMRecord rec) {
        switch (sortOrder) {

            case coordinate:
                return rec.getReferenceName() + ":" + rec.getAlignmentStart();
            case queryname:
                return rec.getReadName();
            case unsorted:
            default:
                return null;
        }
    }

    /**
     * {@link SAMSortOrderChecker} to allow checking of arbitrary queryname order, but keeping reads
     * at least {@code maximumReadsToCheck} apart.
     */
    public static class SAMSortOrderByPairsChecker extends SAMSortOrderChecker {

        private final Murmur3 hasher = new Murmur3(42);
        private final List<Integer> readNameHashes;
        private final int maximumReadsToCheck;

        public SAMSortOrderByPairsChecker(final int maximumReadsToCheck) {
            super(SAMFileHeader.SortOrder.coordinate);
            this.maximumReadsToCheck = maximumReadsToCheck;
            this.readNameHashes = new ArrayList<>(maximumReadsToCheck);
        }

        /**
         * Returns {@code false} if and only if the hash for the record read name is saw before the
         * last added; {@code true} otherwise.
         */
        @Override
        public boolean isSorted(final SAMRecord rec) {
            // gets the hash and the index of it
            final int hash = hasher.hashUnencodedChars(rec.getReadName());
            final int index = readNameHashes.indexOf(hash);

            final boolean sorted;
            if (index == -1) {
                // if not found, add to the list (evicting if necessary)
                if (readNameHashes.size() == maximumReadsToCheck) {
                    readNameHashes.remove(0);
                }
                readNameHashes.add(hash);
                // and it is sorted
                sorted = true;
            } else if (index == readNameHashes.size() - 1) {
                // if it is the last index, it is correctly sorted
                // and do not need to be added
                sorted = true;
            } else {
                // otherwise, it is not sorted
                // any new hash will be unsorted too
                sorted = false;
            }

            // set the previous to the record
            prev = rec;
            return sorted;
        }
    }
}
