package models;

import lombok.Getter;

import java.util.Arrays;
import java.util.StringTokenizer;

@Getter
public class Structure {
    @Getter
    private static int maxModelsNo = 1;

    public static void resetMaxModelsNo(){
        maxModelsNo = 1;
    }

    String id;
    /** null, if file for structure with this pdb id was deleted (delete
     * file from server and delete models from db).
     * 0 if file exist, but has no models, which fulfill conditions
     * of processing(keep file, but delete models from db).
     */
    int[] models;

    /**
     * Create instance of structure with its id and models.
     * @param id id of structure.
     * @param models list of numbers of models of this structure.
     *               If it's null, then it means, that this structure was deleted.
     */
    public Structure(String id, int[] models) {
        this.id = id;
        this.models = models;
        if(models != null) {
            synchronized (Structure.class) {
                maxModelsNo = Math.max(models.length, maxModelsNo);
            }
        }
    }

    @Override
    public String toString() {
        return id + "  " + Arrays.toString(models);
    }

    /**
     * Create Structure based on given String
     * @param line String with given structure. Format need to be same as in toString method.
     * @return structure in Structure Object.
     */
    public static Structure valueOf(String line){
        int[] models;
        int tokenNo = 0;
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        String id = tokenizer.nextToken();
        String token = tokenizer.nextToken();
        if(token.equals("null")){
            return new Structure(id, null);
        }
        token = line.substring(line.indexOf('[') + 1, line.length() - 1);
        tokenizer = new StringTokenizer(token, ",");
        models = new int[tokenizer.countTokens()];
        while (tokenizer.hasMoreTokens()){
            token = tokenizer.nextToken().stripLeading();
            models[tokenNo] = Integer.parseInt(token);
            tokenNo++;
        }
        return new Structure(id, models);
    }
}
