package homology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import cse.CSE;
import models.Sequence;

import java.util.ArrayList;
import java.util.logging.*;

public class Homology {
    public final Logger logger;
    private static final String BRACKET1, BRACKET2;
    private boolean saveToFile = false;
    private final Sequence sequence0;
    private int lbp0, lbp;
    private int seqLength;
    private int homeSeq, homeBp;
    private int[] w01;
    private int[] w02;
    private int[] w1;
    private int[] w2;

    static {
        BRACKET1="([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BRACKET2=")]}>abcdefghijklmnopqrstuvwxyz";
    }

    public Homology(){
        sequence0 = new Sequence();
        logger = Logger.getLogger(Homology.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%5$s%n");
        logger.setUseParentHandlers(false);
        changeLogFile(Path.of("./", "default.txt"));
    }

    /**
     * Change file, where logs will be saving
     * @param filePath path to file, to write logs in
     */
    public void changeLogFile(Path filePath){
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

    public void bpSet(Sequence sequence){
        seqLength = sequence.getTop().length();
        lbp = 0;
        char d1, d2, sp = 0;
        int index, h, j;
        for(int i = 1; i <= seqLength; i++){
            d1 = sequence.getTop().charAt(i - 1);
            index = BRACKET1.indexOf(d1) + 1;
            if(index != 0){//if char was found
                d2 = BRACKET2.charAt(index - 1);
                h = 1;
                j = i + 1;
                while ((h > 0) & j <= seqLength){
                    sp = sequence.getTop().charAt(j - 1);
                    if(sp == d1)
                        h++;
                    if(sp == d2)
                        h--;
                    j++;
                }
                if(sp == d2){
                    lbp++;
                    w1[lbp] = i;
                    w2[lbp] = j - 1;
                }
            }
        }
    }

    public void homology(Sequence sequence){
        seqLength = sequence.getSeq().length();
        homeSeq = homeBp = 0;
        for(int i = 1; i <= seqLength; i++) {
            if (sequence.getSeq().charAt(i - 1) == sequence0.getSeq().charAt(i - 1))
                homeSeq++;
        }
        bpSet(sequence);
        for(int i = 1; i <= lbp; i++){
            for(int i0 = 1; i0 <= lbp0; i0++){
                if(w01[i0] == w1[i] & w02[i0] == w2[i])
                    homeBp++;
            }
        }
    }

    /**
     * compute homology for all sequences in file
     * @param fileName path to file with sequences
     */
    public void compute(Path fileName){

        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(fileName.toString())
                            ))){
            String line;
            ArrayList <String> tokens;
            tokens = CSE.createArray(reader.readLine());
            sequence0.setName(tokens.get(0));
            sequence0.setSeq(tokens.get(1));
            sequence0.setTop(tokens.get(2));
            logger.info(String.format("REFERENCE: %s\n",sequence0.getName()));
            bpSet(sequence0);
            lbp0 = lbp;
            logger.info(String.format("%s\n%s %d\n",sequence0.getSeq(), sequence0.getTop(), lbp0));

            w01 = new int[lbp0 + 1];
            w02 = new int[lbp0 + 1];
            w1 = new int[lbp0 + 1];
            w2 = new int[lbp0 + 1];
            for(int i = 1; i <= lbp0; i++){
                w01[i] = w1[i];
                w02[i] = w2[i];
                logger.info(String.format("LBP0=%d %s%d %s%d\n", i, sequence0.getSeq().charAt(w01[i] - 1), w01[i],
                        sequence0.getSeq().charAt(w02[i] - 1), w02[i]));
            }
            Sequence sequence;
            for(;(line = reader.readLine()) != null;){
                tokens = CSE.createArray(line);
                if(tokens.size() <= 11)
                    continue;
                sequence = new Sequence();
                sequence.setSeq(tokens.get(10));
                sequence.setTop(tokens.get(11));
                for(int i = 15; i < tokens.size(); i += 5){
                    sequence.setSeq(sequence.getSeq() + ";" + tokens.get(i));
                    sequence.setTop(sequence.getTop() + ";" + tokens.get(i + 1));
                }
                homology(sequence);
                seqLength = sequence.getSeq().length();
                logger.info(String.format("%s %8.3f %3d\n", line, homeSeq * 100 / seqLength, homeBp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        Homology homo = new Homology();
        System.out.println(Path.of(".").toAbsolutePath());
        homo.compute(Path.of(".", "files", "ur4_L1_0.txt"));
    }
    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }
}
