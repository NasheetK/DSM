package CPSC482Team.DSM;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// In this class we implement Kosaraju's algorithm on top of our CSC
// representation of the DSM. Our goal is to compute a permutation of
// the tasks that groups strongly connected components into blocks and
// then orders those blocks in a topological order.
public final class KosarajuSCC {

    private KosarajuSCC() {
        // We only call the static methods in this class, so we do not
        // want anyone to create an instance of it.
    }

    // We use this method to run the full Kosaraju pipeline:
    //   1) we run a depth-first search on the original graph to get a
    //      finish-time order of vertices,
    //   2) we build the transpose and run another depth-first search in
    //      that order to identify strongly connected components,
    //   3) we build a DAG of components and topologically order them,
    //   4) we assemble a permutation that lists vertices by component
    //      order.
    //
    // The returned array perm satisfies: perm[k] is the original vertex
    // index we place in row and column k of the reordered matrix.
    public static int[] computePermutation(DSMMatrix g) {
        int n = g.n;

        // First we compute a finish-time order of vertices on the
        // original graph using depth-first search.
        int[] finishOrder = firstPassFinishOrder(g);

        // Next we construct the transpose of the graph so that we can
        // run the second pass of Kosaraju's algorithm.
        DSMMatrix gt = transpose(g);

        // Now we discover strongly connected components on the transpose,
        // visiting vertices in decreasing order of finish time.
        int[] componentOf = secondPassComponents(gt, finishOrder);

        // With component ids in hand we can build the component-level
        // DAG and obtain a topological order of its vertices.
        List<List<Integer>> components = groupVerticesByComponent(componentOf);
        int[] compOrder = topologicalOrderOfComponentDag(g, componentOf, components.size());

        // Finally we build the permutation by walking through components
        // in topological order. Inside each component we apply a simple
        // degree-based heuristic to try to reduce FBM and TFBD without
        // adding much overhead.
        return buildPermutationFromComponents(g, components, compOrder);
    }

    private static int[] firstPassFinishOrder(DSMMatrix g) {
        int n = g.n;
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int[] indexRef = new int[]{0};

        // We rely on recursion here because it keeps the code shorter
        // for us to read. For very large graphs we might switch to an
        // explicit stack in the future.
        for (int v = 0; v < n; v++) {
            if (!visited[v]) {
                dfsFirstPass(g, v, visited, order, indexRef);
            }
        }

        // At this point order[] is filled in increasing finish time,
        // so we reverse it to obtain decreasing finish time.
        int[] reversed = new int[n];
        for (int i = 0; i < n; i++) {
            reversed[i] = order[n - 1 - i];
        }
        return reversed;
    }

    private static void dfsFirstPass(DSMMatrix g,
        int v,
        boolean[] visited,
        int[] order,
        int[] indexRef) {
        visited[v] = true;

        for (int p = g.colPtr[v]; p < g.colPtr[v + 1]; p++) {
            int u = g.rowInd[p];
            if (!visited[u]) {
                dfsFirstPass(g, u, visited, order, indexRef);
            }
        }

        int idx = indexRef[0];
        order[idx] = v;
        indexRef[0] = idx + 1;
    }

    private static DSMMatrix transpose(DSMMatrix g) {
        int n = g.n;
        int nnz = g.nnz;
        int[] row = new int[nnz];
        int[] col = new int[nnz];

        // We flip the direction of every edge by swapping its row and
        // column indices and then rebuild CSC using our builder.
        int k = 0;
        for (int j = 0; j < n; j++) {
            for (int p = g.colPtr[j]; p < g.colPtr[j + 1]; p++) {
                int i = g.rowInd[p];
                row[k] = j;
                col[k] = i;
                k += 1;
            }
        }

        return CSCBuilder.buildFromRowCol(n, nnz, row, col);
    }

    private static int[] secondPassComponents(DSMMatrix gt, int[] finishOrder) {
        int n = gt.n;
        boolean[] visited = new boolean[n];
        int[] componentOf = new int[n];
        int currentComponent = 0;

        for (int idx = 0; idx < n; idx++) {
            int v = finishOrder[idx];
            if (!visited[v]) {
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(v);
                visited[v] = true;
                componentOf[v] = currentComponent;

                while (!stack.isEmpty()) {
                    int x = stack.pop();
                    for (int p = gt.colPtr[x]; p < gt.colPtr[x + 1]; p++) {
                        int u = gt.rowInd[p];
                        if (!visited[u]) {
                            visited[u] = true;
                            componentOf[u] = currentComponent;
                            stack.push(u);
                        }
                    }
                }

                currentComponent += 1;
            }
        }

        return componentOf;
    }

    private static List<List<Integer>> groupVerticesByComponent(int[] componentOf) {
        int n = componentOf.length;
        int maxComponent = 0;
        for (int v = 0; v < n; v++) {
            if (componentOf[v] > maxComponent) {
                maxComponent = componentOf[v];
            }
        }
        int compCount = maxComponent + 1;

        List<List<Integer>> groups = new ArrayList<>(compCount);
        for (int c = 0; c < compCount; c++) {
            groups.add(new ArrayList<>());
        }

        for (int v = 0; v < n; v++) {
            int c = componentOf[v];
            groups.get(c).add(v);
        }

        return groups;
    }

    static int[] topologicalOrderOfComponentDag(DSMMatrix g,
        int[] componentOf,
        int compCount) {
        List<List<Integer>> adj = new ArrayList<>(compCount);
        for (int c = 0; c < compCount; c++) {
            adj.add(new ArrayList<>());
        }

        // Here we create edges between components whenever the original
        // graph has an edge whose endpoints live in different components.
        for (int j = 0; j < g.n; j++) {
            for (int p = g.colPtr[j]; p < g.colPtr[j + 1]; p++) {
                int i = g.rowInd[p];
                int cu = componentOf[j];
                int cv = componentOf[i];
                if (cu != cv) {
                    adj.get(cu).add(cv);
                }
            }
        }

        boolean[] visited = new boolean[compCount];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int c = 0; c < compCount; c++) {
            if (!visited[c]) {
                dfsTopo(c, adj, visited, stack);
            }
        }

        int[] order = new int[compCount];
        int idx = 0;
        while (!stack.isEmpty()) {
            order[idx] = stack.pop();
            idx += 1;
        }
        return order;
    }

    private static void dfsTopo(int c,
        List<List<Integer>> adj,
        boolean[] visited,
        Deque<Integer> stack) {
        visited[c] = true;
        for (int d : adj.get(c)) {
            if (!visited[d]) {
                dfsTopo(d, adj, visited, stack);
            }
        }
        stack.push(c);
    }

    static int[] buildPermutationFromComponents(DSMMatrix g,
                                                List<List<Integer>> components,
                                                int[] compOrder) {
        int totalVertices = 0;
        for (List<Integer> comp : components) {
            totalVertices += comp.size();
        }

        int[] perm = new int[totalVertices];
        int idx = 0;

        for (int compId : compOrder) {
            List<Integer> comp = components.get(compId);

            // Here we optionally reorder vertices inside this component
            // using a small degree-based heuristic that looks only at
            // edges within the component.
            List<Integer> ordered = orderVerticesWithinComponent(g, comp);

            for (int v : ordered) {
                perm[idx] = v;
                idx += 1;
            }
        }

        return perm;
    }

    // We use this helper to choose an order of vertices inside a single
    // strongly connected component. Our goal is to move vertices that
    // behave more like "sources" earlier and "sinks" later, based on
    // in-degree and out-degree restricted to this component.
    //
    // For very small components we simply keep the original order.
    private static List<Integer> orderVerticesWithinComponent(DSMMatrix g,
                                                              List<Integer> comp) {
        int size = comp.size();

        // For tiny components there is not much to gain, so we keep
        // the original order.
        if (size <= 2) {
            return comp;
        }

        // We build a mapping from vertex id to its index in the
        // component list so that we can quickly test membership.
        int[] indexInComp = new int[g.n];
        for (int i = 0; i < size; i++) {
            int v = comp.get(i);
            indexInComp[v] = i + 1; // store i+1 so 0 means "not in comp"
        }

        int[] inDeg = new int[size];
        int[] outDeg = new int[size];

        // We walk over all vertices in this component and count only
        // edges that stay inside the component.
        for (int i = 0; i < size; i++) {
            int v = comp.get(i);
            for (int p = g.colPtr[v]; p < g.colPtr[v + 1]; p++) {
                int u = g.rowInd[p];
                int j = indexInComp[u] - 1;
                if (j >= 0) {
                    outDeg[i] += 1;
                    inDeg[j] += 1;
                }
            }
        }

        // Now we sort a copy of the component vertices according to a
        // simple rule: lower in-degree first, and for ties higher
        // out-degree first. This tends to place "sources" before
        // "sinks" inside the component.
        List<Integer> ordered = new ArrayList<>(comp);
        ordered.sort((a, b) -> {
            int ia = indexInComp[a] - 1;
            int ib = indexInComp[b] - 1;

            int inA = inDeg[ia];
            int inB = inDeg[ib];
            if (inA != inB) {
                return Integer.compare(inA, inB);
            }

            int outA = outDeg[ia];
            int outB = outDeg[ib];
            if (outA != outB) {
                return Integer.compare(outB, outA);
            }

            // As a final tie-breaker we use the vertex id to keep the
            // ordering deterministic.
            return Integer.compare(a, b);
        });

        return ordered;
    }
}

