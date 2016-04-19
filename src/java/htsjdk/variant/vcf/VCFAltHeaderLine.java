/*
* Copyright (c) 2016 The Broad Institute
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package htsjdk.variant.vcf;

import htsjdk.variant.variantcontext.Allele;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Gómez Sánchez (magicDGS)
 */
public class VCFAltHeaderLine extends VCFHeaderLine implements VCFIDHeaderLine {

    // the type for the tag
    private final String type;
    private final String description;
    private final Allele allele;

    /**
     * Create an ALT allele header line
     *
     * @param type   allele type string
     * @param description the value for this header line
     */
    public VCFAltHeaderLine(String type, String description) {
        super(VCFConstants.ALT_HEADER_START, "");
        this.type = type;
        this.description = description;
        this.allele = Allele.create("<"+type+">");
    }

    /**
     * Create an ALT allele header line
     *
     * @param line   the header line
     * @param version      the VCF header version
     */
    public VCFAltHeaderLine(String line, VCFHeaderVersion version) {
        super(VCFConstants.ALT_HEADER_START, "");
        Map<String, String> map = VCFHeaderLineTranslator.parseLine(version, line, Arrays.asList("ID", "Description"));
        this.type = map.get("ID");
        this.description = map.get("Description");
    }

    @Override
    public String getID() {
        return type;
    }

    /**
     * Get the allele represented in this header
     *
     * @return symbolic allele
     */
    public Allele getAllele() {
        return allele;
    }
}
