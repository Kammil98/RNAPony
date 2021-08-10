package cse;

import lombok.Getter;
import lombok.Setter;
import maintokenizers.StringTokenizer;
import csemodels.Pair;
import models.Sequence;
import utils.Computable;
import utils.Utils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.logging.*;

public abstract class CSE implements Computable {
    @Setter @Getter
    private int insertion;
    public static final Logger logger;
    public static final Logger errLog;
    public static final String CHARS_BP1 = "([{<ABCDEFGHIJK";
    public static final String CHARS_BP2 = ")]}>abcdefghijk";
    public static final String BASE = "acgu";
    public static final String SEPARATORS = ";";
    @Getter
    private final ArrayList<Sequence> sequences;
    @Setter @Getter
    private ArrayList<String> seqs;
    @Setter @Getter
    private ArrayList<String> tops;
    @Setter @Getter
    private ArrayList<Integer> bbps;
    @Getter
    private Sequence sourceSequence;

    static {
        logger = Logger.getLogger(cse.CSE.class.getName());
        errLog = Logger.getLogger("errLog_" + cse.CSE.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        logger.setUseParentHandlers(false);
        errLog.setUseParentHandlers(false);
        Utils.changeLogHandler(logger);
        Utils.changeLogHandler(errLog, Path.of("errApp.txt"));
    }

    /**
     * Initialize CSE and read database.
     * @param dBFilePath path to file with database
     * @param insertion number of insertions
     */
    public CSE(String dBFilePath, int insertion){
        this.setInsertion(insertion);
        sequences = new ArrayList<>();
        seqs = new ArrayList<>();
        tops = new ArrayList<>();
        bbps = new ArrayList<>();
        sourceSequence = new Sequence();
        readDataBase(dBFilePath);
    }

    /**
     * Main function, which calculate
     * and display sequences.
     * @param MPseqFileName name of file with base Sequence
     */
    public abstract void compute(String MPseqFileName);

    /**
     * Read necessary data from files.
     * All files should be placed in "files" folder.
     * @param MPseqFilePath path to file with one sequence
     */
    public void initData(String MPseqFilePath){
        sourceSequence = new Sequence();
        readMpSeq(MPseqFilePath);
        setSeqs(Utils.createArray(sourceSequence.getSeq(), SEPARATORS));
        setTops(Utils.createArray(sourceSequence.getTop(), SEPARATORS));
        setBbps(Utils.createArrayInt("", SEPARATORS));
    }

    /**
     * Read necessary data about main sequence from files.
     * @param filePath path to file with one sequence
     */
    public void readMpSeq(String filePath){
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
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
            logger.log(Level.SEVERE, "Problem with opening file " + filePath + ". More information at errApp.txt");
            errLog.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Read necessary data about sequences from database.
     * @param filePath path to file with database of sequences
     */
    public void readDataBase(String filePath){
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;
            String pdb, chain, seq, top, bp;
            double resol;
            int order;
            for(StringTokenizer tokenizer;
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
            logger.log(Level.SEVERE, "Problem with opening file " + filePath + ". More information at errApp.txt");
            errLog.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Display sequences from database
     */
    @SuppressWarnings("unused")
    public void showDataBase(){
        Iterator<Sequence> seqsIter = getSequences().iterator();
        int counter = 0;
        while (seqsIter.hasNext()){
            counter++;
            logger.info(String.format("%5d", counter));
            logger.info(seqsIter.next().toString());
        }
    }

    /**
     * Check if tested string of nucleotides cover another
     * string of nucleotides from list
     * @param pairs list of begins and ends of strings
     * @param testedPair begin and end of tested string of nucleotides
     * @return true, if is not covering any of strings in given list
     */
    boolean isOk(ArrayList<Pair> pairs, Pair testedPair){
        Predicate<Pair> isFirstBetween = pair -> pair.isPointBetweenInclusive(testedPair.getFirst());
        Predicate<Pair> isSecondBetween = pair -> pair.isPointBetweenInclusive(testedPair.getSecond());
        Predicate<Pair> isInside = pair -> pair.isInsideGivenPair(testedPair);
        Predicate<Pair> isNotOk = isFirstBetween.or(isSecondBetween).or(isInside);
        return pairs.stream().noneMatch(isNotOk);
    }

    /**
     * concat Top from newSequence with fragment of tmp string from n to n + vlength
     * and concat Seq from newSequence with fragment of sequence.seq string from n to n + vlength
     * @param newSequence sequence to concat strings
     * @param sequence sequence with substring to add to Seq
     * @param tmp stringbuilder with substring to add to ToP
     * @param start start position of substring
     * @param vlength length of substring
     */
    private void concatTopAndSeq(Sequence newSequence, Sequence sequence, StringBuilder tmp, int start, int vlength){
        newSequence.setTop(newSequence.getTop().concat(
                tmp.substring(start, start + vlength)
        ));/*tops_new+=c;*/
        newSequence.setSeq(newSequence.getSeq().concat(
                sequence.getSeq().substring(start, start + vlength)
        ));/*seqs_new+=c;*/
    }

    /**
     * Unused function, to future expansion of program
     * @param sequence base sequence
     * @param vlength_seq list of lengths
     * @param vbp1_pos first list of bp_pos
     * @param vbp2_pos second list of bp_pos
     * @param vbp_order list of orders
     * @param newSequence new sequence
     */
    @SuppressWarnings("unused")
    private void createPatternShift(Sequence sequence, ArrayList<Integer> vlength_seq, ArrayList<Integer> vbp1_pos,
            ArrayList<Integer> vbp2_pos, ArrayList<Integer> vbp_order, Sequence newSequence){
        char c1, c2;
        int n;
        StringBuilder tmp = new StringBuilder(sequence.getTop());
        newSequence.setSeq("");
        newSequence.setTop("");

        if(vlength_seq.isEmpty()) {
            newSequence.setSeq(sequence.getSeq());
            newSequence.setTop(sequence.getTop());
        }
        else {
            for(int i = 0; i < vbp1_pos.size(); i++ ){
                if(vbp1_pos.get(i) < vlength_seq.get(0) && vbp1_pos.get(i) >= vlength_seq.get(0)){
                    c1 = CHARS_BP2.charAt(vbp_order.get(i));
                    c2 = CHARS_BP1.charAt(vbp_order.get(i));
                    tmp.setCharAt(vbp1_pos.get(i), c1);
                    tmp.setCharAt(vbp2_pos.get(i), c2);
                }
            }
            n = vlength_seq.get(0);
            concatTopAndSeq(newSequence, sequence, tmp, n, vlength_seq.get(1));
            int last = vlength_seq.get(1);
            for(int vlength : vlength_seq.subList(2, vlength_seq.size())){
                n += last;
                concatTopAndSeq(newSequence, sequence, tmp, n, vlength);
                last = vlength;
            }
            concatTopAndSeq(newSequence, sequence, tmp, 0, vlength_seq.get(0));
        }
    }

    /**
     * Set file, where result resultPath computing will be saved.
     * Print to standard output, if parameter is null.
     * @param resultPath path, where results will be saved.
     */
    @Override
    public void changeLogFile(Path resultPath) {
        Utils.changeLogHandler(logger, resultPath);
    }
}
