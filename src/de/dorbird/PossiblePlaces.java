package de.dorbird;

import java.util.ArrayList;

public class PossiblePlaces {
    int number;
    ArrayList<Coord> possiblePlacesCoords;

    public PossiblePlaces(int number, ArrayList<Coord> possiblePlacesCoords) {
        this.number = number;
        this.possiblePlacesCoords = possiblePlacesCoords;
    }
}
