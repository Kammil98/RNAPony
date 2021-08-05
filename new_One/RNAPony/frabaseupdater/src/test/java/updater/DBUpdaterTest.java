package updater;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.SQLException;

class DBUpdaterTest {

    private static DBUpdater updater;

    @AfterAll
    public static void cleanUp(){
        updater.close();
    }

    @BeforeAll
    public static void init(){
        try {
            updater = new DBUpdater();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}