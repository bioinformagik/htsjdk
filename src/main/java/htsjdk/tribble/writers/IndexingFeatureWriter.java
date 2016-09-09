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

package htsjdk.tribble.writers;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.LocationAware;
import htsjdk.samtools.util.PositionalOutputStream;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexCreator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class IndexingFeatureWriter<T extends Feature>
        extends AbstractFeatureWriter<T> {

    private final LocationAware location;
    private final IndexCreator indexer;
    private final File indexFile;
    private final SAMSequenceDictionary refDict;

    /**
     * Construct a new indexing feature writer.
     *
     * @param codec      codec for features.
     * @param outputStream the underlying output stream.
     * @param idxCreator indexer.
     * @param indexFile the file to write the index.
     * @param refDict dictionary to write in the index. May be {@code null}.
     */
    public IndexingFeatureWriter(final FeatureCodec<T, ?> codec,
            final OutputStream outputStream, final IndexCreator idxCreator, final File indexFile,
            final SAMSequenceDictionary refDict) {
        super(codec, asLocationAwareStream(outputStream));
        // checking the parameters
        if (idxCreator == null) {
            throw new IllegalArgumentException("null idxCreator");
        }
        if (indexFile == null) {
            throw new IllegalArgumentException("null indexFile");
        }
        this.indexer = idxCreator;
        this.indexFile = indexFile;
        this.refDict = refDict;
        // this is already a LocationAware
        this.location = (LocationAware) getOutputStream();
    }

    /**
     * Wraps if necessary the output stream
     */
    private static OutputStream asLocationAwareStream(final OutputStream outputStream) {
        return (outputStream instanceof LocationAware)
                ? outputStream : new PositionalOutputStream(outputStream);
    }

    @Override
    public void add(final T feature) {
        // should be added first
        indexer.addFeature(feature, location.getPosition());
        super.add(feature);
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (refDict != null) {
            indexer.setIndexSequenceDictionary(refDict);
        }
        final Index index = indexer.finalizeIndex(location.getPosition());
        index.write(indexFile);
    }
}
