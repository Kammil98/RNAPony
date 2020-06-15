package cse;

import java.nio.file.Path;

public class MainClass {
    public static void main(String[] args){
        Hairpin hairpin = new Hairpin("hairpin.dot", "cse.txt", 0);
        hairpin.findSequences();
        Loop loop = new Loop("bulge.dot", "cse.txt", 0, false);
        loop.findSequences();

    }
}