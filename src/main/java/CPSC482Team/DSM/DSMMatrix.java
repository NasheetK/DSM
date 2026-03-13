package CPSC482Team.DSM;

/**
 * In this class we capture the structure of a sparse DSM matrix in
 * compressed sparse column (CSC) format. We always treat the matrix
 * as square with n rows and n columns, and we only store nonzero entries.
 *
 * We keep this data holder deliberately small so that we can focus our
 * thinking and testing effort on the algorithms that use it.
 */
public class DSMMatrix {

    /** Number of rows (and columns) in the DSM. */
    public final int n;

    /** Number of stored nonzero entries in the DSM. */
    public final int nnz;

    /**
     * Column pointer array of length n + 1. For a column j, all nonzero
     * entries in that column live in rowInd between colPtr[j] (inclusive)
     * and colPtr[j + 1] (exclusive).
     *
     * We use zero-based indexing in Java, so column indices range from
     * 0 to n - 1 and colPtr indices range from 0 to n.
     */
    public final int[] colPtr;

    /**
     * Row index array of length nnz. Each entry rowInd[k] stores the
     * row index (zero-based) of the k-th nonzero entry in column-major
     * order as determined by colPtr.
     */
    public final int[] rowInd;

    /**
     * Once we call this constructor all four fields are initialised,
     * so the resulting DSMMatrix instance is ready to be passed into
     * our algorithms without any further setup.
     */
    public DSMMatrix(int n, int nnz, int[] colPtr, int[] rowInd) {
        this.n = n;
        this.nnz = nnz;
        this.colPtr = colPtr;
        this.rowInd = rowInd;
    }
}

