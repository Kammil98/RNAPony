package updater;

import models.CifFile;
import models.DBrecord;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBUpdater implements Closeable {

    private final Connection conn;
    public static final ConcurrentLinkedQueue<CifFile> updatedFiles = new ConcurrentLinkedQueue<>();
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

    private DBrecord readRecord(String line){
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

    private void addRecordsToDB(ArrayList<DBrecord> records){
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
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException throwables) {
            handleSqlError(throwables);
        }
    }

    public void addOrUpdateNewRecords(Path newRecordsPath){
        ArrayList<DBrecord> records = new ArrayList<>(100);
        try(Scanner recordsReader = new Scanner(newRecordsPath.toFile())){
            while (recordsReader.hasNextLine()){
                records.add(readRecord(recordsReader.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addRecordsToDB(records);
    }

    private ArrayList<CifFile> getArrayOfModelsToDelete(ResultSet structures) throws SQLException {
        ArrayList<CifFile> modelsToDelete = new ArrayList<>();
        String id;
        Array modelsNoHndlr;
        HashSet<Integer> modelsNo;
        while (structures.next()){
            id = structures.getString(1);
            modelsNoHndlr = structures.getArray(2);
            modelsNo = new HashSet<>(Arrays.asList((Integer[]) modelsNoHndlr.getArray()));
            for(CifFile newStructure: updatedFiles){
                if(newStructure.getId().equals(id)){
                    modelsNo.removeAll(Set.of(newStructure.getModels()));
                    modelsToDelete.add(new CifFile(id, modelsNo.toArray(new Integer[]{})));
                    break;
                }
            }
        }
        return modelsToDelete;
    }

    private  ArrayList<CifFile> getModelsToDelete(){
        ArrayList<CifFile> modelsToDelete = null;
        StringBuilder sqlQuerry = new StringBuilder("SELECT id, array_agg(modelNo) AS models FROM " + table +
                " where id IN ( ");
        ResultSet resultSet;
        for(CifFile file: updatedFiles)
            sqlQuerry.append("'").append(file.getId()).append("',");
        sqlQuerry.deleteCharAt(sqlQuerry.length() - 1);
        sqlQuerry.append(" ) GROUP BY id");
        try(Statement stmt = conn.createStatement()) {
            resultSet = stmt.executeQuery(sqlQuerry.toString());
            modelsToDelete = getArrayOfModelsToDelete(resultSet);
        } catch (SQLException throwables) {
            handleSqlError(throwables);
        }
        return modelsToDelete;
    }

    public int deleteOldRecords(){
        String sqlCommand2 = "DELETE FROM " + table + " WHERE id = ? AND modelNo = ?";
        int affectedrows = 0;
        ArrayList<CifFile> modelsToDelete = getModelsToDelete();
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCommand2)) {
            conn.setAutoCommit(false);
            for(CifFile structure: modelsToDelete){
                for(Integer modelNo: structure.getModels()){
                    pstmt.setString(1, structure.getId());
                    pstmt.setInt(2, modelNo);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException ex) {
            handleSqlError(ex);
        }
        return affectedrows;
    }

    public void updateRecords(ArrayList<DBrecord> records){
        String batchCommand = "UPDATE " + table + " SET chain = ?, resol = ?, seq = ?, " +
                "dot = ?, dotIntervals = ?, maxOrder = ? WHERE id = ? AND modelNo = ?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(batchCommand)){
            conn.setAutoCommit(false);
            records.forEach(record ->{
                try {
                    preparedStatement.setString(1, record.getChain());
                    preparedStatement.setDouble(2, record.getResol());
                    preparedStatement.setString(3, record.getSeq());
                    preparedStatement.setString(4, record.getSeq());
                    preparedStatement.setString(5, record.getDotIntervals());
                    preparedStatement.setInt(6, record.getMaxOrder());
                    preparedStatement.setString(7, record.getId());
                    preparedStatement.setInt(8, record.getModelNo());
                    preparedStatement.addBatch();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println(Arrays.toString(updateCounts));
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
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
