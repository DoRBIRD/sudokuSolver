package de.dorbird;

public class Coord {
    int row;
    int col;

    public Coord() {
        row = 0;
        col = 0;
    }

    public Coord(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getBoxRow(){
        return row/3;
    }
    public int getBoxCol(){
        return col/3;
    }

    public int getBoxNumber(){
        return getBoxCol() + (getBoxRow()*3);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Coord))
            return false;
        Coord coord = (Coord) obj;

        return this.row == coord.row && this.col == coord.col;
    }

    @Override
    public String toString() {
        return String.format("[%s/%s] %s", row + 1, col + 1, getBoxNumber());
    }
}
