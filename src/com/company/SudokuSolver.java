package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SudokuSolver {
    CellState[][] cellStates = new CellState[9][9];

    public SudokuSolver(int[][] inputGrid) {
        resetGridStateToInput(inputGrid);
    }

    //region SolverLogic

    public void solve() {
        boolean isSolved;
        int iterations = 0;
        int amountOfChangesThisIteration;
        markDownAllSafeGuesses();
        printGrid();
        do {
            iterations++;
            System.out.println("#" + iterations);
            amountOfChangesThisIteration = markDownAllSafeGuesses();
            if (amountOfChangesThisIteration == 0)
                amountOfChangesThisIteration += byRowColAndBox();
            if (amountOfChangesThisIteration == 0)
                amountOfChangesThisIteration += lastOptionsShareHouse();
            if (amountOfChangesThisIteration == 0)
                amountOfChangesThisIteration += checkOverlapForLastTwo();
            isSolved = isSolved();
            System.out.printf("Changes in this iteration: %s%n", amountOfChangesThisIteration);
        } while (!isSolved && iterations < 100 && amountOfChangesThisIteration > 0);

        printGrid();

        if (isSolved) {
            System.out.printf("Done in %s iterations%n", iterations);
        } else {
            int missingFields = 0;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    missingFields += cellStates[row][col].solvedNumber > 0 ? 1 : 0;
                }
            }
            System.out.printf("Not solved after %s iterations, with %s Changes in last Iteration, missing %s/81 fields%n", iterations, amountOfChangesThisIteration, missingFields);
        }
    }

    private int markDownAllSafeGuesses() {
        int amount = 0;
        Coord coord = new Coord();
        for (coord.row = 0; coord.row < 9; coord.row++)
            for (coord.col = 0; coord.col < 9; coord.col++)
                if (getStateFromCoords(coord).solvedNumber == 0) {
                    int solvedNumber = getStateFromCoords(coord).getTheOnlyPossibleOne();
                    if (solvedNumber > 0) {
                        getStateFromCoords(coord).setOnlyPossibleNumber(solvedNumber);
                        removeImpossibleNumbersFromNeighbours(coord, solvedNumber);
                        amount++;
                        System.out.println("(S) Found new solved number: " + solvedNumber + " at: " + coord);
                    }
                }
        return amount;
    }

    //mark only possibles in row / col / box
    private int byRowColAndBox() {
        int changes = 0;
        for (int number = 1; number <= 9; number++) {
            //rows
            for (int row = 0; row < 9; row++) {
                if (isRowMissingNumber(row, number)) {
                    ArrayList<Coord> possibles = getAllPossiblesForNumberInRow(row, number);
                    if (possibles.size() == 1) {
                        getStateFromCoords(possibles.get(0)).setOnlyPossibleNumber(number);
                        removeImpossibleNumbersFromNeighbours(possibles.get(0), number);
                        changes++;
                        System.out.println("(R) Found new solved number: " + number + " at: " + possibles.get(0));
                    }
                }
            }
            //cols
            for (int col = 0; col < 9; col++) {
                if (isColMissingNumber(col, number)) {
                    ArrayList<Coord> possibles = getAllPossiblesForNumberInCol(col, number);
                    if (possibles.size() == 1) {
                        getStateFromCoords(possibles.get(0)).setOnlyPossibleNumber(number);
                        removeImpossibleNumbersFromNeighbours(possibles.get(0), number);
                        changes++;
                        System.out.println("(C) Found new solved number: " + number + " at: " + possibles.get(0));
                    }
                }
            }
            //boxes
            for (int boxRow = 0; boxRow < 3; boxRow++) {
                for (int boxCol = 0; boxCol < 3; boxCol++) {
                    if (isBoxMissingNumber(boxRow, boxRow, number)) {
                        ArrayList<Coord> possibles = getAllPossiblesForNumberInBox(boxRow, boxCol, number);
                        if (possibles.size() == 1) {
                            getStateFromCoords(possibles.get(0)).setOnlyPossibleNumber(number);
                            removeImpossibleNumbersFromNeighbours(possibles.get(0), number);
                            changes++;
                            System.out.println("(B) Found new solved number: " + number + " at: " + possibles.get(0));
                        }
                    }
                }
            }
        }
        return changes;
    }

    private ArrayList<PossiblePlaces> findOverlaps(ArrayList<PossiblePlaces> list) {
        ArrayList<PossiblePlaces> filteredList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (i != j) {
                    if (list.get(i).possiblePlacesCoords.get(0).equals(list.get(j).possiblePlacesCoords.get(0))
                            && list.get(i).possiblePlacesCoords.get(1).equals(list.get(j).possiblePlacesCoords.get(1))) {
                        filteredList.add(list.get(i));
                    }
                }
            }
        }
        return filteredList;
    }

    private boolean doesShareRow(ArrayList<Coord> coords) {
        int firstRowNumber = coords.get(0).row;
        for (int i = 1; i < coords.size(); i++) {
            if(firstRowNumber != coords.get(i).row)
                return false;
        }
        return true;
    }

    private boolean doesShareCol(ArrayList<Coord> coords) {
        int firstColNumber = coords.get(0).col;
        for (int i = 1; i < coords.size(); i++) {
            if (firstColNumber != coords.get(i).col)
                return false;
        }
        return true;
    }

    private boolean allShareBox(ArrayList<Coord> coords) {
        int firstBoxNumber = coords.get(0).getBoxNumber();
        for (int i = 1; i < coords.size(); i++) {
            if (firstBoxNumber != coords.get(i).getBoxNumber())
                return false;
        }
        return true;
    }

    private int lastOptionsShareHouse() {
        int changes = 0;
        for (int number = 1; number <= 9; number++) {
            //rows
            for (int row = 0; row < 9; row++) {
                if (isRowMissingNumber(row, number)) {
                    ArrayList<Coord> possibles = getAllPossiblesForNumberInRow(row, number);
                    if (possibles.size() == 2 || possibles.size() == 3) {
                        if (allShareBox(possibles)) {
                            ArrayList<Coord> inSameBox = getAllPossiblesForNumberInBox(possibles.get(0).getBoxRow(), possibles.get(0).getBoxCol(), number);
                            if (inSameBox.size() > possibles.size())
                                for (Coord coord : inSameBox) {
                                    if (coord.row != row) {
                                        getStateFromCoords(coord).setNotPossible(number);
                                        changes ++;
                                    }
                                }
                            System.out.println("(H) Found new shared houses for number: " + number + " in row: " + row);
                        }
                    }
                }
            }

            //cols
            for (int col = 0; col < 9; col++) {
                if (isColMissingNumber(col, number)) {
                    ArrayList<Coord> possibles = getAllPossiblesForNumberInCol(col, number);
                    if (possibles.size() == 2 || possibles.size() == 3) {
                        if (allShareBox(possibles)) {
                            ArrayList<Coord> inSameBox = getAllPossiblesForNumberInBox(possibles.get(0).getBoxRow(), possibles.get(0).getBoxCol(), number);
                            if (inSameBox.size() > possibles.size())
                                for (Coord coord : inSameBox) {
                                    if (coord.col != col) {
                                        getStateFromCoords(coord).setNotPossible(number);
                                        changes ++;
                                    }
                                }
                            System.out.println("(H) Found new shared houses for number: " + number + " in col: " + col);
                        }
                    }
                }
            }
        }
        return changes;
    }

    //region Hidden Pairs
    //check if 2 numbers have same last 2 spots in common
    private int checkOverlapForLastTwo() {
        int changes = 0;

        for (int rows = 0; rows < 9; rows++) {
            ArrayList<PossiblePlaces> list = new ArrayList<>();
            for (int number = 1; number <= 9; number++) {
                if (isRowMissingNumber(rows, number)) {
                    PossiblePlaces places = new PossiblePlaces(number, getAllPossiblesForNumberInRow(rows, number));
                    if (places.possiblePlacesCoords.size() == 2) {
                        list.add(places);
                        System.out.printf("(O) Found 2 of %s Left in Row %s %n", number, rows);
                    }
                }
            }
            ArrayList<PossiblePlaces> filteredList = findOverlaps(list);
            //Remove other Numbers for now only if found EXACTLY one pair
            changes += cleanUpOverlapPairs(filteredList);
        }

        //Cols
        for (int cols = 0; cols < 9; cols++) {
            ArrayList<PossiblePlaces> list = new ArrayList<>();
            for (int number = 1; number <= 9; number++) {
                if (isColMissingNumber(cols, number)) {
                    PossiblePlaces places = new PossiblePlaces(number, getAllPossiblesForNumberInCol(cols, number));
                    if (places.possiblePlacesCoords.size() == 2) {
                        list.add(places);
                    }
                }
            }
            ArrayList<PossiblePlaces> filteredList = findOverlaps(list);
            //Remove other Numbers for now only if found EXACTLY one pair
            changes += cleanUpOverlapPairs(filteredList);
        }

        //Box
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                ArrayList<PossiblePlaces> list = new ArrayList<>();
                for (int number = 1; number <= 9; number++) {
                    if (isBoxMissingNumber(boxRow, boxCol, number)) {
                        PossiblePlaces places = new PossiblePlaces(number, getAllPossiblesForNumberInBox(boxRow, boxCol, number));
                        if (places.possiblePlacesCoords.size() == 2) {
                            list.add(places);
                        }
                    }
                }
                ArrayList<PossiblePlaces> filteredList = findOverlaps(list);
                //Remove other Numbers for now only if found EXACTLY one pair
                changes += cleanUpOverlapPairs(filteredList);
            }
        }
        return changes;
    }

    private int cleanUpOverlapPairs(ArrayList<PossiblePlaces> list) {
        int changes = 0;
        if (list.size() == 2) {
            Coord coordA = list.get(0).possiblePlacesCoords.get(0);
            Coord coordB = list.get(0).possiblePlacesCoords.get(1);
            if (getStateFromCoords(coordA).getAllPossibleNumbers().size() > 2 || getStateFromCoords(coordB).getAllPossibleNumbers().size() > 2){
                int numberA = list.get(0).number;
                int numberB = list.get(1).number;
                getStateFromCoords(coordA).setLastTwoPossibleNumbers(numberA, numberB);
                getStateFromCoords(coordB).setLastTwoPossibleNumbers(numberA, numberB);
                changes += 2;
                System.out.printf("(O) Left only %s,%s in the coords %s/%s, %s/%s%n", numberA, numberB, coordA.row, coordA.col, coordB.row, coordB.col);
            }
        }
        return changes;
    }
    //endregion

    //endregion

    //region GridUtils

    public static int[][] readGridFromFile(String path) {
        int[][] grid = new int[9][9];
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    if (myReader.hasNext())
                        grid[row][col] = myReader.nextInt();
                    else throw new IllegalArgumentException("Grid is not formatted correctly");
                }
            }
            myReader.close();
        } catch (FileNotFoundException | IllegalArgumentException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return grid;
    }

    private CellState getStateFromCoords(Coord coord) {
        return cellStates[coord.row][coord.col];
    }

    public void resetGridState() {
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                cellStates[row][col] = new CellState();
    }

    public void resetGridStateToInput(int[][] inputGrid) {
        resetGridState();
        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++) {
                int inputDigit = inputGrid[col][row];
                if (inputDigit != 0) {
                    cellStates[row][col].setOnlyPossibleNumber(inputDigit);
                    cellStates[row][col].solvedNumber = 0;
                }
            }
    }

    private boolean isSolved() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cellStates[row][col].solvedNumber <= 0)
                    return false;
            }
        }
        return true;
    }

    private void printGrid() {
        Coord currentCoord = new Coord();
        System.out.println("Printing Grid");
        for (currentCoord.row = 0; currentCoord.row < 9; currentCoord.row++) {
            if (currentCoord.row % 3 == 0)
                System.out.println("-----------------------");
            System.out.print("|");
            for (currentCoord.col = 0; currentCoord.col < 9; currentCoord.col++) {
                if (currentCoord.col % 3 == 0)
                    System.out.print("|");
                System.out.print(getStateFromCoords(currentCoord).solvedNumber + "|");
            }
            System.out.println("|");
        }
    }

    //remove impossibles
    private void removeImpossibleNumbersFromNeighbours(int row, int col, int number) {
        //rows
        ArrayList<Coord> otherInRow = getOtherCellsInSameRow(row, col);
        removeImpossibleNumberFromCoordsList(otherInRow, number);
        //cols
        ArrayList<Coord> otherInCol = getOtherCellsInSameCol(row, col);
        removeImpossibleNumberFromCoordsList(otherInCol, number);
        //boxes
        ArrayList<Coord> otherInBox = getOtherCellsInSameBox(row, col);
        removeImpossibleNumberFromCoordsList(otherInBox, number);
    }

    private void removeImpossibleNumbersFromNeighbours(Coord coord, int number) {
        removeImpossibleNumbersFromNeighbours(coord.row, coord.col, number);
    }

    private void removeImpossibleNumberFromCoordsList(ArrayList<Coord> coords, int number) {
        for (Coord coord : coords)
            getStateFromCoords(coord).setNotPossible(number);
    }

    private ArrayList<Coord> getOtherCellsInSameRow(int row, int col) {
        ArrayList<Coord> cellsInSameRow = new ArrayList<>();
        for (int otherCol = 0; otherCol < 9; otherCol++)
            if (!(col == otherCol))
                cellsInSameRow.add(new Coord(row, otherCol));
        return cellsInSameRow;
    }

    private ArrayList<Coord> getOtherCellsInSameCol(int row, int col) {
        ArrayList<Coord> cellsInSameRow = new ArrayList<>();
        for (int otherRow = 0; otherRow < 9; otherRow++)
            if (!(row == otherRow))
                cellsInSameRow.add((new Coord(otherRow, col)));
        return cellsInSameRow;
    }

    private ArrayList<Coord> getOtherCellsInSameBox(int row, int col) {
        ArrayList<Coord> cellsInSameRow = new ArrayList<>();
        int rowStart = (row / 3) * 3;
        int colStart = (col / 3) * 3;
        for (int otherRow = rowStart; otherRow < rowStart + 3; otherRow++)
            for (int otherCol = colStart; otherCol < colStart + 3; otherCol++)
                if (!(row == otherRow && col == otherCol))
                    cellsInSameRow.add(new Coord(otherRow, otherCol));
        return cellsInSameRow;
    }

    private boolean isRowMissingNumber(int row, int number) {
        for (int col = 0; col < 9; col++)
            if (cellStates[row][col].solvedNumber == number)
                return false;
        return true;
    }

    private boolean isColMissingNumber(int col, int number) {
        for (int row = 0; row < 9; row++)
            if (cellStates[row][col].solvedNumber == number)
                return false;
        return true;
    }

    private boolean isBoxMissingNumber(int boxRow, int boxCol, int number) {
        boxRow *= 3;
        boxCol *= 3;
        for (int row = boxRow * 3; row < boxRow + 3; row++)
            for (int col = boxCol * 3; col < boxCol + 3; col++)
                if (cellStates[row][col].solvedNumber == number)
                    return false;
        return true;
    }

    private ArrayList<Coord> getAllPossiblesForNumberInRow(int row, int number) {
        ArrayList<Coord> possiblesInRow = new ArrayList<>();
        for (int col = 0; col < 9; col++)
            if (cellStates[row][col].solvedNumber <= 0 && cellStates[row][col].isNumberPossible(number))
                possiblesInRow.add(new Coord(row, col));
        return possiblesInRow;
    }

    private ArrayList<Coord> getAllPossiblesForNumberInCol(int col, int number) {
        ArrayList<Coord> possiblesInCol = new ArrayList<>();
        for (int row = 0; row < 9; row++)
            if (cellStates[row][col].solvedNumber <= 0 && cellStates[row][col].isNumberPossible(number))
                possiblesInCol.add(new Coord(row, col));
        return possiblesInCol;
    }

    private ArrayList<Coord> getAllPossiblesForNumberInBox(int boxRow, int boxCol, int number) {
        ArrayList<Coord> possiblesInBox = new ArrayList<>();
        boxRow *= 3;
        boxCol *= 3;
        for (int row = boxRow; row < boxRow + 3; row++)
            for (int col = boxCol; col < boxCol + 3; col++)
                if (cellStates[row][col].solvedNumber <= 0 && cellStates[row][col].isNumberPossible(number))
                    possiblesInBox.add(new Coord(row, col));
        return possiblesInBox;
    }
    //endregion
}
