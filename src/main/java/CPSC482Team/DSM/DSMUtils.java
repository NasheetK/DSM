package CPSC482Team.DSM;

/**
 * In this helper class we code up small utility methods that operate on
 * DSMMatrix instances. We need a permutation helper and a way to compute
 * feedback metrics right now, and we can extend this class later as the
 * project grows.
 */
public final class DSMUtils {

    private DSMUtils() {
        // We avoid creating instances of this class by keeping the
        // constructor private and only exposing static methods.
    }

    /**
     * We use this method to permute the rows and columns of a DSM
     * according to a given permutation. We assume that perm[k] is the
     * original vertex index that we want to place into row and column k.
     *
     * To do this, we map every stored nonzero entry into its new
     * (row, col) location and then rebuild CSC using our builder.
     */
    public static DSMMatrix permute(DSMMatrix g, int[] perm) {
        int n = g.n;
        int nnz = g.nnz;

        int[] positionOf = new int[n]; // positionOf[v] tells us where v moves to.
        for (int k = 0; k < n; k++) {
            int v = perm[k];
            positionOf[v] = k;
        }

        int[] row = new int[nnz];
        int[] col = new int[nnz];
        int k = 0;

        for (int j = 0; j < n; j++) {
            for (int p = g.colPtr[j]; p < g.colPtr[j + 1]; p++) {
                int i = g.rowInd[p];
                int newRow = positionOf[i];
                int newCol = positionOf[j];
                row[k] = newRow;
                col[k] = newCol;
                k += 1;
            }
        }

        return CSCBuilder.buildFromRowCol(n, nnz, row, col);
    }

    /**
     * Here we compute the two feedback metrics defined in the project
     * description for a permuted DSM:
     *
     * - FBM is the count of nonzeros strictly above the main diagonal,
     * - TFBD is the sum of (col - row) over those entries.
     *
     * We return the two values as a small array so that we can easily
     * print or assert them in our tests.
     */
    public static long[] computeFeedbackMetrics(DSMMatrix g) {
        long fbm = 0L;
        long tfbd = 0L;

        for (int j = 0; j < g.n; j++) {
            for (int p = g.colPtr[j]; p < g.colPtr[j + 1]; p++) {
                int i = g.rowInd[p];
                if (j > i) {
                    fbm += 1L;
                    tfbd += (long) (j - i);
                }
            }
        }

        return new long[]{fbm, tfbd};
    }

    /**
     * We use this method when we want to see the full 0/1 matrix that a
     * DSMMatrix represents. It is mainly for debugging and for printing
     * small test examples like our 10x10 case.
     *
     * We build an n x n dense array in memory and then turn it into a
     * string with one row per line.
     */
    public static String toDenseString(DSMMatrix g) {
        int n = g.n;
        int[][] dense = new int[n][n];

        // First we populate the dense array from the CSC structure.
        for (int j = 0; j < n; j++) {
            for (int p = g.colPtr[j]; p < g.colPtr[j + 1]; p++) {
                int i = g.rowInd[p];
                dense[i][j] = 1;
            }
        }

        // Then we build up a simple string representation row by row.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sb.append(dense[i][j]);
                if (j + 1 < n) {
                    sb.append(' ');
                }
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}

