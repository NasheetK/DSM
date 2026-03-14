## DSM Project for CPSC482 / CPSC682

This project implements the Design Structure Matrix (DSM) partitioning problem for the CPSC482/682 Data Structures II term project. We store sparse DSMs in compressed sparse column (CSC) format and use Kosaraju’s and Tarjan’s algorithms to find strongly connected components (SCCs) and build a task ordering.

We use Java with Maven. The main code lives under the `CPSC482Team.DSM` package. We can either run a fixed 10×10 example through a JUnit test or run the same pipeline on any Matrix Market file from the `TestData` folder via the main application.

---

## Files and what they do

- **`App.java`**  
  Command-line entry point. Reads a Matrix Market file, builds the DSM, reports original FBM/TFBD, then runs Kosaraju and Tarjan + permutation and prints runtimes and FBM/TFBD for each.

- **`DSMMatrix.java`**  
  Stores a DSM in CSC format: `n`, `nnz`, `colPtr[]`, `rowInd[]`.

- **`CSCBuilder.java`**  
  Builds a `DSMMatrix` from arrays of nonzero `(row, col)` positions.

- **`TestMatrices.java`**  
  Provides a fixed 10×10 DSM (`example10x10()`) for testing.

- **`KosarajuSCC.java`**  
  Runs Kosaraju’s algorithm on a `DSMMatrix` and returns a permutation array `perm[]` giving the new task order (with a simple intra-SCC heuristic).

- **`TarjanSCC.java`**  
  Runs Tarjan’s algorithm on a `DSMMatrix` and returns a permutation array `perm[]` using the same component DAG and intra-SCC heuristic as Kosaraju.

- **`DSMUtils.java`**  
  - `permute(...)`: applies a row/column permutation to a `DSMMatrix`.  
  - `computeFeedbackMetrics(...)`: computes FBM and TFBD for a permuted matrix.  
  - `toDenseString(...)`: prints a small DSM as a 0/1 matrix.

- **`MatrixMarketReader.java`**  
  Reads a square DSM from a Matrix Market coordinate file (e.g. files in the `TestData` folder) and returns a `DSMMatrix`.

- **`AppTest.java`**  
  JUnit test that:
  - Builds the 10×10 DSM.  
  - Prints the original matrix.  
  - Computes and prints original FBM/TFBD.  
  - Runs Kosaraju + permutation and prints the runtime and FBM/TFBD.  
  - Runs Tarjan + permutation and prints the runtime and FBM/TFBD.  
  - Prints the permuted matrices and checks that all FBM and TFBD values are non-negative.

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

---

## How to run `App` on a TestData file

From the project root, after compiling:

```bash
mvn compile
```

Run the main application on a Matrix Market file (for example `TestData/EVA.mtx`):

```bash
cd target/classes
java CPSC482Team.DSM.App ..\..\TestData\EVA.mtx
```

`App` will:

- Read the DSM from the given file.
- Print the matrix size and number of nonzeros.
- Compute and print FBM and TFBD for the original ordering.
- Run Kosaraju + permutation and print the runtime and FBM/TFBD.
- Run Tarjan + permutation and print the runtime and FBM/TFBD.

