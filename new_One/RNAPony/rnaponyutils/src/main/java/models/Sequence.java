package models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Sequence {
    private String pdb;
    private String chain;
    private double resol;
    private String seq;
    private String top;
    private String bp;
    private int order;
    private String name;

    public Sequence(String pdb, String chain, double resol, String seq,
                    String top, String bp, int order) {
        this.setSeq(seq);
        this.setPdb(pdb);
        this.setChain(chain);
        this.setTop(top);
        this.setBp(bp);
        this.setResol(resol);
        this.setOrder(order);
    }

    /**
     * Create representation of this class in String Object
     * @return String representation of Sequence class.
     */
    public String toString(){
        String information = "";
        information += String.format("ID PDB: %s    CHAINS: %s    ORDER: %d   RESOLUTION: %f\n",
                getPdb(), getChain(), getOrder(), getResol());
        information += String.format("SEQUENCE: %s\n", getSeq());
        information += String.format("TOPOLOGY: %s\n", getTop());
        information += String.format("BPSEQ:    %s\n", getBp());
        return information;
    }
}
