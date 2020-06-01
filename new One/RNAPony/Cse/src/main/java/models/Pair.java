package models;

public class Pair {
    private int first;
    private int second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public Pair(){}

    public boolean isPointBetweenInclusive(int x){return first <= x & x <= second;}

    public boolean isInsideGivenPair(Pair p2){return p2.getFirst() < first & second < p2.getSecond();}

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
}
