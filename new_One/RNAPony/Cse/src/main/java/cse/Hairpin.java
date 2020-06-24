package cse;

import models.Sequence;
import utils.Utils;

import java.util.logging.Level;


public class Hairpin extends CSE{

    private int li;
    private int step;
    private boolean ok;

    /**
     * Initialize Hairpin and read database
     * @param sequenceFileName name of file with searching sequence
     * @param dBFileName name of file with database
     * @param insertion number of insertions
     */
    public Hairpin(String sequenceFileName, String dBFileName, int insertion) {
        super(sequenceFileName, dBFileName, insertion);
    }

    /**
     * Compute, wheather given sequence match or not
     * @param sequence sequence to check
     * @param bbpNo counter from outer function
     * @param i1 counter from outer function
     */
    private void compute(Sequence sequence, int bbpNo, int i1){
        li++;
        int x1, x2;
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("NR %5d ins %d %s %s %s", li, i1
                ,sequence.getPdb(), sequence.getChain(), sequence.getResol()));
        msg.append(String.format(" %d - %d %s %s", bbpNo + 1, bbpNo + 1 + step,
                sequence.getSeq().substring(bbpNo, bbpNo + step + 1),
                sequence.getTop().substring(bbpNo, bbpNo + step + 1)));
        logger.log(Level.INFO, msg.toString());
        x2 = bbpNo + step;
        int bbp2No = bbpNo - 1;
        for(int bbp2: getBbps().subList(bbpNo, x2)){
            bbp2No++;
            if(bbp2 != 0){
                x1 = bbp2No + bbp2;
                if( x1 < bbpNo || x1 > x2){
                    ok = true;
                    msg = new StringBuilder(String.format(" %d %d %s %s", bbp2No + 1, x1 + 1,
                            sequence.getSeq().substring(x1, x1 + 1), sequence.getTop().substring(x1, x1 + 1)));
                    logger.log(Level.INFO, msg.toString());
                }
            }
        }
        if(ok){
            logger.info("");
            ok = false;
        }
    }

    /**
     * Find sequences from database, which matches
     */
    public void findSequences(){
        li = 0;
        ok = false;
        int step_origin, nins;

        nins = getInsertion();
        step_origin = getSourceSequence().getSeq().length() - 1;
        logger.info(String.format("%s %s %s", getSourceSequence().getName(),
                getSourceSequence().getSeq(), getSourceSequence().getTop()));
        for(int i1 = 0; i1 <= nins; i1++){
            logger.log(Level.INFO, "INSERT= " + i1);
            step = step_origin + i1;
            for(Sequence sequence: getSequences()){
                setBbps(Utils.createArrayInt(sequence.getBp(), SEPARATORS));
                int bbpNo = -1;
                for(int bbp: getBbps()){
                    bbpNo++;
                    if(bbp == step)
                        compute(sequence, bbpNo, i1);
                }
            }
        }

    }
}
