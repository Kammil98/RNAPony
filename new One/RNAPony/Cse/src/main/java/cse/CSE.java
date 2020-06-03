package cse;

import maintokenizers.StringTokenizer;///check which one is fastest
import models.Pair;
import models.Sequence;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class CSE {
    public static final String SEPARATORS = ";";
    private final ArrayList<Sequence> sequences;
    private ArrayList<String> seqs;
    private ArrayList<String> tops;
    private ArrayList<Integer> bbps;
    private final Sequence sourceSequence;

    /**
     *  Initialize CSE attributes
     */
    public CSE(){
        sequences = new ArrayList<>();
        seqs = new ArrayList<>();
        tops = new ArrayList<>();
        bbps = new ArrayList<>();
        sourceSequence = new Sequence();
    }

    /**
     * Read necessary data from files.
     * All files should be placed in "files" folder
     * @param MPseqFile name of file with one sequence
     * @param dbFile name of file with sequences from database
     */
    public void initData(String MPseqFile, String dbFile){
        Path filesPath = Path.of("./", "files"),
                MpSeqFP = Path.of(filesPath.toString(), MPseqFile),
                dBFP = Path.of(filesPath.toString(), dbFile);

        readMpSeq(MpSeqFP.toString());
        readDataBase(dBFP.toString());

        setSeqs(createArray(sourceSequence.getSeq()));
        setTops(createArray(sourceSequence.getTop()));
        setBbps(createArrayInt(""));
    }

    /**
     * Find group of tokens and save it to ArrayList of Strings
     * @param source String with tokens divided by Separators
     * @return ArrayList of All elements from source
     */
    public ArrayList<String> createArray(String source){
        return StringTokenizer.getStreamOfTokens(source, SEPARATORS)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Find group of tokens and save it to ArrayList of Integers
     * @param source String with integer tokens divided by Separators
     * @return ArrayList of All elements from source
     */
    public ArrayList<Integer> createArrayInt(String source){
        return StringTokenizer.getStreamOfTokens(source, SEPARATORS)
                .map(Integer::valueOf).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Read necessary data about main sequence from files.
     * @param fileName name of file with one sequence
     */
    public void readMpSeq(String fileName){
        try(BufferedReader reader =
                    new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(fileName)
                    ))){
            String line;
            for(int lineNo = 1; (line = reader.readLine()) != null; lineNo++){
                switch(lineNo){
                    case 1:
                        getSourceSequence().setName(line.substring(1));
                        break;
                    case 2:
                        getSourceSequence().setSeq(line);
                        break;
                    case 3:
                        getSourceSequence().setTop(line);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read necessary data about sequences from files.
     * @param fileName name of file with database of sequences
     */
    public void readDataBase(String fileName){
        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(fileName)
                            ))){
            String line;
            String pdb, chain, seq, top, bp;
            double resol;
            int order;
            for(StringTokenizer tokenizer = null;
                (line = reader.readLine()) != null;){

                tokenizer = new StringTokenizer(line, " ");
                if(tokenizer.countTokens() == 7){
                    pdb = tokenizer.nextToken();
                    chain = tokenizer.nextToken();
                    resol = Double.parseDouble(tokenizer.nextToken());
                    seq = tokenizer.nextToken();
                    top = tokenizer.nextToken();
                    bp = tokenizer.nextToken();
                    order = Integer.parseInt(tokenizer.nextToken());
                    getSequences().add(new Sequence(pdb, chain, resol, seq, top, bp, order));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Display sequences from database
     */
    public void showDataBase(){
        Iterator<Sequence> seqsIter = getSequences().iterator();
        int counter = 0;
        while (seqsIter.hasNext()){
            counter++;
            System.out.printf("%5d\n", counter);
            System.out.println(seqsIter.next());
        }
    }

    /**
     * Check if tested string of nucleotides cover another
     * string of nucleotides from list
     * @param pairs list of begins and ends of strings
     * @param testedPair begin and end of tested string of nucleotides
     * @return true, if is not covering any of strings in given list
     */
    public boolean isOk(ArrayList<Pair> pairs, Pair testedPair){
        Predicate<Pair> isFirstBetween = pair -> pair.isPointBetweenInclusive(testedPair.getFirst());
        Predicate<Pair> isSecondBetween = pair -> pair.isPointBetweenInclusive(testedPair.getSecond());
        Predicate<Pair> isInside = pair -> pair.isInsideGivenPair(testedPair);
        Predicate<Pair> isNotOk = isFirstBetween.or(isSecondBetween).or(isInside);
        return pairs.stream().noneMatch(isNotOk);
    }

    public void createPatternShift(){

    }

    public static void main(String args[]) {

    }


    public ArrayList<Sequence> getSequences() {
        return sequences;
    }

    public Sequence getSourceSequence() {
        return sourceSequence;
    }

    public ArrayList<String> getSeqs() {
        return seqs;
    }

    public void setSeqs(ArrayList<String> seqs) {
        this.seqs = seqs;
    }

    public ArrayList<String> getTops() {
        return tops;
    }

    public void setTops(ArrayList<String> tops) {
        this.tops = tops;
    }

    public ArrayList<Integer> getBbps() {
        return bbps;
    }

    public void setBbps(ArrayList<Integer> bbps) {
        this.bbps = bbps;
    }
}
