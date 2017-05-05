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

import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.LineIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple codec for {@link GFFFeature}s.
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public final class GFFCodec extends AsciiFeatureCodec<GFFFeature> {

    public static final List<String> GFF_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(
       ".gff3", ".gff", ".gtf", ".gvf"
    ));

    /** Default constructor. */
    public GFFCodec() {
        super(GFFFeature.class);
    }

    @Override
    public GFFFeature decode(final String line) {
        // skip emtpy lines and comments
        if (line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        final String[] tokens = line.split("\t");
        if (tokens.length != 9) {
            throw new TribbleException.InvalidDecodeLine("Incorrect number of columns", line);
        }
        // get a simple GFFFeature
        final GFFFeature feature = new GFFFeature(tokens[0], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));

        feature.setSource(tokens[1]);
        feature.setType(tokens[2]);

        if (!GFFFeature.MISSING_FIELD.equals(tokens[5])) {
            feature.setScore(Double.parseDouble(tokens[5]));
        }

        // TODO: make '?' a new strand definition?
        if (!(GFFFeature.MISSING_FIELD.equals(tokens[6]) || "?".equals(tokens[6]))) {
            feature.setStrand(Strand.toStrand(tokens[6]));
        }

        if (!GFFFeature.MISSING_FIELD.equals(tokens[7])) {
            feature.setPhase(Integer.parseInt(tokens[7]));
        }

        parseAttributes(feature, tokens[8]);

        return feature;
    }

    // parse the attributes and append them to the feature
    private void parseAttributes(final GFFFeature feature, final String attributeColumn) {
        final String[] attributePairs = attributeColumn.split(";");
        for (final String tagValue: attributePairs) {
            final String[] split = tagValue.split("=");
            if (split.length != 2) {
                throw new TribbleException(String.format("Invalid tag=value pair (%s) for attributes: %s", tagValue, attributeColumn));
            }
            feature.setAttributeValue(split[0], split[1]);
        }
    }

    /** Header is not currently supported for GFF features.*/
    @Override
    public Object readActualHeader(final LineIterator reader) {
        return null;
    }

    /** Returns {@code true} if the file ends with the {@link #GFF_EXTENSIONS} (maybe gzipped); {@code false} otherwise. */
    @Override
    public boolean canDecode(final String path) {
        final String toDecode;
        if (AbstractFeatureReader.hasBlockCompressedExtension(path)) {
            toDecode = path.substring(0, path.lastIndexOf("."));
        } else {
            toDecode = path;
        }
        return GFF_EXTENSIONS.stream().anyMatch(ext -> toDecode.toLowerCase().endsWith(ext));
    }

    @Override
    public TabixFormat getTabixFormat() {
        return TabixFormat.GFF;
    }
}
