package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import updater.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DBrecord {

    public static final String CHARS_BP1 = "([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String CHARS_BP2 = ")]}>abcdefghijklmnopqrstuvwxyz";

    String id;
    int modelNo;
    String chain;
    double resol;
    String seq;
    String dot;
    String dotIntervals;
    int maxOrder;

    /**
     * Calculate maximum order of structure, based
     * on given 2D structure.
     * @param dot .dot file with 2D representation of structure.
     * @return maximum order.
     */
    private int getMaxOrderFrom2D(String dot) {
        int charIdx;
        int maxOrder = -1;
        int dotSize = dot.length();
        for (int charNo = 0; charNo < dotSize; charNo++) {
            charIdx = CHARS_BP1.indexOf(dot.charAt(charNo));
            if (charIdx > maxOrder)
                maxOrder = charIdx;
        }
        return maxOrder;
    }

    private int getIntervalIdx(final StringBuilder intervals, int charNo){
        int index = 0;
        while (charNo >= 0){
            index = intervals.indexOf(";", index + 1);
            charNo--;
        }
        return index;
    }

    /**
     * Calculate distance between every connected pair of residues,
     * based on given 2D structure.
     * @param dot .dot file with 2D representation of structure.
     * @return String, which represent semicolon separated
     * distances between every connected pair of residues.
     */
    private String getIntervalsFrom2D(final String dot){
        StringBuilder intervals = new StringBuilder(dot.length() * 3);
        ArrayList<Deque<Integer>> stacks = new ArrayList<>();
        int order, openingBracketIdx, interval, index;
        char currChar;
        for(int i = 0; i <= getMaxOrder(); i ++)
            stacks.add(new ArrayDeque<>());
        //"x" sign should disapper till end of this function
        //just make place in stringbuilder
        intervals.append("x;".repeat(dot.length()));
        for (int charNo = 0; charNo < dot.length(); charNo++) {
            currChar = dot.charAt(charNo);
            if(currChar == '.' || currChar == '-'){
                index = getIntervalIdx(intervals, charNo);
                intervals.replace(index - 1, index,"0");
            }
            else {
                order = CHARS_BP1.indexOf(currChar);
                if(order != -1){
                    stacks.get(order).add(charNo);
                }
                else {//It's closing bracket
                    order = CHARS_BP2.indexOf(currChar);
                    openingBracketIdx = stacks.get(order).pollLast();
                    interval = charNo - openingBracketIdx;
                    index = getIntervalIdx(intervals, openingBracketIdx);
                    intervals.replace(index - 1, index, String.valueOf(interval));//for opening bracket;
                    index = getIntervalIdx(intervals, charNo);
                    intervals.replace(index - 1, index, String.valueOf(-interval));//for closing bracket
                }
            }
        }
        return intervals.deleteCharAt(intervals.length() - 1).toString();
    }

    /**
     * Check resolution in resolu.idx file of structure,
     * which id is given as parameter.
     * @param id idCode of structure.
     * @return resolution of structure.
     */
    private double checkResol(String id){
        StringTokenizer tokenizer;
        String line, resol;
        id = id.substring(0, 4).toUpperCase();
        try(Scanner dotReader = new Scanner(
                new File(
                        Main.frabaseDir.resolve("resolu.idx").toString()))){
            while (!dotReader.nextLine().stripLeading().startsWith("IDCODE")) {
            }
            dotReader.nextLine();
            while (dotReader.hasNextLine()){
                line = dotReader.nextLine();
                if(line.startsWith(id)){
                    tokenizer = new StringTokenizer(line, ";\t");
                    tokenizer.nextToken();
                    resol = tokenizer.nextToken();
                    return resol.equals("-1.00") ? 999.99d : Double.parseDouble(resol);
                }
            }
        } catch (FileNotFoundException e) {
            Main.errLogger.severe("Couldn't find file: resolu.idx");
            System.exit(-1);
        }
        return 999.99d;
    }

    /**
     * Compute values of record based on 2D representation
     * of structure and filename (structure of preprocessed
     * filename is following: id_modelNo.cif).
     * @param dotFile Object, which represent 2D structure.
     * @param filename name of preprocessed file according to pattern:
     *                 id_modelNo.cif, where id means id from Protein Data Bank.
     */
    public void computeRecord(final DotFile dotFile, final String filename){
        String id = filename.substring(0, 4);
        setId(id);
        setModelNo(Integer.parseInt(
                filename.substring(5, filename.lastIndexOf('.'))
        ));
        setChain(dotFile.getName());
        setResol(checkResol(id));
        setSeq(dotFile.getSeq());
        setDot(dotFile.getDot());
        setMaxOrder(getMaxOrderFrom2D(dotFile.getDot()));
        setDotIntervals(getIntervalsFrom2D(dotFile.getDot()));
    }

    @Override
    public String toString() {
        return id + " " + modelNo + " " + chain + " " + resol + " " + seq + " " + dot + " " + dotIntervals + " " + maxOrder;
    }

    /**
     * Create DBrecord based on given String
     * @param line String with given record. Format need to be same as in toString method.
     * @return record in DBrecord Object.
     */
    public static DBrecord valueOf(String line){
        DBrecord record;
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        record = new DBrecord(tokenizer.nextToken(),
                Integer.parseInt(tokenizer.nextToken()),
                tokenizer.nextToken(),
                Double.parseDouble(tokenizer.nextToken()),
                tokenizer.nextToken(),
                tokenizer.nextToken(),
                tokenizer.nextToken(),
                Integer.parseInt(tokenizer.nextToken()));
        return record;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBrecord dBrecord = (DBrecord) o;
        return id.equals(dBrecord.id) &&
                modelNo == dBrecord.modelNo &&
                Double.compare(dBrecord.resol, resol) == 0 &&
                chain.equals(dBrecord.chain) &&
                seq.equals(dBrecord.seq) &&
                dot.equals(dBrecord.dot) &&
                dotIntervals.equals(dBrecord.dotIntervals) &&
                maxOrder == dBrecord.maxOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, modelNo, chain, resol, seq, dot, dotIntervals, maxOrder);
    }
}
