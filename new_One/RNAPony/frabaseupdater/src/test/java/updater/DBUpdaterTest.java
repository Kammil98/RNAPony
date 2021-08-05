package updater;

import models.DBrecord;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DBUpdaterTest {

    private static DBUpdater updater;
    private static final String tableName = "testTable";
    @AfterAll
    public static void cleanUp(){
        updater.close();
    }

    @BeforeAll
    public static void init(){
        try {
            updater = new DBUpdater(tableName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown(){
        try (Statement stmt = updater.getConn().createStatement()){
            stmt.execute("TRUNCATE TABLE " + tableName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Returns sorted list of records from given file
     * @param path pth to file with records
     * @return sorted list of records
     */
    private ArrayList<DBrecord> readRecords(Path path){
        ArrayList<DBrecord> records = new ArrayList<>(20);
        try(Scanner recordsReader = new Scanner(path.toFile())){
            while (recordsReader.hasNextLine()){
                records.add(DBrecord.valueOf(recordsReader.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        records.sort((o1, o2) -> {
            if (o1.getId().compareTo(o2.getId()) != 0)
                return o1.getId().compareTo(o2.getId());
            if (o1.getModelNo() != o2.getModelNo())
                return o1.getModelNo() - o2.getModelNo();
            return 0;
        });
        return records;
    }

    private void checkRecordsEquality(ArrayList<DBrecord> records){
        int i = 0;
        DBrecord record, correctRec;
        try (Statement stmt = updater.getConn().createStatement()){
            ResultSet rs = stmt.executeQuery("Select * from " + tableName + " order by id, modelno");
            while(rs.next()){
                record = new DBrecord(rs.getString(1), rs.getInt(2),
                        rs.getString(3), rs.getDouble(4), rs.getString(5),
                        rs.getString(6), rs.getString(7), rs.getInt(8));
                correctRec = records.get(i);
                assertEquals(correctRec, record);
                i++;
            }
            assertEquals(records.size(), i);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    void addOrUpdateNewRecords() {
        Path path = Path.of(Objects.requireNonNull(getClass().getResource("/DBUpdater_test_DBrecords_1.txt"))
                .getPath());
        DBrecord record;
        ArrayList<DBrecord> records = readRecords(path);
        updater.addOrUpdateNewRecords(path);
        checkRecordsEquality(records);

        //Updating rows and add some new
        records.remove(15);
        records.remove(14);
        records.remove(13);
        path = Path.of(Objects.requireNonNull(getClass().getResource("/DBUpdater_test_DBrecords_2.txt"))
                .getPath());
        records.addAll(readRecords(path));
        updater.addOrUpdateNewRecords(path);
        checkRecordsEquality(records);
    }
}