package CPSC482Team.DSM;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// In this test class we start exercising our DSM and algorithm code.
// We keep the tests small so that we can quickly see if a change breaks
// anything before we move on to larger matrices.
public class AppTest 
    extends TestCase
{
    // Create the test case with the given name.
    public AppTest( String testName )
    {
        super( testName );
    }

    // Return the suite of tests being run.
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    // In this test we run our 10x10 DSM through both Kosaraju and
    // Tarjan pipelines and make sure that everything behaves in a
    // consistent way. This gives us a small, fixed example where we
    // can compare runtimes and FBM/TFBD for the two algorithms.
    public void testExample10x10Kosaraju()
    {
        DSMMatrix dsm = TestMatrices.example10x10();
        assertEquals(10, dsm.n);

        // We print the original matrix so that we can visually confirm
        // the pattern of dependencies in our fixed 10x10 example.
        System.out.println("Original 10x10 DSM:");
        System.out.println(DSMUtils.toDenseString(dsm));

        // We also compute FBM and TFBD for the original ordering so
        // that we can compare before and after the permutation.
        long[] originalMetrics = DSMUtils.computeFeedbackMetrics(dsm);
        System.out.println("Original FBM: " + originalMetrics[0]);
        System.out.println("Original TFBD: " + originalMetrics[1]);

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

        // We now run Tarjan's algorithm on the same matrix so that we
        // can compare runtime and metrics with Kosaraju on the 10x10
        // example.
        long tarjanStart = System.nanoTime();

        int[] permTarjan = TarjanSCC.computePermutation(dsm);
        assertEquals(10, permTarjan.length);

        DSMMatrix permutedTarjan = DSMUtils.permute(dsm, permTarjan);
        long[] metricsTarjan = DSMUtils.computeFeedbackMetrics(permutedTarjan);

        long tarjanEnd = System.nanoTime();
        double tarjanMillis = (tarjanEnd - tarjanStart) / 1_000_000.0;

        System.out.println("Tarjan + permutation runtime on 10x10 DSM: "
                           + tarjanMillis + " ms");
        System.out.println("Tarjan FBM: " + metricsTarjan[0]);
        System.out.println("Tarjan TFBD: " + metricsTarjan[1]);

        assertTrue(metricsTarjan[0] >= 0);
        assertTrue(metricsTarjan[1] >= 0);
    }
}
