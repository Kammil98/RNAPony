package models;

//import lombok.Getter;
//import lombok.Setter;

public class Sequence {
    //@Getter @Setter
    private String pdb;
    //@Getter @Setter
    private String chain;
    //@Getter @Setter
    private double resol;
    //@Getter @Setter
    private String seq;
    //@Getter @Setter
    private String top;
    //@Getter @Setter
    private String bp;
    //@Getter @Setter
    private int order;
    //@Getter @Setter
    private String name;


    public Sequence(){}
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

    public String getPdb() {
        return pdb;
    }

    public void setPdb(String pdb) {
        this.pdb = pdb;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public double getResol() {
        return resol;
    }

    public void setResol(double resol) {
        this.resol = resol;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
