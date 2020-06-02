package cse;

import maintokenizers.StringTokenizer;///check which one is fastest
import models.Pair;
import models.Sequence;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class CSE {
    //private ArrayList<String> vb_pdb, vb_chain, vb_top, vbs_bp, vb_seq;
    //private ArrayList<Float> vb_resol;
    //private ArrayList<Integer> vb_order;
    public static final String SEPARATORS = ";";
    private ArrayList<Sequence> sequences;
    private ArrayList<String> seqs;
    private ArrayList<String> tops;
    private ArrayList<Integer> bbps;
    private final Sequence sourceSequence;

    //functions create_vect and create_vectint from c++ version are replaced
    //by StringTokenizer.getStreamOfTokens(source, SEPARATORS)
    //returning String need only by map in following way:
    //StringTokenizer.getStreamOfTokens(source, SEPARATORS).map(s -> Integer.valueOf(s)).collect(Collectors.toList())
    //and add following List to target ArrayList

    public CSE(){
        sequences = new ArrayList<>();
        /*vb_pdb = new ArrayList<>();
        vb_chain = new ArrayList<>();
        vb_resol = new ArrayList<>();
        vb_seq = new ArrayList<>();
        vb_top = new ArrayList<>();
        vbs_bp = new ArrayList<>();
        vb_order = new ArrayList<>();*/
        sourceSequence = new Sequence();
    }

    private void initData(String args[]){
        Path filesPath = Path.of("./", "files"),
                MpSeqFP = Path.of(filesPath.toString(), args[0]),
                dBFP = Path.of(filesPath.toString(), args[1]);

        readMpSeq(MpSeqFP.toString());
        readDataBase(dBFP.toString());
        seqs = StringTokenizer.getStreamOfTokens(sourceSequence.getSeq(), SEPARATORS)
                .collect(Collectors.toCollection(ArrayList::new));
        tops = StringTokenizer.getStreamOfTokens(sourceSequence.getTop(), SEPARATORS)
                .collect(Collectors.toCollection(ArrayList::new));
        bbps = StringTokenizer.getStreamOfTokens(sourceSequence.getTop(), SEPARATORS)
                .map(s -> Integer.valueOf(s)).collect(Collectors.toCollection(ArrayList::new));
    }

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

    public void readDataBase(String fileName){
        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(fileName)
                            ))){
            String line;
            String pdb, chain, seq, top, bp;
            float resol;
            int order;
            for(StringTokenizer tokenizer = null;
                (line = reader.readLine()) != null;){

                tokenizer = new StringTokenizer(line, " ");
                if(tokenizer.countTokens() == 7){
                    pdb = tokenizer.nextToken();
                    chain = tokenizer.nextToken();
                    resol = Float.parseFloat(tokenizer.nextToken());
                    seq = tokenizer.nextToken();
                    top = tokenizer.nextToken();
                    bp = tokenizer.nextToken();
                    order = Integer.parseInt(tokenizer.nextToken());
                    getSequences().add(new Sequence(pdb, chain, resol, seq, top, bp, order));
                    /*vb_pdb.add(tokenizer.nextToken());
                    vb_chain.add(tokenizer.nextToken());
                    vb_resol.add(Float.valueOf(tokenizer.nextToken()));
                    vb_seq.add(tokenizer.nextToken());
                    vb_top.add(tokenizer.nextToken());
                    vbs_bp.add(tokenizer.nextToken());
                    vb_order.add(Integer.valueOf(tokenizer.nextToken()));*/
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDataBase(){
        Iterator<Sequence> seqsIter = getSequences().iterator();
        int counter = 0;
        while (seqsIter.hasNext()){
            counter++;
            System.out.println(seqsIter.next());
        }
        /*Iterator<String> pdbIter = vb_pdb.iterator(), chainIter = vb_chain.iterator(),
                seqIter = vb_seq.iterator(), topIter = vb_top.iterator(),
                bpIter = vbs_bp.iterator();
        Iterator<Float> resolIter = vb_resol.iterator();
        Iterator<Integer> orderIter = vb_order.iterator();

        //all Arrays have the same length, soo it's enough to check
        //only on iterator.hasNext()
        while(pdbIter.hasNext()){
            counter++;
            System.out.printf("%-5d\n", counter);
            System.out.printf("ID PDB: %s    CHAINS: %s    ORDER: %d   RESOLUTION: %f\n",
                    pdbIter.next(), chainIter.next(), orderIter.next(), resolIter.next());
            System.out.printf("SEQUENCE: %s\n", seqIter.next());
            System.out.printf("TOPOLOGY: %s\n", topIter.next());
            System.out.printf("BPSEQ:    %s\n\n", bpIter.next());
        }*/
    }

    public boolean isOk(ArrayList<Pair> pairs, Pair testedPair){
        Predicate<Pair> isFirstBetween = pair -> pair.isPointBetweenInclusive(testedPair.getFirst());
        Predicate<Pair> isSecondBetween = pair -> pair.isPointBetweenInclusive(testedPair.getSecond());
        Predicate<Pair> isInside = pair -> pair.isInsideGivenPair(testedPair);
        Predicate<Pair> isNotOk = isFirstBetween.or(isSecondBetween).or(isInside);
        return !pairs.stream().anyMatch(isNotOk);
    }

    public void createPatternShift(){

    }

    public static void main(String args[]) {

    }


    public ArrayList<Sequence> getSequences() {
        return sequences;
    }

    public Sequence getSourceSequence() {
        return sourceSequence;
    }
}
