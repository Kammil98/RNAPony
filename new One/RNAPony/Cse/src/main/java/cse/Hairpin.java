package cse;

import models.Sequence;

import java.util.logging.Level;


public class Hairpin extends CSE{

    private static int li;
    private static int step;
    private static boolean ok;
    private static void compute(Sequence sequence, int bbpNo, int i1, Hairpin hp){
        li++;
        int x1, x2;
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("NR %5d ins %d %s %s %s", li, i1
                ,sequence.getPdb(), sequence.getChain(), sequence.getResol()));
        msg.append(String.format(" %d - %d %s %s", bbpNo + 1, bbpNo + 1 + step,
                sequence.getSeq().substring(bbpNo, bbpNo + step + 1),
                sequence.getTop().substring(bbpNo, bbpNo + step + 1)));
        CSE.logger.log(Level.INFO, msg.toString());
        x2 = bbpNo + step;
        int bbp2No = bbpNo - 1;
        for(int bbp2: hp.getBbps().subList(bbpNo, x2)){
            bbp2No++;
            if(bbp2 != 0){
                x1 = bbp2No + bbp2;
                if( x1 < bbpNo || x1 > x2){
                    ok = true;
                    msg = new StringBuilder(String.format(" %d %d %s %s", bbp2No + 1, x1 + 1,
                            sequence.getSeq().substring(x1, x1 + 1), sequence.getTop().substring(x1, x1 + 1)));
                    CSE.logger.log(Level.INFO, msg.toString());
                }
            }
        }
        if(ok){
            CSE.logger.info("");
            ok = false;
        }
    }



    public static void main(String[] args){
        li = 0;
        ok = false;
        Hairpin hp = new Hairpin();
        int step_origin, nins;

        if(isArgsNotOk(args, 3))
            return;
        nins = Integer.parseInt(args[2]);
        hp.initData(args[0], args[1]);
        step_origin = hp.getSourceSequence().getSeq().length() - 1;

        for(int i1 = 0; i1 <= nins; i1++){
            CSE.logger.log(Level.INFO, "INSERT= " + i1);
            step = step_origin + i1;
            for(Sequence sequence: hp.getSequences()){
                hp.setBbps(hp.createArrayInt(sequence.getBp()));
                int bbpNo = -1;
                for(int bbp: hp.getBbps()){
                    bbpNo++;
                    if(bbp == step)
                        compute(sequence, bbpNo, i1, hp);
                }
            }
        }

    }
}
