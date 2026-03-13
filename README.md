## DSM Project for CPSC482 / CPSC682

This project implements the Design Structure Matrix (DSM) partitioning problem for the CPSC482/682 Data Structures II term project. We store sparse DSMs in compressed sparse column (CSC) format and use Kosaraju’s algorithm to find strongly connected components (SCCs) and build a task ordering.

We use Java with Maven. The main code lives under the `CPSC482Team.DSM` package, and we drive a fixed 10×10 example through the full pipeline using a JUnit test.

---

## Files and what they do

- **`App.java`**  
  Minimal entry point for the project (not used in the 10×10 test yet).

- **`DSMMatrix.java`**  
  Stores a DSM in CSC format: `n`, `nnz`, `colPtr[]`, `rowInd[]`.

- **`CSCBuilder.java`**  
  Builds a `DSMMatrix` from arrays of nonzero `(row, col)` positions.

- **`TestMatrices.java`**  
  Provides a fixed 10×10 DSM (`example10x10()`) for testing.

- **`KosarajuSCC.java`**  
  Runs Kosaraju’s algorithm on a `DSMMatrix` and returns a permutation array `perm[]` giving the new task order.

- **`DSMUtils.java`**  
  - `permute(...)`: applies a row/column permutation to a `DSMMatrix`.  
  - `computeFeedbackMetrics(...)`: computes FBM and TFBD for a permuted matrix.  
  - `toDenseString(...)`: prints a small DSM as a 0/1 matrix.

- **`AppTest.java`**  
  JUnit test that:
  - Builds the 10×10 DSM.  
  - Prints the original matrix.  
  - Runs Kosaraju + permutation and prints the runtime.  
  - Prints the permuted matrix.  
  - Checks that FBM and TFBD are non-negative.

---

## How to build and run the test

From the project root (where `pom.xml` is):

```bash
mvn test
```

This compiles the code and runs `AppTest`. During the test you will see:

- The original 10×10 DSM printed as 0/1 entries.
- The runtime for the Kosaraju + permutation step on the 10×10 matrix.
- The permuted 10×10 DSM printed as 0/1 entries.

If everything is working, Maven finishes with `BUILD SUCCESS`.

