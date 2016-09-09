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

import java.io.Closeable;
import java.io.IOException;

/**
 * Writer for features.
 *
 * @param <FEATURE_TYPE> a feature type
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public interface FeatureWriter<FEATURE_TYPE extends Feature> extends Closeable {

    /**
     * Writes the header.
     *
     * @throws IllegalStateException if the header is already written or the writer was already closed.
     */
    public void writeHeader(FeatureCodecHeader header);

    /** Close the writer. */
    public void close() throws IOException;

    /**
     * Add a feature into the writer.
     *
     * @throws IllegalStateException if the header was not written (if it is required) or the writer was already closed.
     */
    public void add(FEATURE_TYPE vc);

}
