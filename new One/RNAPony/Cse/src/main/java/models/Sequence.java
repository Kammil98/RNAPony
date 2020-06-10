package models;

import lombok.Getter;
import lombok.Setter;

public class Sequence {
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String seq;
    @Getter @Setter
    private String pdb;
    @Getter @Setter
    private String chain;
    @Getter @Setter
    private String top;
    @Getter @Setter
    private String bp;
    @Getter @Setter
    private double resol;
    @Getter @Setter
    private int order;

    public Sequence(){}
    public Sequence(String pdb, String chain, double resol, String seq,
                    String top, String bp, int order) {
        this.seq = seq;
        this.pdb = pdb;
        this.chain = chain;
        this.top = top;
        this.bp = bp;
        this.resol = resol;
        this.order = order;
    }

    /**
     * Create representation of this class in String Object
     * @return String representation of Sequence class.
     */
    public String toString(){
        String information = "";
        information += String.format("ID PDB: %s    CHAINS: %s    ORDER: %d   RESOLUTION: %f\n",
                getPdb(), getChain(), getOrder(), getResol());
        information += String.format("SEQUENCE: %s\n", seq);
        information += String.format("TOPOLOGY: %s\n", top);
        information += String.format("BPSEQ:    %s\n", bp);
        return information;
    }
}
