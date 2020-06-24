package cse;

import maintokenizers.StringTokenizer;
import models.Pair;
import models.Sequence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.logging.*;
import java.util.stream.Collectors;

public abstract class CSE {
    private String sequenceFileName;
    private String dBFileName;
    private int insertion;
    public final Logger logger;
    private boolean saveToFile = false;
    public static final String CHARS_BP1 = "([{<ABCDEFGHIJK";
    public static final String CHARS_BP2 = ")]}>abcdefghijk";
    public static final String BASE = "acgu";
    public static final String SEPARATORS = ";";
    private final ArrayList<Sequence> sequences;
    private ArrayList<String> seqs;
    private ArrayList<String> tops;
    private ArrayList<Integer> bbps;
    private final Sequence sourceSequence;

    /**
     * Change file, where logs will be saving
     * @param filePath path to file, to write logs in
     */
    public void changeLogHandler(Path filePath){
        for(Handler handler : logger.getHandlers()){//remove old handlers
            handler.close();
            logger.removeHandler(handler);
        }

        Handler handler = null;
        if(isSaveToFile()){
            try {
                handler = new FileHandler(filePath.toString(), false);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        else
            handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    /**
     * Initialize CSE and read database
     * @param sequenceFileName name of file with searching sequence
     * @param dBFileName name of file with database
     * @param insertion number of insertions
     */
    public CSE(String sequenceFileName, String dBFileName, int insertion){
        logger = Logger.getLogger(cse.CSE.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%5$s%n");
        logger.setUseParentHandlers(false);
        changeLogHandler(Path.of("./", "default.txt"));

        this.setSequenceFileName(sequenceFileName);
        this.setdBFileName(dBFileName);
        this.setInsertion(insertion);
        sequences = new ArrayList<>();
        seqs = new ArrayList<>();
        tops = new ArrayList<>();
        bbps = new ArrayList<>();
        sourceSequence = new Sequence();

        initData(sequenceFileName, dBFileName);
    }

    /**
     * Main function, which calculate
     * and display sequences
     */
    public abstract void findSequences();

    /**
     * Read necessary data from files.
     * All files should be placed in "files" folder
     * @param MPseqFile name of file with one sequence
     * @param dbFile name of file with sequences from database
     */
    public void initData(String MPseqFile, String dbFile){
        Path startPath;
        if(Files.exists(Path.of("./","files"))){//Release Path
            startPath = Path.of("./", "files");
        }
        else{//Test Path
            startPath = Path.of("../", "files");
        }
        Path filesPath = Path.of(startPath.toString()),
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
    public static ArrayList<String> createArray(String source){
        return StringTokenizer.getStreamOfTokens(source, SEPARATORS)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Find group of tokens and save it to ArrayList of Integers
     * @param source String with integer tokens divided by Separators
     * @return ArrayList of All elements from source
     */
    public static ArrayList<Integer> createArrayInt(String source){
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
     * Read necessary data about sequences from database.
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
    public boolean isOk(ArrayList<Pair> pairs, Pair testedPair){
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

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
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

    public String getSequenceFileName() {
        return sequenceFileName;
    }

    public void setSequenceFileName(String sequenceFileName) {
        this.sequenceFileName = sequenceFileName;
    }

    public int getInsertion() {
        return insertion;
    }

    public void setInsertion(int insertion) {
        this.insertion = insertion;
    }

    public String getdBFileName() {
        return dBFileName;
    }

    public void setdBFileName(String dBFileName) {
        this.dBFileName = dBFileName;
    }
}
