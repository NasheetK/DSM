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
            System.out.println("Nonzeros: " + dsm.nnz);

            // First we compute FBM and TFBD for the original ordering
            // so that we can see how much the permutation improves.
            long[] originalMetrics = DSMUtils.computeFeedbackMetrics(dsm);
            System.out.println("Original FBM: " + originalMetrics[0]);
            System.out.println("Original TFBD: " + originalMetrics[1]);

            // We time the Kosaraju + permutation pipeline on this input,
            // in the same spirit as in our 10x10 test.
            long startNanosKos = System.nanoTime();

            int[] permKos = KosarajuSCC.computePermutation(dsm);
            DSMMatrix permutedKos = DSMUtils.permute(dsm, permKos);
            long[] metricsKos = DSMUtils.computeFeedbackMetrics(permutedKos);

            long endNanosKos = System.nanoTime();
            double elapsedMillisKos = (endNanosKos - startNanosKos) / 1_000_000.0;

            System.out.println("Kosaraju + permutation runtime: " + elapsedMillisKos + " ms");
            System.out.println("Kosaraju FBM: " + metricsKos[0]);
            System.out.println("Kosaraju TFBD: " + metricsKos[1]);

            // We also run Tarjan's algorithm on the same DSM so that we
            // can compare runtimes and metrics for this input.
            long startNanosTarjan = System.nanoTime();

            int[] permTarjan = TarjanSCC.computePermutation(dsm);
            DSMMatrix permutedTarjan = DSMUtils.permute(dsm, permTarjan);
            long[] metricsTarjan = DSMUtils.computeFeedbackMetrics(permutedTarjan);

            long endNanosTarjan = System.nanoTime();
            double elapsedMillisTarjan = (endNanosTarjan - startNanosTarjan) / 1_000_000.0;

            System.out.println("Tarjan + permutation runtime: " + elapsedMillisTarjan + " ms");
            System.out.println("Tarjan FBM: " + metricsTarjan[0]);
            System.out.println("Tarjan TFBD: " + metricsTarjan[1]);

        } catch (Exception ex) {
            System.out.println("Error while processing DSM file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
