package cse;

import models.Pair;
import models.Sequence;

import java.util.ArrayList;

public class Loop extends CSE{

    static {
        li = 0;
    }

    private static int li;
    private static int start, limit;
    private final boolean openLoop;

    public Loop(boolean openLoop) {
        this.openLoop = openLoop;
    }

    public static void printResult(Sequence sequence, ArrayList<Pair> pairs, ArrayList<Integer> steps, int i1){
        li++;
        System.out.printf("NR %5d ins %d %s %s %5.2f", li, i1,
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

    public static void prepareArrays(Loop loop, ArrayList<Integer> steps,
                                     ArrayList<Integer> steps_origin, ArrayList<Boolean> direct){
        for(String seq: loop.getSeqs()){
            steps_origin.add(seq.length() - 1);
            steps.add(seq.length() - 1);
            direct.add(!CSE.BASE.contains(seq.substring(0, 1)));
        }
    }

    public static void computeStartAndLimit(Loop loop, Sequence sequence, int firstStep){
        loop.setBbps( loop.createArrayInt(sequence.getBp()));
        limit = loop.getBbps().size();
        for(int i = 0; i < 20; i++)
            loop.getBbps().add(0);
        if(firstStep > 0){
            start = 0;
            limit -= firstStep;
        }
        else
            start = -firstStep;
    }

    public static void compute(Loop loop, Sequence sequence, int i, int i1,
                               ArrayList<Integer> steps, ArrayList<Pair> pairs){
        boolean breaked = false;
        int x1, x2;
        int stepNo = 0;
        Pair testedPair;
        if(loop.getBbps().get(i) != 0 && loop.getBbps().get(i + steps.get(0)) != 0){
            if(!pairs.isEmpty())
                pairs.clear();
            x1 = i;
            x2 = x1 + steps.get(stepNo);
            pairs.add(new Pair(x1, x2));
            for(stepNo = 1; stepNo < steps.size(); stepNo++){
                x1 = x2 + loop.getBbps().get(x2);
                x2 = x1 + steps.get(stepNo);
                testedPair = new Pair(x1, x2);
                if(x1 == 0 || x2 >= loop.getBbps().size() || !loop.isOk(pairs, testedPair)){
                    breaked = true;
                    break;
                }
                pairs.add(testedPair);
            }
            if(!breaked && (loop.isOpenLoop() || i == loop.getBbps().get(x2) + x2))
                printResult(sequence, pairs, steps, i1);
        }
    }

    public static void main(String[] args){
        Loop loop;
        int nins;
        int i1, i2;
        ArrayList<Pair> pairs = new ArrayList<>();
        ArrayList<Boolean> direct = new ArrayList<>();
        ArrayList<Integer> steps_origin = new ArrayList<>(),
                steps = new ArrayList<>();

        if(isArgsNotOk(args, 4))
            return;
        nins = Integer.parseInt(args[2]);
        loop = new Loop(args[3].equals("1"));
        loop.initData(args[0], args[1]);
        prepareArrays(loop, steps, steps_origin, direct);


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
                computeStartAndLimit(loop, sequence, steps.get(0));
                for(int i = start; i < limit; i++)
                    compute(loop, sequence, i, i1, steps, pairs);
            }
            steps.set(i2, steps_origin.get(i2));
        }
    }

    public boolean isOpenLoop() {
        return openLoop;
    }
}
