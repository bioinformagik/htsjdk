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

import htsjdk.tribble.Feature;
import htsjdk.tribble.annotation.Strand;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Simple GFF feature as defined in http://gmod.org/wiki/GFF3.
 *
 * @author Daniel Gomez-Sanchez (magicDGS)
 */
public final class GFFFeature implements Feature {

    public static final String MISSING_FIELD = ".";

    private String seqid;
    private int start;
    private int end;

    private String source;
    private String type;
    private Double score;
    private Strand strand;
    private Integer phase;
    private Map<String, Object> attributes;

    /** Constructor for empty feature. */
    public GFFFeature(final String contig, final int start, final int end) {
        this(contig, start, end, null, null, null, null, null, null);
    }

    /** Constructor for all the possible params. */
    public GFFFeature(final String contig, final int start, final int end,
            final String source, final String type, final Double score,
            final Strand strand, final Integer phase, final Map<String, Object> attributes) {
        this.seqid = contig;
        this.start = start;
        this.end = end;
        this.attributes = (attributes == null) ? new LinkedHashMap<>() : attributes;
        // setters validates the options if they are null
        setStrand(strand);
        setType(type);
        setSource(source);
        setScore(score);
        setPhase(phase);
    }

    /** Gets the contig. */
    @Override
    public String getContig() {
        return seqid;
    }

    /** Sets the contig. */
    public void setContig(final String contig) {
        if (contig == null) {
            throw new IllegalArgumentException("null contig");
        }
        this.seqid = contig;
    }

    /** Gets the start. */
    @Override
    public int getStart() {
        return start;
    }

    /** Sets the start. */
    public void setStart(final int start) {
        this.start = start;
    }

    /** Gents the end. */
    @Override
    public int getEnd() {
        return end;
    }

    /** Sets the end. */
    public void setEnd(final int end) {
        this.end = end;
    }

    /**
     * Gets the source.
     * @throws NoSuchElementException if the source is missing.
     */
    public String getSource() {
        if (!hasSource()) {
            throw new NoSuchElementException("source");
        }
        return source;
    }

    /** Returns {@code true} if source is not missing; {@code false} otherwise. */
    public boolean hasSource() {
        return source != null;
    }

    /** Sets the source. To clean the source, set to {@code null} or {@link #MISSING_FIELD}. */
    public void setSource(final String source) {
        if (source == null || source.equals(MISSING_FIELD)) {
            this.source = null;
        } else {
            this.source = source;
        }
    }

    /**
     * Gets the type.
     * @throws NoSuchElementException if the type is missing.
     */
    public String getType() {
        if (!hasType()) {
            throw new NoSuchElementException("type");
        }
        return type;
    }

    /** Sets the type. To clean the type, set to {@code null} or {@link #MISSING_FIELD}. */
    public void setType(final String type) {
        if (type == null || type.equals(MISSING_FIELD)) {
            this.type = null;
        } else {
            this.type = type;
        }
    }

    /** Returns {@code true} if type is not missing; {@code false} otherwise. */
    public boolean hasType() {
        return type != null;
    }

    /**
     * Gets the score.
     * @throws NoSuchElementException if the score is missing.
     */
    public double getScore() {
        if (!hasScore()) {
            throw new NoSuchElementException("score");
        }
        return score;
    }

    /** Sets the score. To clean the score, set to {@code null}. */
    public void setScore(final Double score) {
        if (score == null) {
            this.score = null;
        } else {
            this.score = score;
        }
    }

    /** Returns {@code true} if score is not missing; {@code false} otherwise. */
    public boolean hasScore() {
        return score != null;
    }

    /** Gets the strand. */
    public Strand getStrand() {
        return strand;
    }

    /** Sets the strand. To clean the strand, set to {@code null} or {@link Strand#NONE}. */
    public void setStrand(final Strand strand) {
        if (strand == null) {
            this.strand = Strand.NONE;
        } else {
            this.strand = strand;
        }
    }

    /**
     * Gets the phase.
     * @throws NoSuchElementException if the phase is missing.
     */
    public int getPhase() {
        if (!hasPhase()) {
            throw new NoSuchElementException("phase");
        }
        return phase;
    }

    /**
     * Sets the phase. To clean the phase, set to {@code null}.
     * @throws IllegalArgumentException if the phase is not 0, 1 or 2.
     */
    public void setPhase(final Integer phase) {
        if (phase == null) {
            this.phase = null;
        } else {
            switch (phase) {
                case 0:
                case 1:
                case 2:
                    this.phase = phase;
                    break;
                default:
                    throw new IllegalArgumentException("Not recognized phase: " + phase);
            }
        }

    }

    /** Returns {@code true} if phase is not missing; {@code false} otherwise. */
    public boolean hasPhase() {
        return phase != null;
    }

    /** Returns an unmodifiable map with all the attributes. */
    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /** Replace all the attributes and set the new ones. */
    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /** Clear all the attributes. */
    public void clearAttributes() {
        this.attributes.clear();
    }

    /**
     * Gets the value for the attribute.
     * @throws NoSuchElementException if the attribute is not present.
     */
    public Object getAttribute(final String tag) {
        if (!hasAttribute(tag)) {
            throw new NoSuchElementException(tag);
        }
        return attributes.get(tag);
    }

    /** Sets an attribute to a value. */
    public void setAttributeValue(final String tag, final Object value) {
        this.attributes.put(tag, value);
    }

    /** Returns {@code true} if the attribute is present; {@code false} otherwise*/
    public boolean hasAttribute(final String tag) {
        return attributes.containsKey(tag);
    }

    /** Returns the feature encoded as a GFF3 string. */
    public String asGff3String() {
        return String.join("\t",
                getContig(),
                (hasSource()) ? getSource() : MISSING_FIELD,
                (hasType()) ? getType() : MISSING_FIELD,
                Integer.toString(getStart()), Integer.toString(getEnd()),
                (hasScore()) ? Double.toString(getScore()) : MISSING_FIELD,
                strandString(),
                (hasPhase()) ? Integer.toString(getPhase()) : MISSING_FIELD,
                attributesToString());
    }

    // convert the attributes to a String format
    private String attributesToString() {
        final List<String> tagValue = getAllAttributes().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());

        return String.join(";", tagValue);
    }

    private String strandString() {
        switch (strand) {
            case NONE:
                return ".";
            case POSITIVE:
                return "+";
            case NEGATIVE:
                return "-";
            default:
                throw new RuntimeException("Should not be reached");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GFFFeature)) {
            return false;
        }

        GFFFeature feature = (GFFFeature) o;

        if (start != feature.start) {
            return false;
        }
        if (end != feature.end) {
            return false;
        }
        if (!seqid.equals(feature.seqid)) {
            return false;
        }
        if (source != null ? !source.equals(feature.source) : feature.source != null) {
            return false;
        }
        if (type != null ? !type.equals(feature.type) : feature.type != null) {
            return false;
        }
        if (score != null ? !score.equals(feature.score) : feature.score != null) {
            return false;
        }
        if (strand != feature.strand) {
            return false;
        }
        if (phase != null ? !phase.equals(feature.phase) : feature.phase != null) {
            return false;
        }
        return attributes.equals(feature.attributes);

    }

    @Override
    public int hashCode() {
        int result = seqid.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + strand.hashCode();
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        result = 31 * result + attributes.hashCode();
        return result;
    }
}
