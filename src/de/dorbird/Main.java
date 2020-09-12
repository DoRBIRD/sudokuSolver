package de.dorbird;

public class Main {

    public static void main(String[] args) {
        String path = "sodukoHard.txt";

        if (args.length == 1)
            path = args[0];

        int[][] inputGridA = SudokuSolver.readGridFromFile(path);

        int[][] inputGridB = new int[9][9];
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                inputGridB[row][col] = inputGridA[col][row];
        SudokuSolver solver = new SudokuSolver(inputGridB);
        solver.solve();
    }
}
