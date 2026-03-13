## DSM Project for CPSC482 / CPSC682

This project is our implementation of the Design Structure Matrix (DSM) partitioning problem for the CPSC482/682 Data Structures II term project. We work with large, sparse DSMs, stored in compressed sparse column (CSC) format, and apply graph algorithms (Kosaraju now, Tarjan later) to find strongly connected components (SCCs) and reorder tasks to reduce feedback dependencies.

We use Java with Maven. The code is organised into a small set of classes under the `CPSC482Team.DSM` package plus a JUnit-based test that drives a fixed 10×10 example.

---

## Project structure and class overview

- **`App.java`**
  - Entry point for the project (currently minimal).
  - We will later extend this to read DSM matrices in Matrix Market format and run the full pipeline on larger inputs.

- **`DSMMatrix.java`**
  - Holds a DSM in **CSC format**:
    - `n`: matrix dimension (n×n).
    - `nnz`: number of nonzero entries.
    - `colPtr[]`: column pointer array of length `n + 1`.
    - `rowInd[]`: row indices for each nonzero.
  - This is the core data structure that all algorithms use.

- **`CSCBuilder.java`**
  - Static helper that builds a `DSMMatrix` from parallel `row[]` and `col[]` arrays of nonzero positions.
  - Implements the standard CSC build:
    1. Count entries per column.
    2. Convert counts to prefix sums for `colPtr`.
    3. Fill `rowInd` using a running pointer per column.

- **`TestMatrices.java`**
  - Contains small, fixed DSM examples used for testing and debugging.
  - Currently exposes:
    - `example10x10()`: returns a hand-crafted 10×10 `DSMMatrix` with a few cycles and chains so that SCC algorithms have something non-trivial to work on.

- **`KosarajuSCC.java`**
  - Implements **Kosaraju’s SCC algorithm** on top of `DSMMatrix` (CSC) and builds a task permutation:
    - First pass DFS on the original graph to compute finish times.
    - Graph transpose (CSC → transposed CSC).
    - Second pass DFS on the transposed graph in decreasing finish time to find SCCs.
    - Build a component-level DAG and compute a topological order of SCCs.
    - Construct a permutation array `perm[]` that lists vertices component-by-component in topological order.
  - Main API:
    - `computePermutation(DSMMatrix g)`: returns `int[] perm` where `perm[k]` is the original vertex index that should occupy row/column `k` after reordering.

- **`DSMUtils.java`**
  - Utility methods that operate on `DSMMatrix`:
    - `permute(DSMMatrix g, int[] perm)`: applies a row/column permutation to `g` and returns a new `DSMMatrix` in CSC form.
    - `computeFeedbackMetrics(DSMMatrix g)`: computes:
      - `FBM`: number of nonzeros strictly above the main diagonal.
      - `TFBD`: sum of `(col - row)` over those entries.
    - `toDenseString(DSMMatrix g)`: converts a DSM into a dense `n × n` 0/1 string representation, mainly for visual inspection of small matrices in tests.

- **`AppTest.java`**
  - JUnit test class (under `src/test/java/CPSC482Team/DSM/`).
  - Uses the 10×10 example DSM to exercise the full pipeline:
    1. Build the 10×10 `DSMMatrix`.
    2. Print the original dense matrix.
    3. Run Kosaraju to get a permutation.
    4. Permute the DSM and compute feedback metrics.
    5. Print the permuted dense matrix.
    6. Print the runtime of the Kosaraju + permutation steps on the 10×10 matrix.
  - Also asserts that:
    - The matrix dimension and permutation length are 10.
    - The feedback metrics are well-defined and non-negative.

---

## Prerequisites

- **Java**: JDK 8 or later.
- **Maven**: to build and run tests.

You can confirm versions with:

```bash
java -version
mvn -version
```

---

## Building the project

From the project root (where `pom.xml` lives), run:

```bash
mvn compile
```

This compiles the main code under `src/main/java`.

---

## Running the tests (including `AppTest`)

We use Maven’s default test lifecycle. From the project root:

```bash
mvn test
```

Maven will:

- Compile main and test code.
- Run all tests under `src/test/java`, including `CPSC482Team.DSM.AppTest`.

During `AppTest`, you should see console output similar to:

- A header `Original 10x10 DSM:` followed by 10 lines of `0` and `1`, showing the original DSM.
- A line showing the runtime for the Kosaraju + permutation step on the 10×10 matrix, in milliseconds.
- A header `Permuted 10x10 DSM:` followed by 10 more lines of `0` and `1`, showing the reordered DSM.

If the test passes, Maven will finish with `BUILD SUCCESS`.

---

## How `AppTest` works in detail

The core test method is `testExample10x10Kosaraju()` in `AppTest`:

1. **Construct the fixed 10×10 DSM**
   - Calls `TestMatrices.example10x10()` to build a small, hard-coded DSM using `CSCBuilder`.
   - Asserts that the dimension is 10.

2. **Print the original matrix**
   - Calls `DSMUtils.toDenseString(dsm)` and prints:
     - `"Original 10x10 DSM:"`
     - Followed by 10 rows of `0`/`1` entries for visual inspection.

3. **Run Kosaraju and measure runtime**
   - Records `startNanos = System.nanoTime()`.
   - Calls `KosarajuSCC.computePermutation(dsm)` to get the permutation `perm[]`.
   - Calls `DSMUtils.permute(dsm, perm)` to construct the permuted DSM.
   - Calls `DSMUtils.computeFeedbackMetrics(permuted)` to get `FBM` and `TFBD`.
   - Records `endNanos = System.nanoTime()` and prints:
     - `"Kosaraju + permutation runtime on 10x10 DSM: <milliseconds> ms"`.

4. **Print the permuted matrix**
   - Calls `DSMUtils.toDenseString(permuted)` and prints:
     - `"Permuted 10x10 DSM:"`
     - Followed by the permuted 10×10 matrix as `0`/`1`.

5. **Assertions**
   - Checks:
     - `perm.length == 10`.
     - `FBM >= 0` and `TFBD >= 0`.

This test gives us:

- A quick end-to-end check that our CSC structure, Kosaraju algorithm, permutation, and metrics all work together.
- Visual confirmation of how the permutation affects a small DSM.
- A first (very small-scale) runtime measurement.

---

## Next steps (planned)

We plan to extend the project in the following directions:

- Implement Tarjan’s SCC algorithm on the same `DSMMatrix` structure and compare it to Kosaraju.
- Implement a Matrix Market file reader to build DSMs from the benchmark test matrices provided for the term project.
- Add a more complete `main` in `App.java` that:
  - Accepts a Matrix Market file path.
  - Builds the DSM.
  - Runs both Tarjan and Kosaraju.
  - Prints feedback metrics and basic runtime statistics for each algorithm.

These steps will align the code with the full project requirements described in the assignment handout.

