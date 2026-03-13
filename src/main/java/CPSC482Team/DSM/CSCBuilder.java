package CPSC482Team.DSM;

/**
 * In this helper class we collect the code we use to build a CSC matrix
 * from a list of (row, column) coordinates. By keeping this logic in one
 * place we make it easier for us to trust that both our small fixed
 * examples and our larger matrices behave in the same way.
 */
public final class CSCBuilder {

    private CSCBuilder() {
        // We do not want anyone to create an instance of this helper,
        // because everything we need here lives in static methods.
    }

    /**
     * We use this method to build a CSC representation from arrays of
     * row and column indices. Our assumptions are:
     * - n is the dimension of the n x n matrix,
     * - row[k] and col[k] store zero-based indices for the k-th nonzero,
     * - we have nnz nonzero entries overall.
     *
     * The construction follows the standard three step pattern:
     *   1) we count how many nonzeros live in each column,
     *   2) we turn those counts into prefix sums to get starting offsets,
     *   3) we place each (row, col) into its slot in column order.
     */
    public static DSMMatrix buildFromRowCol(int n, int nnz, int[] row, int[] col) {
        int[] colPtr = new int[n + 1];
        int[] rowInd = new int[nnz];

        // First we count how many entries fall into each column so we
        // know how large each column segment in rowInd needs to be.
        for (int k = 0; k < nnz; k++) {
            int j = col[k];
            colPtr[j] += 1;
        }

        // Next we turn these counts into prefix sums so that colPtr[j]
        // becomes the starting index for column j in the rowInd array.
        int cumulative = 0;
        for (int j = 0; j < n; j++) {
            int count = colPtr[j];
            colPtr[j] = cumulative;
            cumulative += count;
        }
        colPtr[n] = cumulative;

        // We keep a working copy of colPtr so we can fill rowInd
        // from left to right without losing the true column boundaries.
        int[] next = new int[n + 1];
        System.arraycopy(colPtr, 0, next, 0, n + 1);

        // Finally we walk through all nonzeros again and drop each row
        // index into the correct column slice using our running offsets.
        for (int k = 0; k < nnz; k++) {
            int j = col[k];
            int i = row[k];
            int dest = next[j];
            rowInd[dest] = i;
            next[j] = dest + 1;
        }

        return new DSMMatrix(n, nnz, colPtr, rowInd);
    }
}

