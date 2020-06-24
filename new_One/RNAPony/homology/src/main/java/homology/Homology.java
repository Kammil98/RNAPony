package homology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import models.Sequence;
import utils.Utils;
import java.util.ArrayList;
import java.util.logging.*;

public class Homology {

    public final Logger logger;
    private static final String SEPARATORS, BRACKET1, BRACKET2;
    private final Sequence sequence0;
    private int lbp0;
    private final ArrayList<Integer> w01;
    private final ArrayList<Integer> w02;
    private final ArrayList<Integer> w1;
    private final ArrayList<Integer> w2;

    static {
        SEPARATORS = " \t\f";
        BRACKET1="([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BRACKET2=")]}>abcdefghijklmnopqrstuvwxyz";
    }

    /**
     * Initiliza Homology Object
     */
    public Homology(){
        sequence0 = new Sequence();
        w01 = new ArrayList<>();
        w02 = new ArrayList<>();
        w1 = new ArrayList<>();
        w2 = new ArrayList<>();
        logger = Logger.getLogger(Homology.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%5$s%n");
        logger.setUseParentHandlers(false);
        Utils.changeLogHandler(logger);
    }

    public class HomologyResult{
        public HomologyResult(int homeSeq, int homeBp){
            this.homeSeq = homeSeq;
            this.homeBp = homeBp;
        }
        public int homeSeq, homeBp;
    }

    /**
     * @param sequence sequence with given seq and top field
     * @return lbp
     */
    private int bpSet(Sequence sequence){
        int seqLength = sequence.getTop().length();
        int lbp = 0;
        char nucleotide1, nucleotide2, nucleotide2Top = 0;
        int index, h, nuc2Index;

        for(int nuc1Index = 0; nuc1Index < seqLength; nuc1Index++){
            nucleotide1 = sequence.getTop().charAt(nuc1Index);
            index = BRACKET1.indexOf(nucleotide1);
            if(index != -1){//if char was found
                nucleotide2 = BRACKET2.charAt(index);
                h = 1;
                nuc2Index = nuc1Index + 1;
                while ((h > 0) & nuc2Index < seqLength){
                    nucleotide2Top = sequence.getTop().charAt(nuc2Index);
                    if(nucleotide2Top == nucleotide1)
                        h++;
                    if(nucleotide2Top == nucleotide2)
                        h--;
                    nuc2Index++;
                }
                if(nucleotide2Top == nucleotide2){
                    lbp++;
                    if(w1.size() < lbp){
                        w1.add(nuc1Index);
                        w2.add(nuc2Index - 1);
                    }
                    else{
                        w1.set(lbp, nuc1Index);
                        w2.set(lbp, nuc2Index - 1);
                    }
                }
            }
        }
        return lbp;
    }

    /**
     * Compute homology for single sequence
     * @param sequence sequence Object with given seq and top fields. Contain sequence, to compute homology
     */
    private HomologyResult homology(Sequence sequence){
        int seqLength = sequence.getSeq().length();
        int homeSeq, homeBp;
        homeSeq = homeBp = 0;
        for(int i = 0; i < seqLength; i++) {
            if (sequence.getSeq().charAt(i) == sequence0.getSeq().charAt(i))
                homeSeq++;
        }
        int lbp = bpSet(sequence);
        for(int i = 0; i < lbp; i++){
            for(int i0 = 0; i0 < lbp0; i0++){
                if(w01.get(i0).equals(w1.get(i)) & w02.get(i0).equals(w2.get(i)))
                    homeBp++;
            }
        }
        return new HomologyResult(homeSeq, homeBp);
    }

    /**
     * initialize lbp0, w01 and w02 values.
     * Log some basic information about basic sequence
     * @param reader reader to file with sequences
     * @throws IOException
     */
    private void initTargetSequence(BufferedReader reader) throws IOException {
        ArrayList <String> tokens;
        tokens = Utils.createArray(reader.readLine(), SEPARATORS);
        sequence0.setName(tokens.get(0));
        sequence0.setSeq(tokens.get(1));
        sequence0.setTop(tokens.get(2));
        logger.info(String.format("REFERENCE: %s",sequence0.getName()));
        lbp0 = bpSet(sequence0);
        logger.info(String.format("%s\n%s %d",sequence0.getSeq(), sequence0.getTop(), lbp0));
        w01.addAll(w1);
        w02.addAll(w2);
        for(int i = 0; i < lbp0; i++){
            logger.info(String.format("LBP0=%d %s%d %s%d", i + 1,
                    sequence0.getSeq().charAt(w01.get(i)), w01.get(i) + 1,
                    sequence0.getSeq().charAt(w02.get(i)), w02.get(i) + 1));
        }
    }

    /**
     * compute homology for all sequences in file
     * @param fileName name of file with sequences
     */
    public void compute(String fileName){

        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(getClass().getResourceAsStream("/" + fileName))
                    )){
            String line;
            ArrayList <String> tokens;
            Sequence sequence;
            HomologyResult result;

            initTargetSequence(reader);
            while ((line = reader.readLine()) != null) {
                tokens = Utils.createArray(line, SEPARATORS);
                if(tokens.size() <= 11)
                    continue;
                sequence = new Sequence();
                sequence.setSeq(tokens.get(10));
                sequence.setTop(tokens.get(11));
                for(int i = 15; i < tokens.size(); i += 5){
                    sequence.setSeq(sequence.getSeq() + ";" + tokens.get(i));
                    sequence.setTop(sequence.getTop() + ";" + tokens.get(i + 1));
                }
                result = homology(sequence);
                logger.info(String.format("%s %8.3f %3d", line, (float)(result.homeSeq * 100) / sequence.getSeq().length(), result.homeBp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
