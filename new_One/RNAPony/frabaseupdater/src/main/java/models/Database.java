package models;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;


public class Database {
    //Commented values are for testing environment and don't suit production environment
    @Parameter(names = {"--host", "-h"}, description = "Adress of host with PostgreSQL database.")
    @Getter
    @Setter
    private static String dbHost;
    @Parameter(names = {"--port", "-p"}, description = "Port with exposed PostgreSQL database.")
    @Getter
    @Setter
    private static String dbPort;
    @Parameter(names = {"--user", "-U"}, description = "User of PostgreSQL database.")
    @Getter
    @Setter
    private static String dbUser;
    @Parameter(names = {"--passwd", "-P"}, description = "Password of user of PostgreSQL database.")
    @Getter
    @Setter
    private static String dbUserPasswd;
    @Parameter(names = {"--DBName", "-n"}, description = "Name of database.")
    @Getter
    @Setter
    private static String dbName;
    @Parameter(names = {"--table", "-N"}, description = "Name of databases table.")
    @Getter
    @Setter
    private static String dbTableName;
}
