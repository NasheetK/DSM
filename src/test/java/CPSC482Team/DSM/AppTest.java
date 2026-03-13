package CPSC482Team.DSM;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * In this test class we start exercising our DSM and algorithm code.
 * We keep the tests small so that we can quickly see if a change breaks
 * anything before we move on to larger matrices.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * In this test we run our 10x10 DSM through the full Kosaraju
     * pipeline and make sure that everything behaves in a consistent
     * way. As we extend the project we can refine this test to check
     * specific expected FBM and TFBD values.
     */
    public void testExample10x10Kosaraju()
    {
        DSMMatrix dsm = TestMatrices.example10x10();
        assertEquals(10, dsm.n);

        // We print the original matrix so that we can visually confirm
        // the pattern of dependencies in our fixed 10x10 example.
        System.out.println("Original 10x10 DSM:");
        System.out.println(DSMUtils.toDenseString(dsm));

        // Here we time how long the full Kosaraju pipeline takes on
        // our 10x10 matrix. This is mainly for curiosity and to make
        // sure our code is not doing anything unexpectedly slow.
        long startNanos = System.nanoTime();

        int[] perm = KosarajuSCC.computePermutation(dsm);
        assertEquals(10, perm.length);

        DSMMatrix permuted = DSMUtils.permute(dsm, perm);
        long[] metrics = DSMUtils.computeFeedbackMetrics(permuted);

        long endNanos = System.nanoTime();
        double elapsedMillis = (endNanos - startNanos) / 1_000_000.0;
        System.out.println("Kosaraju + permutation runtime on 10x10 DSM: "
                           + elapsedMillis + " ms");

        // We also print the permuted matrix so that we can see how our
        // ordering changes the structure of the DSM.
        System.out.println("Permuted 10x10 DSM:");
        System.out.println(DSMUtils.toDenseString(permuted));

        // For now we only verify that the feedback metrics are
        // well-defined and non-negative for this example.
        assertTrue(metrics[0] >= 0);
        assertTrue(metrics[1] >= 0);
    }
}
