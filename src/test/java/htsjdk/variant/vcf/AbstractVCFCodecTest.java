package htsjdk.variant.vcf;

import htsjdk.tribble.TribbleException;
import htsjdk.variant.VariantBaseTest;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class AbstractVCFCodecTest extends VariantBaseTest {
	@Test
	public void shouldPreserveSymbolicAlleleCase() {
		VCFFileReader reader = new VCFFileReader(new File(VariantBaseTest.variantTestDataRoot + "breakpoint.vcf"), false);
		VariantContext variant = reader.iterator().next();
		reader.close();
		
		// VCF v4.1 s1.4.5
		// Tools processing VCF files are not required to preserve case in the allele String, except for IDs, which are case sensitive.
		Assert.assertTrue(variant.getAlternateAllele(0).getDisplayString().contains("chr12"));
	}

	@Test
	public void TestSpanDelParseAlleles(){
		List<Allele> list = VCF3Codec.parseAlleles("A", Allele.SPAN_DEL_STRING, 0);
	}

	@Test(expectedExceptions = TribbleException.class)
	public void TestSpanDelParseAllelesException(){
		List<Allele> list1 = VCF3Codec.parseAlleles(Allele.SPAN_DEL_STRING, "A", 0);
	}

	@DataProvider(name = "MultipleSNPs")
	public Object[][] getMultipleSNPsData() {
		return new Object[][] {
				{"A", "T,C", Arrays.asList(Allele.create("A", true), Allele.create("T"), Allele.create("C"))},
				{"A", "C,T", Arrays.asList(Allele.create("A", true), Allele.create("C"), Allele.create("T"))},
				{"A", "G,C,T", Arrays.asList(Allele.create("A", true), Allele.create("G"), Allele.create("C"), Allele.create("T"))},
				{"ATC", "G,AT", Arrays.asList(Allele.create("ATC", true), Allele.create("G"), Allele.create("AT"))},
				{"G", "ATC,AT", Arrays.asList(Allele.create("G", true), Allele.create("ATC"), Allele.create("AT"))}
		};
	}

	@Test(dataProvider = "MultipleSNPs")
	public void testMultipleSNPAlleleOrdering(final String refString, final String alts, final List<Allele> expectedOrderedAlleles) {
		final List<Allele> observed = VCF3Codec.parseAlleles(refString, alts, 0);
		Assert.assertEquals(observed.size(), expectedOrderedAlleles.size());
		for(int i = 0; i < expectedOrderedAlleles.size(); i++) {
			Assert.assertEquals(observed.get(i), expectedOrderedAlleles.get(i));
		}
	}

	@DataProvider(name="thingsToTryToDecode")
	public Object[][] getThingsToTryToDecode(){
		return new Object[][] {
				{"src/test/resources/htsjdk/tribble/tabix/testTabixIndex.vcf", true},
				{"src/test/resources/htsjdk/tribble/tabix/testTabixIndex.vcf.gz", true},
				{"src/test/resources/htsjdk/tribble/nonexistant.garbage", false},
				{"src/test/resources/htsjdk/tribble/testIntervalList.list", false}
		};
	}

	@Test(dataProvider = "thingsToTryToDecode")
	public void testCanDecodeFile(String potentialInput, boolean canDecode) {
		Assert.assertEquals(AbstractVCFCodec.canDecodeFile(potentialInput, VCFCodec.VCF4_MAGIC_HEADER), canDecode);
	}

}
