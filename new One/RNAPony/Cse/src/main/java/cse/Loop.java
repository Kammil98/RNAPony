package cse;

import models.Pair;
import models.Sequence;

import java.util.ArrayList;

public class Loop extends CSE{

    public static void main(String[] args){
        Loop loop = new Loop();
        boolean breaked;
        int nins;
        int li = 0, x1, x2, i1, i2, limit, start, stepNo;
        Pair testedPair;
        ArrayList<Pair> pairs = new ArrayList<>();
        ArrayList<Boolean> direct = new ArrayList<>();
        ArrayList<Integer> steps_origin = new ArrayList<>(),
                steps = new ArrayList<>();
        if(args.length != 3){
            System.out.println("Inappropriate number of arguments (given " + args.length + " while 3 was expected)\n");
            System.out.println("Usage : a.out <filename_dot> <filename_database> insert");
            return;
        }
        nins = Integer.parseInt(args[2]);
        loop.initData(args[0], args[1]);

        for(String seq: loop.getSeqs()){
            steps_origin.add(seq.length() - 1);
            steps.add(seq.length() - 1);
            direct.add(!CSE.BASE.contains(seq.substring(0, 1)));
        }

        for(int i0 = steps_origin.size() - 1;
            i0 < ((nins + 1) * steps_origin.size()); i0++){
            i1 = i0 / steps_origin.size();
            System.out.println("INSERT= " + i1);
            i2 = i0 % steps_origin.size();
            if(direct.get(i2))
                steps.set(i2, steps_origin.get(i2) + i1);
            else
                steps.set(i2, -(steps_origin.get(i2) + i1));
            for(Sequence sequence: loop.getSequences()){
                loop.setBbps( loop.createArrayInt(sequence.getBp()));
                limit = loop.getBbps().size();
                for(int i = 0; i < 20; i++)
                    loop.getBbps().add(0);
                if(steps.get(0) > 0){
                    start = 0;
                    limit -= steps.get(0);
                }
                else
                    start = -steps.get(0);
                for(int i = start; i < limit; i++){
                    if(loop.getBbps().get(i) != 0 & loop.getBbps().get(i + steps.get(0)) != 0){
                        if(!pairs.isEmpty())
                            pairs.clear();
                        stepNo = 0;
                        x1 = i;
                        x2 = x1 + steps.get(stepNo);
                        pairs.add(new Pair(x1, x2));
                        breaked = false;
                        for(stepNo = 1; stepNo < steps.size(); stepNo++){
                            x1 = x2 + loop.getBbps().get(x2);
                            x2 = x1 + steps.get(stepNo);
                            testedPair = new Pair(x1, x2);
                            if(x1 == 0 | x2 >= loop.getBbps().size() | !loop.isOk(pairs, testedPair)){
                                breaked = true;
                                break;
                            }
                            pairs.add(testedPair);
                        }
                        if(!breaked & i == loop.getBbps().get(x2) + x2){
                            li++;
                            System.out.printf("NR %5d ins %d %s %s %11.5f", li, i1,
                                    sequence.getPdb(), sequence.getChain(), sequence.getResol());
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
                                System.out.printf(" %d - %d %s %s", from + 1, to + 1,
                                        sequence.getSeq().substring(from, end),
                                        sequence.getTop().substring(from, end));
                            }
                            System.out.print("\n");
                        }
                    }
                }
            }
            steps.set(i2, steps_origin.get(i2));
        }
    }
}
