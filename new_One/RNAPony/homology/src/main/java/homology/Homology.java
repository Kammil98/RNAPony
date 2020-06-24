package homology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import models.Sequence;
import utils.Utils;
import java.util.ArrayList;
import java.util.logging.*;

public class Homology {

    public final Logger logger;
    private static final String SEPARATORS, BRACKET1, BRACKET2;
    private boolean saveToFile = false;
    private final Sequence sequence0;
    private int lbp0, lbp;
    private int homeSeq, homeBp;
    private ArrayList<Integer> w01;
    private ArrayList<Integer> w02;
    private ArrayList<Integer> w1;
    private ArrayList<Integer> w2;

    static {
        SEPARATORS = " \t\f";
        BRACKET1="([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BRACKET2=")]}>abcdefghijklmnopqrstuvwxyz";
    }

    public Homology(){
        sequence0 = new Sequence();
        w01 = new ArrayList<>();
        w02 = new ArrayList<>();
        w1 = new ArrayList<>();
        w2 = new ArrayList<>();
        w01.add(-1);//because we count from 1 as in AWK
        w02.add(-1);
        w1.add(-1);
        w2.add(-1);
        logger = Logger.getLogger(Homology.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%5$s%n");
        logger.setUseParentHandlers(false);
        Utils.changeLogHandler(logger);
    }

    public void bpSet(Sequence sequence){
        int seqLength = sequence.getTop().length();
        lbp = 0;
        char d1, d2, sp = 0;
        int index, h, j;
        //logger.info(sequence.getSeq() + "\n" + sequence.getTop());
        for(int i = 1; i <= seqLength; i++){
            d1 = sequence.getTop().charAt(i - 1);
            index = BRACKET1.indexOf(d1) + 1;
            if(index != 0){//if char was found
                d2 = BRACKET2.charAt(index - 1);
                //logger.info("d1 = " + d1 + " d2 = " + d2 + " index = " + index);
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
                    if(w1.size() <= lbp){
                        w1.add(i);
                        w2.add(j - 1);
                    }
                    else{
                        w1.set(lbp, i);
                        w2.set(lbp, j - 1);
                    }
                    /*logger.info(String.format("i=%d j=%d h=%d",i,j,h));
                    logger.info(String.format("LBP=%d %s%d %s%d", lbp, sequence.getSeq().charAt(w1.get(lbp) - 1),
                            w1.get(lbp), sequence.getSeq().charAt(w2.get(lbp) - 1), w2.get(lbp)));*/
                }
            }
        }
    }

    public void homology(Sequence sequence){
        int seqLength = sequence.getSeq().length();
        homeSeq = homeBp = 0;
        for(int i = 1; i <= seqLength; i++) {
            if (sequence.getSeq().charAt(i - 1) == sequence0.getSeq().charAt(i - 1))
                homeSeq++;
        }
        bpSet(sequence);
        for(int i = 1; i <= lbp; i++){
            for(int i0 = 1; i0 <= lbp0; i0++){
                if(w01.get(i0) == w1.get(i) & w02.get(i0) == w2.get(i))
                    homeBp++;
            }
        }
    }

    /**
     * compute homology for all sequences in file
     * @param fileName path to file with sequences
     */
    public void compute(String fileName){

        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(getClass().getResourceAsStream("/" + fileName))
                    )){
            String line;
            ArrayList <String> tokens;
            tokens = Utils.createArray(reader.readLine(), SEPARATORS);
            sequence0.setName(tokens.get(0));
            sequence0.setSeq(tokens.get(1));
            sequence0.setTop(tokens.get(2));
            logger.info(String.format("REFERENCE: %s",sequence0.getName()));
            bpSet(sequence0);
            lbp0 = lbp;
            logger.info(String.format("%s\n%s %d",sequence0.getSeq(), sequence0.getTop(), lbp0));

            for(int i = 1; i <= lbp0; i++){
                w01.add(w1.get(i));
                w02.add(w2.get(i));
                logger.info(String.format("LBP0=%d %s%d %s%d", i, sequence0.getSeq().charAt(w01.get(i) - 1), w01.get(i),
                        sequence0.getSeq().charAt(w02.get(i) - 1), w02.get(i)));
            }
            Sequence sequence;
            for(;(line = reader.readLine()) != null;){
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

                homology(sequence);
                logger.info(String.format("%s %8.3f %3d", line, (float)(homeSeq * 100) / sequence.getSeq().length(), homeBp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Homology homo = new Homology();
        //ClassLoader classLoader = homo.getClass().getClassLoader();
        //homo.logger.info(classLoader.getResource("ur4_L1_0.txt").getFile());
        homo.compute("ur4_L1_0.txt");
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }
}
