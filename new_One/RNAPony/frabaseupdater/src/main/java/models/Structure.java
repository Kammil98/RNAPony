package models;

import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

@Getter
public class Structure implements Serializable {
    @Getter
    private static int maxModelsNo = 1;

    String id;
    /** null, if file for structure with this pdb id was deleted (delete
     * file from server and delete models from db).
     * 0 if file exist, but has no models, which fulfill conditions
     * of processing(keep file, but delete models from db).
     */
    int[] models;

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
}
