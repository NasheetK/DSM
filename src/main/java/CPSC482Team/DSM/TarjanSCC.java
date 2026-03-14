package CPSC482Team.DSM;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

// In this class we implement Tarjan's algorithm for strongly connected
// components on top of our CSC-based DSMMatrix. The goal here is the
// same as for Kosaraju: we want a permutation of tasks that groups SCCs
// into blocks and orders those blocks topologically.
public final class TarjanSCC {

    private TarjanSCC() {
        // We only use the static methods defined below.
    }

    // We use this method to run Tarjan's algorithm end-to-end:
    //   1) we run a single depth-first search on the original graph to
    //      assign each vertex to a strongly connected component,
    //   2) we build the component-level DAG and topologically order it,
    //   3) we build a permutation that lists vertices by component order
    //      and applies a small heuristic inside each component.
    //
    // The returned array perm satisfies: perm[k] is the original vertex
    // index we place in row and column k of the reordered matrix.
    public static int[] computePermutation(DSMMatrix g) {
        int n = g.n;

        int[] index = new int[n];
        int[] lowlink = new int[n];
        boolean[] onStack = new boolean[n];
        int[] componentOf = new int[n];

        // We use 1-based indices for the Tarjan index counter so that
        // 0 means "unvisited".
        int[] indexCounter = new int[]{1};
        int[] currentComponent = new int[]{0};
        Deque<Integer> stack = new ArrayDeque<>();

        for (int v = 0; v < n; v++) {
            if (index[v] == 0) {
                strongConnect(g, v, index, lowlink, onStack, stack, indexCounter, componentOf, currentComponent);
            }
        }

        // Once we have component ids we can group vertices by component,
        // build the component DAG, and order the components.
        List<List<Integer>> components = groupVerticesByComponent(componentOf, currentComponent[0]);
        int[] compOrder = KosarajuSCC.topologicalOrderOfComponentDag(g, componentOf, components.size());

        // Finally we assemble the permutation, reusing the same
        // intra-component ordering heuristic as in Kosaraju.
        return KosarajuSCC.buildPermutationFromComponents(g, components, compOrder);
    }

    private static void strongConnect(DSMMatrix g,
                                      int v,
                                      int[] index,
                                      int[] lowlink,
                                      boolean[] onStack,
                                      Deque<Integer> stack,
                                      int[] indexCounter,
                                      int[] componentOf,
                                      int[] currentComponent) {
        index[v] = indexCounter[0];
        lowlink[v] = indexCounter[0];
        indexCounter[0] += 1;

        stack.push(v);
        onStack[v] = true;

        // We follow outgoing edges v -> u by walking down column v.
        for (int p = g.colPtr[v]; p < g.colPtr[v + 1]; p++) {
            int u = g.rowInd[p];
            if (index[u] == 0) {
                strongConnect(g, u, index, lowlink, onStack, stack, indexCounter, componentOf, currentComponent);
                lowlink[v] = Math.min(lowlink[v], lowlink[u]);
            } else if (onStack[u]) {
                lowlink[v] = Math.min(lowlink[v], index[u]);
            }
        }

        // If v is a root vertex, we pop the stack to form one SCC.
        if (lowlink[v] == index[v]) {
            int compId = currentComponent[0];
            currentComponent[0] = compId + 1;

            while (true) {
                int w = stack.pop();
                onStack[w] = false;
                componentOf[w] = compId;
                if (w == v) {
                    break;
                }
            }
        }
    }

    private static List<List<Integer>> groupVerticesByComponent(int[] componentOf, int compCount) {
        List<List<Integer>> groups = new ArrayList<>(compCount);
        for (int c = 0; c < compCount; c++) {
            groups.add(new ArrayList<>());
        }

        for (int v = 0; v < componentOf.length; v++) {
            int c = componentOf[v];
            groups.get(c).add(v);
        }

        return groups;
    }
}

