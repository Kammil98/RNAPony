package models;

public class Sequence {
    private String name;
    private String seq;
    private String pdb;
    private String chain;
    private String top;
    private String bp;
    private float resol;
    private int order;

    public Sequence(){}
    public Sequence(String pdb, String chain, float resol, String seq,
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

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets seq.
     *
     * @return the seq
     */
    public String getSeq() {
        return seq;
    }

    /**
     * Sets seq.
     *
     * @param seq the seq
     */
    public void setSeq(String seq) {
        this.seq = seq;
    }

    /**
     * Gets top.
     *
     * @return the top
     */
    public String getTop() {
        return top;
    }

    /**
     * Sets top.
     *
     * @param top the top
     */
    public void setTop(String top) {
        this.top = top;
    }

    /**
     * Gets pdb.
     *
     * @return the pdb
     */
    public String getPdb() {
        return pdb;
    }

    /**
     * Sets pdb.
     *
     * @param pdb the pdb
     */
    public void setPdb(String pdb) {
        this.pdb = pdb;
    }

    /**
     * Gets chain.
     *
     * @return the chain
     */
    public String getChain() {
        return chain;
    }

    /**
     * Sets chain.
     *
     * @param chain the chain
     */
    public void setChain(String chain) {
        this.chain = chain;
    }

    /**
     * Gets bp.
     *
     * @return the bp
     */
    public String getBp() {
        return bp;
    }

    /**
     * Sets bp.
     *
     * @param bp the bp
     */
    public void setBp(String bp) {
        this.bp = bp;
    }

    /**
     * Gets resol.
     *
     * @return the resol
     */
    public float getResol() {
        return resol;
    }

    /**
     * Sets resol.
     *
     * @param resol the resol
     */
    public void setResol(float resol) {
        this.resol = resol;
    }

    /**
     * Gets order.
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
