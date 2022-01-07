package updater;

import lombok.AccessLevel;
import lombok.Getter;
import models.Database;
import models.Structure;
import models.DBrecord;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBUpdater implements Closeable {

    @Getter(AccessLevel.PACKAGE)
    private final Connection conn;
    @Getter
    private static final ConcurrentLinkedQueue<Structure> updatedFiles = new ConcurrentLinkedQueue<>();
    public static final Path updatedStructuresPath = Main.frabaseDir.resolve("UpdatedStructures.txt");
    private static final int queryBatchSize = 1000;

    public DBUpdater() throws SQLException {
        this(Database.getDbTableName());
    }

    public DBUpdater(String tableName) throws SQLException {
        String jdbcUrl = "jdbc:postgresql://" + Database.getDbHost() + ":" +
                Database.getDbPort() + "/" + Database.getDbName();
        conn = DriverManager.getConnection(jdbcUrl, Database.getDbUser(), Database.getDbUserPasswd());
        Statement stmt = conn.createStatement();
        Database.setDbTableName(tableName);
        String CreateSql = "Create Table IF NOT EXISTS " + Database.getDbTableName() + "(" +
                "id SERIAL PRIMARY KEY, " +
                "pdbid varchar(4), " +
                "modelno int, " +
                "chain text NOT NULL, " +
                "resol numeric(5,2) NOT NULL," +
                "seq text NOT NULL," +
                "dot text NOT NULL," +
                "dotintervals text NOT NULL," +
                "maxorder int NOT NULL," +
                "UNIQUE (pdbid, modelno) )";
        stmt.executeUpdate(CreateSql);
        stmt.close();
    }

    /**
     * Handle printing SQL errors.
     * @param e error to print.
     */
    private void handleSqlError(SQLException e){
        while(e!= null){
            Main.verboseInfo("Couldn't execute sql command: \n" + e.getSQLState() + "\n" + e.getErrorCode() +
                    "\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()), 1);
            Main.errLogger.severe("Couldn't execute sql command: \n" + e.getSQLState() + "\n" + e.getErrorCode() +
                    "\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            e = e.getNextException();
        }
    }

    /**
     * Add single DBrecord to batch, which will be send to database.
     * @param record recorde to be added.
     * @param pstmt PreparedStatement, which keep batch to be sent.
     * @throws SQLException if a database access error occurs or this method is called on a closed PreparedStatement.
     */
    private void addRecordToBatch(DBrecord record, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, record.getId());
        pstmt.setInt(2, record.getModelNo());
        pstmt.setString(3, record.getChain());
        pstmt.setDouble(4, record.getResol());
        pstmt.setString(5, record.getSeq());
        pstmt.setString(6, record.getDot());
        pstmt.setString(7, record.getDotIntervals());
        pstmt.setInt(8, record.getMaxOrder());
        pstmt.addBatch();
    }

    /**
     * Add given records to database.
     * @param records records to be added.
     * @return amount of affected rows.
     */
    private int addRecordsToDB(ArrayList<DBrecord> records){
        int affectedRows = 0;
        int[] affectedRowsList;
        String batchCommand = "INSERT INTO " + Database.getDbTableName() +
                "  (pdbid, modelno, chain, resol, seq, dot, dotintervals, maxorder) " +
                "VALUES  (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(pdbid, modelno) DO UPDATE SET " +
                "chain=excluded.chain, " +
                "resol=excluded.resol, " +
                "seq=excluded.seq, " +
                "dot=excluded.dot, " +
                "dotintervals=excluded.dotintervals, " +
                "maxorder=excluded.maxorder;";
        try (PreparedStatement pstmt = conn.prepareStatement(batchCommand)){
            conn.setAutoCommit(false);
            for(DBrecord record: records)
                addRecordToBatch(record, pstmt);
            affectedRowsList = pstmt.executeBatch();
            for (int val : affectedRowsList)
                affectedRows += val;
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException throwables) {
            handleSqlError(throwables);
        }
        return affectedRows;
    }

    /**
     * Add new records to database. If some records with this pdbID and modelNo
     * exist, then this record in database is updated.
     * @param newRecordsPath path to file with new records.
     * @return amount of affected rows.
     */
    public int addOrUpdateNewRecords(Path newRecordsPath){
        int affectedRows = 0;
        ArrayList<DBrecord> records = new ArrayList<>(queryBatchSize);
        try(Scanner recordsReader = new Scanner(newRecordsPath.toFile())){
            while (recordsReader.hasNextLine()){
                records.add(DBrecord.valueOf(recordsReader.nextLine()));
                if(records.size() == queryBatchSize) {//to save memory  - can't load whole database at once
                    affectedRows += addRecordsToDB(records);
                    records.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(!records.isEmpty())
            affectedRows += addRecordsToDB(records);
        return affectedRows;
    }

    public void deleteManuallyAddedRecords(){
        //TODO delete all records of structures, which arrived in db by adding
        // them manually or just not by this program (delete all, which are not in oldFiles)
    }

    /**
     * Add single structure to batch, which will be send to database, to delete some records,
     * which represent old models, which didn't occur in this update.
     * @param structure structure with models, which should be saved in database.
     * @param pstmt PreparedStatement, which keep batch to be sent.
     * @throws SQLException if a database access error occurs or this method is called on a closed PreparedStatement.
     */
    private void addDeletionToBatch(PreparedStatement pstmt, Structure structure) throws SQLException {
        int[] models;
        models = structure.getModels();
        pstmt.setString(1, structure.getId());
        if((models == null) || (models.length == 0)){
            pstmt.setBoolean(2, true);
            //value -1 inside "NOT IN" doesn't matter because it's connected by OR with "true" value
            for(int i = 0; i < Structure.getMaxModelsNo(); i++)
                pstmt.setInt(i + 3, -1);
        }
        else{
            pstmt.setBoolean(2, false);
            for(int i = 0; i < models.length; i++)
                pstmt.setInt(i + 3, models[i]);
            //it's just for fulfill pstmt with repeating values. This values will be deleted by optimizer
            //before query will be executed
            for(int i = models.length; i < Structure.getMaxModelsNo(); i++)
                pstmt.setInt(i + 3, models[0]);
        }
        pstmt.addBatch();
    }

    /**
     * @return Query, to delete old models.
     */
    private String getDeleteQuery(){
        StringBuilder sqlQuery = new StringBuilder("DELETE FROM " + Database.getDbTableName() + " WHERE pdbid = ? AND " +
                "(? OR modelno NOT IN ( ");
        sqlQuery.append("?,".repeat(Math.max(0, Structure.getMaxModelsNo())));
        sqlQuery.deleteCharAt(sqlQuery.length() - 1);
        sqlQuery.append(" ) )");
        return sqlQuery.toString();
    }

    /**
     * Delete old structures from database (delete only models of structures, which have its
     * pdb id structures on list and don't have its models number in structures models list.
     * @param structures structures, which will stay in database.
     * @return amount of deleted rows.
     */
    private int deleteRecordsFromDB(ArrayList<Structure> structures){
        String sqlQuery = getDeleteQuery();
        int affectedRows = 0;
        int[] affectedRowsList;
        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            conn.setAutoCommit(false);
            for (Structure structure: structures)
                addDeletionToBatch(pstmt, structure);
            affectedRowsList = pstmt.executeBatch();
            for (int val : affectedRowsList)
                affectedRows += val;
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException ex) {
            handleSqlError(ex);
        }
        return affectedRows;
    }

    /**
     * Delete all old records from database, which occurred in last update,
     * but don't occur in present update.
     * @return amount of deleted rows.
     */
    public int deleteOldRecords(){
        int affectedrows = 0;
        DBDownloader.saveQueueToFile(getUpdatedFiles(), true, updatedStructuresPath);

        ArrayList<Structure> structures = new ArrayList<>(queryBatchSize);
        try(Scanner structuresReader = new Scanner(updatedStructuresPath.toFile())){
            while (structuresReader.hasNextLine()){
                structures.add(Structure.valueOf(structuresReader.nextLine()));
                if(structures.size() == queryBatchSize) {//to save memory  - can't load whole database at once
                    affectedrows += deleteRecordsFromDB(structures);
                    structures.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(!structures.isEmpty()) {
            affectedrows += deleteRecordsFromDB(structures);
            structures.clear();
        }

        return affectedrows;
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
