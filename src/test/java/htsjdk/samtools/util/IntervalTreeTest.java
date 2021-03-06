/*
 * The MIT License
 *
 * Copyright (c) 2010 The Broad Institute
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
package htsjdk.samtools.util;

import htsjdk.HtsjdkTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static htsjdk.samtools.util.IntervalTree.Node.HAS_OVERLAPPING_PART;

/**
 * @author alecw@broadinstitute.org
 */
@Test(singleThreaded=true) // to assure that the common resources aren't clobbered
public class IntervalTreeTest extends HtsjdkTest {
    @Test
    public void testNoMatches()
    {
        // Test empty tree
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        Iterator<IntervalTree.Node<String>> results = intervalTree.overlappers(1, 500);
        Assert.assertEquals(countElements(results), 0, "Testing with no left-hand set failed.");

        // Test no matches at all
        intervalTree.put(1, 400, "foo");
        intervalTree.put(600, 800, "foo2");
        results = intervalTree.overlappers(450, 599);
        Assert.assertEquals(countElements(results), 0, "Testing with no overlaps at all.");

    }

    private int countElements(final Iterator<IntervalTree.Node<String>> it) {
        int ret = 0;
        while (it.hasNext()) {
            ++ret;
            it.next();
        }
        return ret;
    }

    private final IntervalTree<String> intervalTree = new IntervalTree<String>();

    @BeforeMethod
    public void init(){ //due to the destructive nature of removeMany test...
        intervalTree.clear();

        // each interval has a "name:length"
        intervalTree.put(1, 10, "foo1:10");
        intervalTree.put(2, 9, "foo2:8");
        intervalTree.put(3, 8, "foo3:6");
        intervalTree.put(4, 7, "foo4:4");
        intervalTree.put(5, 6, "foo5:2");
        intervalTree.put(1, 9, "foo6:9");
    }

    @Test
    public void testLength(){

        Iterator<IntervalTree.Node<String>> iterator = intervalTree.iterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> node : iterable) {
            Assert.assertEquals(node.getLength(), Integer.parseInt(node.getValue().replaceAll(".*:", "")));
        }
    }

    @DataProvider(name="adjacentIntervalsTestData")
    public Object[][] adjacentIntervalsTestData() {
        return new Object[][]{
                {1, 4, 5, 10, true},
                {1, 3, 5, 10, false},
                {1, 4, 6, 10, false},
                {1, 2, 6, 10, false},
                {1, 10, 6, 10, false},
                {1, 10, 11, 20, true},
                {1, 10, 11, 20, true},
        };
    }

    @Test(dataProvider = "adjacentIntervalsTestData")
    public void testAdjacent(int start1, int end1, int start2, int end2, boolean areAdjacent){

        final IntervalTree.Node<String> node1 = new IntervalTree.Node<>(start1, end1, "one");
        final IntervalTree.Node<String> node2 = new IntervalTree.Node<>(start2, end2, "two");

        Assert.assertTrue(node1.isAdjacent(node2) == areAdjacent);
        Assert.assertTrue(node2.isAdjacent(node1) == areAdjacent);
    }


    @Test
    public void testRank() {
        for (IntervalTree.Node<String> node: intervalTree) {
            Assert.assertEquals(intervalTree.findByIndex(
                    intervalTree.getIndex(node.getStart(), node.getEnd())), node);
        }
    }

    @Test
    public void testIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.iterator(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            Assert.assertTrue(node.compare(testNode.getStart(), testNode.getEnd()) <= 0);
            count++;
        }
        Assert.assertEquals(count, 3); // foobar3, foobar4, and foobar5 only.
    }

    @Test
    public void testRemoveMany() {
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> node : iterable) {
            intervalTree.removeNode(node);
        }
        Assert.assertEquals(intervalTree.size(), 0);
    }

    @Test
    public void testRevIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            Assert.assertTrue(node.compare(testNode.getStart(), testNode.getEnd()) >= 0);
            count++;
        }
        Assert.assertEquals(count, 3); // foobar1, foobar2, and foobar6
    }


    @Test
    public void testOverlapIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.overlappers(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            Assert.assertTrue( (testNode.getRelationship(node) & HAS_OVERLAPPING_PART) != 0, String.format("%s with %s = %d", node.toString(), testNode.toString(), node.getRelationship(testNode)));
            count++;
        }
        Assert.assertEquals(count, 5); // foobar1, foobar2, foobar3, foobar4, and foobar6
    }


    @Test
    public void testTotalRevIterator() {

        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> ignored : iterable) {
            count++;
        }
        Assert.assertEquals(count, intervalTree.size()); // foobar1, foobar2, and foobar6
    }

    @Test
    public void testMatches()
    {
        // Single match
        Assert.assertEquals(countElements(intervalTree.overlappers(10, 10)), 1, "Test single overlap");
        Assert.assertTrue(iteratorContains(intervalTree.overlappers(10, 10), "foo1:10"), "Test single overlap for correct overlapee");

        // Multiple matches
        Assert.assertEquals(countElements(intervalTree.overlappers(7, 8)), 5, "Test multiple overlap");
        Assert.assertTrue( iteratorContains(intervalTree.overlappers(7, 8), "foo1:10"), "Test multiple overlap for correct overlapees");
        Assert.assertTrue( iteratorContains(intervalTree.overlappers(7, 8), "foo2:8"), "Test multiple overlap for correct overlapees");
        Assert.assertTrue( iteratorContains(intervalTree.overlappers(7, 8), "foo3:6"), "Test multiple overlap for correct overlapees");
        Assert.assertTrue( iteratorContains(intervalTree.overlappers(7, 8), "foo4:4"), "Test multiple overlap for correct overlapees");
        Assert.assertTrue( iteratorContains(intervalTree.overlappers(7, 8), "foo6:9"), "Test multiple overlap for correct overlapees");
        Assert.assertTrue(!iteratorContains(intervalTree.overlappers(7, 8), "foo5:2"), "Test multiple overlap for correct overlapees");
    }

    private boolean iteratorContains(final Iterator<IntervalTree.Node<String>> nodeIterator, final String s) {
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next().getValue().equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testNearEnds()
    {
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        intervalTree.put(10, 20, "foo");
        Assert.assertEquals(countElements(intervalTree.overlappers(10, 10)), 1, "Test overlap (no buffers) at near end exactly");
        Assert.assertEquals(countElements(intervalTree.overlappers(9, 10)), 1, "Test overlap (no buffers) at near end exactly");
        Assert.assertEquals(countElements(intervalTree.overlappers(9, 9)), 0, "Test just before overlap (no buffers)");
        Assert.assertEquals(countElements(intervalTree.overlappers(20, 20)), 1, "Test overlap (no buffers) at far end exactly");
        Assert.assertEquals(countElements(intervalTree.overlappers(20, 21)), 1, "Test overlap (no buffers) at far end exactly");
        Assert.assertEquals(countElements(intervalTree.overlappers(21, 21)), 0, "Test just beyond overlap (no buffers)");
    }

    @Test
    public void performanceTest()
    {
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        final long start = System.currentTimeMillis();
        for (int i = 1; i <= 50000; i++)  intervalTree.put(i, i, "frob");
        System.out.println("Time to construct a tree with 50000 nodes: " + (System.currentTimeMillis() - start) + " milliseconds" );

        final long end   = System.currentTimeMillis() + 10000;
        int count = 0;
        while (System.currentTimeMillis() < end) {
            intervalTree.overlappers(17000, 17099);
            ++count;
        }
        System.out.println("Queried for the same 100-length mapping " + count + " times in 10 seconds.");
    }

    @Test
    public void testHandlingOfDuplicateMappings()
    {
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        intervalTree.put(1, 10, "foo1");
        // This call replaces foo1 with foo2
        Assert.assertEquals(intervalTree.put(1, 10, "foo2"), "foo1");
        intervalTree.put(2, 8, "foo3");

        Assert.assertEquals(countElements(intervalTree.overlappers(3, 5)), 2);
        Assert.assertFalse(iteratorContains(intervalTree.overlappers(3, 5), "foo1"));
        Assert.assertTrue(iteratorContains(intervalTree.overlappers(3, 5), "foo2"));
        Assert.assertTrue(iteratorContains(intervalTree.overlappers(3, 5), "foo3"));
    }

    /**
     * Test of PIC-123
     */
    @Test
    public void testRemove() {
        int[][] adds = {
                {46129744, 46129978},
                {46393843, 46394077},
                {46260491, 46260725},
                {46402360, 46402594},
                {46369255, 46369464},
                {46293772, 46293981},
                {46357687, 46357896},
                {46431752, 46431961},
                {46429997, 46430206},
                {46404026, 46404192},
                {46390511, 46390677},
                {46090593, 46090759},
                {46045352, 46045518},
                {46297633, 46297799},
                {46124297, 46124463},
                {46395291, 46395504},
                {46439072, 46439240},
                {46400792, 46400959},
                {46178616, 46178851},
                {46129747, 46129982},
                {46396546, 46396781},
                {46112353, 46112588},
                {46432996, 46433231},
                {46399109, 46399344},
                {46372058, 46372292},
                {46386826, 46387060},
                {46381795, 46382029},
                {46179789, 46180023},
                {46394409, 46394643},
                {46376176, 46376429},
                {46389943, 46390177},
                {46433654, 46433888},
                {46379440, 46379674},
                {46391117, 46391351},
        };
        IntervalTree<String> intervalTree = new IntervalTree<String>();
        for (int[] add : adds) {
            intervalTree.put(add[0], add[1], "frob");
        }
        Assert.assertEquals(intervalTree.remove(46402360, 46402594), "frob");
        intervalTree.checkMaxEnds();
    }


}
