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
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureEncoder;
import htsjdk.tribble.FeatureWriter;
import htsjdk.tribble.Tribble;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.index.DynamicIndexCreator;
import htsjdk.tribble.index.IndexCreator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.variant.vcf.VCFEncoder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Abstract feature writer.
 *
 * Uses an internal Writer, based by the ByteArrayOutputStream lineBuffer,
 * to temp. buffer the header and per-site output before flushing the per line output
 * in one go to the super.getOutputStream.  This results in high-performance, proper encoding,
 * and allows us to avoid flushing explicitly the output stream, which
 * allows us to properly compress files in gz format without breaking indexing on the fly
 * for uncompressed streams.
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public class AbstractFeatureWriter<HEADER_TYPE, FEATURE_TYPE extends Feature>
        implements FeatureWriter<HEADER_TYPE, FEATURE_TYPE> {

    /**
     * The encoder for the writer.
     */
    private final FeatureEncoder<HEADER_TYPE, FEATURE_TYPE> encoder;

    /*
    * The AbstractFeatureWriter writer uses an internal Writer, based by the ByteArrayOutputStream lineBuffer,
    * to temp. buffer the header and per-site output before flushing the per line output
    * in one go to the super.getOutputStream.  This results in high-performance, proper encoding,
    * and allows us to avoid flushing explicitly the output stream getOutputStream, which
    * allows us to properly compress vcfs in gz format without breaking indexing on the fly
    * for uncompressed streams.
    */
    private static final int INITIAL_BUFFER_SIZE = 1024 * 16;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
    /* Wrapping in a {@link BufferedWriter} avoids frequent conversions with individual writes to OutputStreamWriter. */
    private final Writer writer =
            new BufferedWriter(new OutputStreamWriter(lineBuffer, VCFEncoder.VCF_CHARSET));

    /**
     * The output stream to write in.
     */
    private final OutputStream outputStream;

    /**
     * @param encoder      encoder for features
     * @param outputStream the underlying output stream
     */
    public AbstractFeatureWriter(final FeatureEncoder<HEADER_TYPE, FEATURE_TYPE> encoder,
            final OutputStream outputStream) {
        this.encoder = encoder;
        this.outputStream = outputStream;
    }

    /**
     * Get the underlying output stream.
     */
    OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Writes the header using the encoder, and reset the buffer.
     */
    @Override
    public void writeHeader(final HEADER_TYPE header) {
        final byte[] encodedHeader = encoder.encodeHeader(header);
        try {
            outputStream.write(encodedHeader);
            writeAndResetBuffer();
        } catch (IOException e) {
            throw new TribbleException("Can't write file.", e);
        }
    }

    @Override
    public void close() throws IOException {
        // write the rest of the buffer
        writeAndResetBuffer();
        // close the writer and the output stream after it
        writer.close();
        outputStream.close();
    }

    /**
     * Actually write the line buffer contents to the destination output stream. After calling this
     * function
     * the line buffer is reset so the contents of the buffer can be reused
     */
    private void writeAndResetBuffer() throws IOException {
        writer.flush();
        outputStream.write(lineBuffer.toByteArray());
        lineBuffer.reset();
    }

    @Override
    public void add(final FEATURE_TYPE vc) {
        try {
            outputStream.write(encoder.encode(vc));
        } catch (IOException e) {
            throw new TribbleException(e.getMessage(), e);
        }
    }

    /**
     * @throws TribbleException
     */
    public static <HEADER, FEATURE extends Feature> AbstractFeatureWriter<HEADER, FEATURE> getFeatureWriter(final String outputPath, final FeatureEncoder<HEADER, FEATURE> codec, final boolean indexOnTheFly, final
            SAMSequenceDictionary refDict, final IndexFactory.IndexBalanceApproach iba) throws TribbleException {
        final OutputStream outputStream;
        try {
             outputStream = new FileOutputStream(outputPath);
        } catch (IOException e) {
            throw new TribbleException(e.getMessage(), e);
        }
        // this is the simplest case: no index requested
        if (!indexOnTheFly) {
            return new AbstractFeatureWriter<>(codec, outputStream);
        }
        final boolean isTabix = AbstractFeatureReader.hasBlockCompressedExtension(outputPath);
        // if index is requested, create the indexing
        final IndexCreator idxCreator = (isTabix)
                ? new TabixIndexCreator(refDict, codec.getTabixFormat())
                : new DynamicIndexCreator(new File(outputPath), iba);
        final File indexFile = new File((isTabix) ? Tribble.tabixIndexFile(outputPath) : Tribble.indexFile(outputPath));
        return new IndexingFeatureWriter<>(codec, outputStream, idxCreator, indexFile, refDict);
    }
}
