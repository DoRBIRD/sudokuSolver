package com.company;

public class Main {

    public static void main(String[] args) {
        String path = "sodukoHard.txt";
        int[][] inputGridA = SodukoSolver.readGridFromFile(path);

        int[][] inputGridB = new int[9][9];
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                inputGridB[row][col] = inputGridA[col][row];
        SodukoSolver solver = new SodukoSolver(inputGridB);
        solver.solve();
    }
}
