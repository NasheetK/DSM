package CPSC482Team.DSM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// In this helper class we read sparse matrices stored in Matrix Market
// coordinate format and turn them into DSMMatrix instances backed by
// our CSC representation.
public final class MatrixMarketReader {

    private MatrixMarketReader() {
        // We only provide static helper methods here.
    }

    // We read a square matrix from a Matrix Market file. The file is
    // expected to use the "coordinate" format where each nonzero entry
    // is given by a triplet (row, col, value). We ignore the value and
    // only use the row and column indices.
    //
    // We also skip any header or comment lines that start with '%'.
    public static DSMMatrix read(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // First we skip all comment/header lines that start with '%'.
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.charAt(0) == '%') {
                    continue;
                }
                // As soon as we hit a non-comment line we expect the
                // dimensions and nnz counts to be present.
                break;
            }

            if (line == null) {
                throw new IOException("Matrix Market file appears to be empty: " + filePath);
            }

            String[] headerParts = line.trim().split("\\s+");
            if (headerParts.length < 3) {
                throw new IOException("Could not parse matrix dimensions from: " + line);
            }

            int m = Integer.parseInt(headerParts[0]);
            int n = Integer.parseInt(headerParts[1]);
            int nnz = Integer.parseInt(headerParts[2]);

            if (m != n) {
                throw new IOException("DSM matrix is not square: " + m + "x" + n);
            }

            // We collect row and column indices in temporary lists first
            // and convert them to arrays at the end.
            List<Integer> rows = new ArrayList<>(nnz);
            List<Integer> cols = new ArrayList<>(nnz);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.charAt(0) == '%') {
                    // We ignore any mid-file comments as well.
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                int rowIndex = Integer.parseInt(parts[0]) - 1; // convert 1-based to 0-based
                int colIndex = Integer.parseInt(parts[1]) - 1; // convert 1-based to 0-based

                rows.add(rowIndex);
                cols.add(colIndex);
            }

            if (rows.size() != nnz || cols.size() != nnz) {
                // We trust the file but we still want to know if there
                // is a mismatch between the declared and actual nnz.
                System.out.println("Warning: expected nnz = " + nnz
                        + " but read " + rows.size() + " entries from " + filePath);
                nnz = rows.size();
            }

            int[] row = new int[nnz];
            int[] col = new int[nnz];
            for (int k = 0; k < nnz; k++) {
                row[k] = rows.get(k);
                col[k] = cols.get(k);
            }

            return CSCBuilder.buildFromRowCol(n, nnz, row, col);
        }
    }
}

