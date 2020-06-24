package csemodels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
