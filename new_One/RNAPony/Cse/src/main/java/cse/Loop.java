package cse;

import csemodels.Pair;
import models.Sequence;
import utils.Utils;

import java.util.ArrayList;
import java.util.logging.Level;

public class Loop extends CSE{

    private static int li;
    private static int start, limit;
    private final boolean openLoop;

    /**
     * Initialize Loop and read database
     * @param sequenceFileName name of file with searching sequence
     * @param dBFileName name of file with database
     * @param insertion number of insertions
     * @param openLoop true, if it is openLoop, false otherwise
     */
    public Loop(String sequenceFileName, String dBFileName, int insertion, boolean openLoop) {
        super(sequenceFileName, dBFileName, insertion);
        this.openLoop = openLoop;
    }

    /**
     * Finding subsequence in given sequence and print it
     * @param sequence sequence with subsequence to extract
     * @param pairs array of pairs
     * @param steps array of steps
     * @param i1 counter from outer function
     */
    private void printResult(Sequence sequence, ArrayList<Pair> pairs, ArrayList<Integer> steps, int i1){
        li++;
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("NR %5d ins %d %s %s %s", li, i1,
                sequence.getPdb(), sequence.getChain(), sequence.getResol()));
        int pairNo = -1;
        int step;
        int from, to, end;

        for(Pair pair: pairs){
            pairNo++;
            step = steps.get(pairNo);
            if(step > 0){
                from = pair.getFirst();
                to = pair.getSecond();
                end = from + step + 1;
            }
            else {
                from = pair.getSecond();
                to = pair.getFirst();
                end = from - (step - 1);
            }
            msg.append(String.format(" %d - %d %s %s", from + 1, to + 1,
                    sequence.getSeq().substring(from, end),
                    sequence.getTop().substring(from, end)));
        }
        logger.log(Level.INFO, msg.toString());
    }

    /**
     * initialize arrays
     * @param steps array of steps
     * @param steps_origin array of origin steps
     * @param direct direct array
     */
    private void prepareArrays(ArrayList<Integer> steps,
                                     ArrayList<Integer> steps_origin, ArrayList<Boolean> direct){
        for(String seq: getSeqs()){
            steps_origin.add(seq.length() - 1);
            steps.add(seq.length() - 1);
            direct.add(!CSE.BASE.contains(seq.substring(0, 1)));
        }
    }

    /**
     * Compute values of start and limit
     * @param sequence currently checking sequence
     * @param firstStep first step
     */
    private void computeStartAndLimit(Sequence sequence, int firstStep){
        setBbps(Utils.createArrayInt(sequence.getBp(), SEPARATORS));
        limit = getBbps().size();
        for(int i = 0; i < 20; i++)
            getBbps().add(0);
        if(firstStep > 0){
            start = 0;
            limit -= firstStep;
        }
        else
            start = -firstStep;
    }

    private void compute(Sequence sequence, int i, int i1,
                               ArrayList<Integer> steps, ArrayList<Pair> pairs){
        boolean breaked = false;
        int x1, x2;
        int stepNo = 0;
        Pair testedPair;
        if(getBbps().get(i) != 0 && getBbps().get(i + steps.get(0)) != 0){
            if(!pairs.isEmpty())
                pairs.clear();
            x1 = i;
            x2 = x1 + steps.get(stepNo);
            pairs.add(new Pair(x1, x2));
            for(stepNo = 1; stepNo < steps.size(); stepNo++){
                x1 = x2 + getBbps().get(x2);
                x2 = x1 + steps.get(stepNo);
                testedPair = new Pair(x1, x2);
                if(x1 == 0 || x2 >= getBbps().size() || !isOk(pairs, testedPair)){
                    breaked = true;
                    break;
                }
                pairs.add(testedPair);
            }
            if(!breaked && (isOpenLoop() || i == getBbps().get(x2) + x2))
                printResult(sequence, pairs, steps, i1);
        }
    }

    /**
     * Find sequences from database, which matches
     */
    public void findSequences(){
        li = 0;
        int nins;
        int i1, i2;
        ArrayList<Pair> pairs = new ArrayList<>();
        ArrayList<Boolean> direct = new ArrayList<>();
        ArrayList<Integer> steps_origin = new ArrayList<>(),
                steps = new ArrayList<>();

        nins = getInsertion();
        prepareArrays(steps, steps_origin, direct);
        logger.info(String.format("%s %s %s", getSourceSequence().getName(),
                getSourceSequence().getSeq(), getSourceSequence().getTop()));

        for(int i0 = steps_origin.size() - 1;
            i0 < ((nins + 1) * steps_origin.size()); i0++){
            i1 = i0 / steps_origin.size();
            logger.log(Level.INFO, "INSERT= " + i1);
            i2 = i0 % steps_origin.size();
            if(direct.get(i2))
                steps.set(i2, steps_origin.get(i2) + i1);
            else
                steps.set(i2, -(steps_origin.get(i2) + i1));
            for(Sequence sequence: getSequences()){
                computeStartAndLimit(sequence, steps.get(0));
                for(int i = start; i < limit; i++)
                    compute(sequence, i, i1, steps, pairs);
            }
            steps.set(i2, steps_origin.get(i2));
        }
    }

    public boolean isOpenLoop() {
        return openLoop;
    }
}
