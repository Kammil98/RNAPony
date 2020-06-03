package cse;

import models.Sequence;

public class Hairpin extends CSE{

    public static void main(String[] args){
        Hairpin hp = new Hairpin();
        boolean ok = false;
        int step_origin, step, nins;
        int li = 0, x1, x2;

        if(args.length != 3){
            System.out.println("Inappropriate number of arguments (given " + args.length + " while 3 was expected)\n");
            System.out.println("Usage : a.out <filename_dot> <filename_database> insert");
            return;
        }
        nins = Integer.parseInt(args[2]);
        hp.initData(args[0], args[1]);
        step_origin = hp.getSourceSequence().getSeq().length() - 1;
        for(int i1 = 0; i1 <= nins; i1++){
            System.out.println("INSERT= " + i1);
            step = step_origin + i1;
            for(Sequence sequence: hp.getSequences()){
                hp.setBbps(hp.createArrayInt(sequence.getBp()));
                int bbpNo = -1;
                for(int bbp: hp.getBbps()){
                    bbpNo++;
                    if(bbp == step){
                        li++;
                        System.out.printf("NR: %5d ins %d %s %s %11.5f", li, i1
                                ,sequence.getPdb(), sequence.getChain(), sequence.getResol());
                        System.out.printf(" %d - %d %s %s\n", bbpNo + 1, bbpNo + 1 + step,
                                sequence.getSeq().substring(bbpNo, bbpNo + step + 1), sequence.getTop().substring(bbpNo, bbpNo + step + 1));
                        x2 = bbpNo + step;
                        int bbp2No = bbpNo - 1;
                        for(int bbp2: hp.getBbps().subList(bbpNo, x2)){
                            bbp2No++;
                            if(bbp2 != 0){
                                x1 = bbp2No + bbp2;
                                if( x1 < bbpNo | x1 > x2){
                                    ok = true;
                                    System.out.printf(" %d %d %s %s\n", bbp2No + 1, x1 + 1,
                                            sequence.getSeq().substring(x1, x1 + 1),
                                            sequence.getTop().substring(x1, x1 + 1));
                                }
                            }
                        }
                        if(ok){
                            System.out.print("\n");
                            ok = false;
                        }
                    }
                }
            }
        }

    }
}
