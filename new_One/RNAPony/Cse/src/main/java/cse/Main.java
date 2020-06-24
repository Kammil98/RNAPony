package cse;


import homology.Homology;

public class Main {
    public static void main(String[] args){
        Hairpin hairpin = new Hairpin("cse.txt", 0);
        hairpin.compute("hairpin.dot");
        Loop loop = new Loop("cse.txt", 0, false);
        loop.compute("ur4_L1.dot");
        loop.compute("ur4_L2.dot");
        //Loop loop = new Loop("ur15_spinka.dot", "cse.txt", 0, false);
        //loop.setSaveToFile(true);
        //loop.changeLogFile(Path.of("plik.txt"));
        //loop.findSequences();
        Homology homology = new Homology();
        homology.compute("ur4_L1_0.txt");
    }
}
