package CPSC482Team.DSM;

import java.io.File;

// In this main class we provide a simple command-line interface that
// lets us point to a Matrix Market file (for example in the TestData
// folder) and run the main analysis steps:
//
// - read the DSM file and build the CSC structure,
// - compute original FBM and TFBD,
// - run Kosaraju's algorithm to get a permutation and report runtime
//   and FBM/TFBD,
// - run Tarjan's algorithm to get a permutation and report runtime
//   and FBM/TFBD.
public class App
{
    public static void main(String[] args)
    {
        if (args.length < 1) {
            System.out.println("Usage: java CPSC482Team.DSM.App <path-to-matrix-market-file>");
            System.out.println("Example (from project root):");
            System.out.println("  mvn exec:java -Dexec.mainClass=CPSC482Team.DSM.App"
                    + " -Dexec.args=\"TestData/EVA.mtx\"");
            return;
        }

        String filePath = args[0];
        System.out.println("Reading DSM from file: " + filePath);

        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) {
            System.out.println("The file \"" + filePath + "\" does not exist or is not a regular file.");
            return;
        }

        try {
            // We read the Matrix Market file into our CSC-backed DSMMatrix.
            DSMMatrix dsm = MatrixMarketReader.read(filePath);
            System.out.println("Matrix size: " + dsm.n + " x " + dsm.n);
            System.out.println("Number of nonzeros: " + dsm.nnz);

            // First we compute FBM and TFBD for the original ordering
            // so that we can see how much the permutation improves.
            long[] originalMetrics = DSMUtils.computeFeedbackMetrics(dsm);
            System.out.println("Original FBM: " + originalMetrics[0]);
            System.out.println("Original TFBD: " + originalMetrics[1]);

            // We run Tarjan's algorithm on the DSM so that we
            // generate a matrix that has the number of feedback depenencies as 
            // few as possible.
            long startNanosFewAbove = System.nanoTime();

            int[] permTarjan = TarjanSCC.computePermutation(dsm);
            DSMMatrix permutedTarjan = DSMUtils.permute(dsm, permTarjan);
            long[] metricsFewAbove = DSMUtils.computeFeedbackMetrics(permutedTarjan);

            long endNanosFewAbove = System.nanoTime();
            double elapsedMillisFewAbove = (endNanosFewAbove - startNanosFewAbove) / 1_000_000.0;

            System.out.println("Minimum feedback dependencies above + permutation runtime: " + elapsedMillisFewAbove + " ms");
            System.out.println("#FBM for Minimum feedback dependencies above: " + metricsFewAbove[0]);
            System.out.println("Total FBD for Minimum feedback dependencies above: " + metricsFewAbove[1]);

            // Next, we permute the DSM so that the average distance from the diagonal
            // is minimized. This is done with a greedy algorithm.
            long startNanosClosest = System.nanoTime();

            int[] closestPerm = FBDOptimizer.optimize(permutedTarjan);
            DSMMatrix closest = DSMUtils.permute(permutedTarjan, closestPerm);
            long[] metricsClosest = DSMUtils.computeFeedbackMetrics(closest);

            long endNanosClosest = System.nanoTime();
            double elapsedMillisClosest = (endNanosClosest - startNanosClosest) / 1_000_000.0;
            
            System.out.println("Minimum feedback distance from diagonal + permutation runtime: " + elapsedMillisClosest + " ms");
            System.out.println("#FBM for miniumum feedback distance from diagonal: " + metricsClosest[0]);
            System.out.println("Total FBD for miniumum feedback distance for diagonal: " + metricsClosest[1]);

        } catch (Exception ex) {
            System.out.println("Error while processing DSM file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
