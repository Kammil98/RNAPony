package cse;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args){
        /*Hairpin hairpin = new Hairpin("hairpin.dot", "cse.txt", 0);
        hairpin.findSequences();
        Loop loop = new Loop("bulge.dot", "cse.txt", 0, false);
        loop.findSequences();*/
        Loop loop = new Loop("ur15_spinka.dot", "cse.txt", 0, false);
        //loop.setSaveToFile(true);
        //loop.changeLogFile(Path.of("plik.txt"));
        loop.findSequences();
    }
}
