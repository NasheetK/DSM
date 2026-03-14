package CPSC482Team.DSM;

// In this class we collect small fixed DSM examples that we can plug
// into our tests. Having at least one 10x10 matrix helps us exercise
// the full algorithm pipeline without depending on external data files.
public final class TestMatrices {

    private TestMatrices() {
        // We keep this constructor private because this class is only
        // meant to host static factory methods for test matrices.
    }

    // We construct a hand-crafted 10x10 DSM here. We keep the structure
    // small but non-trivial so that the SCC algorithms see both cycles
    // and simple chains, while still being easy for us to reason about
    // when we inspect test output.
    //
    // We treat all indices as zero-based inside the code.
    public static DSMMatrix example10x10() {
        int n = 10;

        // In this example we describe dependencies as (row, col) pairs
        // meaning "row depends on col". We pick a few edges that create
        // a cycle among {0,1,2} plus a longer chain, so that we can see
        // how the permutation behaves on something slightly realistic.
        int[] row = {
                0, 1, 2,        // 0<-1, 1<-2, 2<-0 form a 3-cycle among {0,1,2}
                3, 4,          // 3<-2, 4<-3 create a chain 2->3->4
                5, 6, 7,       // 5<-4<-3, 6<-5, 7<-6 continue the chain
                8, 9,          // 8<-7, 9<-8
                4, 6           // 4<-7 and 6<-4 introduce another small cycle
        };

        int[] col = {
                1, 2, 0,
                2, 3,
                4, 5, 6,
                7, 8,
                7, 4
        };

        int nnz = row.length;

        return CSCBuilder.buildFromRowCol(n, nnz, row, col);
    }
}

