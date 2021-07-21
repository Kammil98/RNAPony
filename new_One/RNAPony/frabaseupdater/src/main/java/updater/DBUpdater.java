package updater;

import models.Structure;
import models.DBrecord;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBUpdater implements Closeable {

    private final Connection conn;
    public static final ConcurrentLinkedQueue<Structure> updatedFiles = new ConcurrentLinkedQueue<>();
    public static final Path updatedStructuresPath = Main.frabaseDir.resolve("UpdatedStructures.txt");
    private final String table = "rnapony";
    public DBUpdater() throws SQLException {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/rnaponydb";
        String user = "rnaponyadmin";
        String pass = "rnapony";
        conn = DriverManager.getConnection(jdbcUrl, user, pass);
        Statement stmt = conn.createStatement();

        String CreateSql = "Create Table IF NOT EXISTS " + table + "(" +
                "id varchar(4), " +
                "modelNo int, " +
                "chain text NOT NULL, " +
                "resol numeric(5,2) NOT NULL," +
                "seq text NOT NULL," +
                "dot text NOT NULL," +
                "dotIntervals text NOT NULL," +
                "maxOrder int NOT NULL," +
                "PRIMARY KEY (id, modelNo) )";
        stmt.executeUpdate(CreateSql);
        stmt.close();
    }

    DBrecord readRecord(String line){
        DBrecord record;
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        record = new DBrecord(tokenizer.nextToken(),
                Integer.parseInt(tokenizer.nextToken()),
                tokenizer.nextToken(),
                Double.parseDouble(tokenizer.nextToken()),
                tokenizer.nextToken(),
                tokenizer.nextToken(),
                tokenizer.nextToken(),
                Integer.parseInt(tokenizer.nextToken()));
        return record;
    }

    Structure readStructure(String line){
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

    private void handleSqlError(SQLException e){
        while(e!= null){
            Main.verboseInfo("Couldn't execute sql command: \n" + e.getSQLState() + "\n" + e.getErrorCode() +
                    "\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()), 1);
            Main.errLogger.severe("Couldn't execute sql command: \n" + e.getSQLState() + "\n" + e.getErrorCode() +
                    "\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            e = e.getNextException();
        }
    }

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

    private int addRecordsToDB(ArrayList<DBrecord> records){
        int affectedRows = 0;
        int[] affectedRowsList;
        String batchCommand = "INSERT INTO " + table +
                "  (id, modelNo, chain, resol, seq, dot, dotIntervals, maxOrder) " +
                "VALUES  (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id, modelNo) DO UPDATE SET " +
                "chain=excluded.chain, " +
                "resol=excluded.resol, " +
                "seq=excluded.seq, " +
                "dot=excluded.dot, " +
                "dotIntervals=excluded.dotIntervals, " +
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

    public int addOrUpdateNewRecords(Path newRecordsPath){
        int affectedRows = 0;
        ArrayList<DBrecord> records = new ArrayList<>(1000);
        try(Scanner recordsReader = new Scanner(newRecordsPath.toFile())){
            while (recordsReader.hasNextLine()){
                records.add(readRecord(recordsReader.nextLine()));
                if(records.size() == 1000) {//to save memory  - can't load whole database at once
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

    private String getDeleteQuery(){
        StringBuilder sqlQuery = new StringBuilder("DELETE FROM " + table + " WHERE id = ? AND " +
                "(? OR modelNo NOT IN ( ");
        sqlQuery.append("?,".repeat(Math.max(0, Structure.getMaxModelsNo())));
        sqlQuery.deleteCharAt(sqlQuery.length() - 1);
        sqlQuery.append(" ) )");
        return sqlQuery.toString();
    }

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

    public int deleteOldRecords(){
        int affectedrows = 0;
        DBDownloader.saveQueueToFile(updatedFiles, true, updatedStructuresPath);

        ArrayList<Structure> structures = new ArrayList<>(1000);
        try(Scanner structuresReader = new Scanner(updatedStructuresPath.toFile())){
            while (structuresReader.hasNextLine()){
                structures.add(readStructure(structuresReader.nextLine()));
                if(structures.size() == 1000) {//to save memory  - can't load whole database at once
                    affectedrows += deleteRecordsFromDB(structures);
                    structures.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(!structures.isEmpty())
            affectedrows += deleteRecordsFromDB(structures);

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
