package CPSC482Team.DSM;

import java.util.Random;

/**
 * Used for generating random DSM matrices
 */
public final class MatrixGenerator {
    private Random generator;

    /**
     * Creates a new {@Code MatrixGenerator}.
     *
     * @param generator The random number generator to use
     */
    public MatrixGenerator(Random generator) {
        this.generator = generator;
    }

    /**
     * Generates a DSM matrix containing random data.
     *
     * @param n The size of the matrix
     * @param nnz The number of non-zero elements in the matrix
     *
     * @return A {@Code DSMMatrix} containing random data
     */
    public DSMMatrix generate(int n, int nnz) {
        int[] rows = new int[nnz];
        int[] cols = new int[nnz];

        for (int i = 0; i < nnz; i++) {
            int row = 0;
            int col = 0;
            // Make sure nothing is on the diagonal
            do {
                row = this.generator.nextInt(n);    
                col = this.generator.nextInt(n);
            } while (row != col && elementExists(rows, cols, row, col));
        
            rows[i] = row;
            cols[i] = col;
        }

        return CSCBuilder.buildFromRowCol(n, nnz, rows, cols);
    }

    // Used to check if the provided row and column have an existing element in
    // the rows anc cols arrays
    private boolean elementExists(int[] rows, int[] cols, int row, int col) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == row && cols[i] == col) {
                return true;
            }
        }
        return false;
    }
}
