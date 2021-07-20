package models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CifFile {
    String id;
    /** null, if file for structure with this pdb id was deleted (delete
     * file from server and delete models from db).
     * 0 if file exist, but has no models, which fulfill conditions
     * of processing(keep file, but delete models from db).
     */
    Integer[] models;
}
