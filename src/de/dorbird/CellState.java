package de.dorbird;

import java.util.ArrayList;

public class CellState {
    boolean[] state = {true, true, true, true, true, true, true, true, true};
    int solvedNumber = 0;

    public boolean isNumberPossible(int number) {
        return state[number - 1];
    }

    public ArrayList<Integer> getAllPossibleNumbers() {
        ArrayList<Integer> possibleNumbers = new ArrayList<>();
        for (int digit = 1; digit <= 9; digit++) {
            if (isNumberPossible(digit))
                possibleNumbers.add(digit);
        }
        return possibleNumbers;
    }

    public void setOnlyPossibleNumber(int number) {
        for (int digit = 0; digit < 9; digit++) {
            state[digit] = digit == number - 1;
        }
        solvedNumber = number;
    }

    public void setLastTwoPossibleNumbers(int numberA, int numberB) {
        for (int digit = 0; digit < 9; digit++) {
            state[digit] = digit == (numberA - 1) ||
                    digit == (numberB - 1);
        }
    }

    public int getTheOnlyPossibleOne() {
        ArrayList<Integer> allPossibleNumbers = getAllPossibleNumbers();
        return allPossibleNumbers.size() == 1 ? allPossibleNumbers.get(0) : -1;
    }

    public void setNotPossible(int number) {
        state[number - 1] = false;
    }


    @Override
    public String toString() {
        return String.format("[%s SolvedNumber: %s 1-%s, 2-%s, 3-%s, 4-%s, 5-%s, 6-%s, 7-%s, 8-%s, 9-%s]",
                boolToEmoji(solvedNumber > 0),solvedNumber,
                boolToEmoji(state[0]), boolToEmoji(state[1]), boolToEmoji(state[2]),
                boolToEmoji(state[3]), boolToEmoji(state[4]), boolToEmoji(state[5]),
                boolToEmoji(state[6]), boolToEmoji(state[7]), boolToEmoji(state[8]));
    }

    private String boolToEmoji(boolean bool) {
        return bool ? "\u2705" : "\u274E";
    }
}
