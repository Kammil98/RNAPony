package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {

    public static final Logger errLogger = Logger.getLogger(Utils.class.getName() + "err");
    public static final Logger stdLogger = Logger.getLogger(Utils.class.getName() + "std");

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        Utils.errLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(errLogger);
        Utils.stdLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(stdLogger);
    }

    /**
     * Utils constructor
     */
    private Utils(){}
    /**
     * Change file, for logger, where logs will be saving.
     * Delete all old loggers directories
     * @param logger logger, which will receive new handler
     * @param filePath path to file, to write logs in. Change to Console logger, if path == null
     */
    public static void changeLogHandler(Logger logger, Path filePath){
        for(Handler handler : logger.getHandlers()){//remove old handlers
            handler.close();
            logger.removeHandler(handler);
        }
        Handler handler = null;
        if(filePath != null){
            try {
                handler = new FileHandler(filePath.toString(), false);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
                System.exit(-1);
            }
        }
        else
            handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    /**
     * Change logger handler, to log to console.
     * Delete all old loggers directories
     * @param logger logger, which will receive new handler
     */
    public static void changeLogHandler(Logger logger){Utils.changeLogHandler(logger, null);}

    /**
     * Find group of tokens and save it to ArrayList of Strings
     * @param source String with tokens divided by Separators
     * @param separators string of characters, which divide source string into tokens
     * @return ArrayList of All elements from source
     */
    public static ArrayList<String> createArray(String source, String separators){
        return Utils.getStreamOfTokens(source, separators)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Find group of tokens and save it to ArrayList of Integers
     * @param source String with integer tokens divided by Separators
     * @param separators string of characters, which divide source string into tokens
     * @return ArrayList of All elements from source
     */
    public static ArrayList<Integer> createArrayInt(String source, String separators){
        return Utils.getStreamOfTokens(source, separators)
                .map(Integer::valueOf).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Extract tokens from String and return it as a stream
     * @param text text with tokens to extract
     * @param delims possible delimiters in text with tokens
     * @return Stream of tokens in String Objects
     */
    public static Stream<String> getStreamOfTokens(String text, String delims){
        Pattern delimsPattern = Pattern.compile("[^" + delims + "]+");
        Matcher tokenMatcher = delimsPattern.matcher(text);
        return tokenMatcher.results().map(MatchResult::group);
    }

    /**
     * Execute given command in blocking mode.
     * @param command Command to execute.
     * @param displayInputInRealTime whether to display or not data printed to standard output from executed process,
     *                              while it's still working.
     * @param stdLogger standard logger to print message, when command return status != 0,
     *                  which indicates abnormal termination.
     * @param errLogger error logger to print message, when command return status != 0,
     *                  which indicates abnormal termination.
     */
    public static InputStream execCommand(String command, boolean displayInputInRealTime, Logger stdLogger, Logger errLogger){
        Process proc;
        InputStream err, std;
        try {
            proc = Runtime.getRuntime().exec(command);
            err = proc.getErrorStream();
            std = proc.getInputStream();
            while (displayInputInRealTime && proc.isAlive()){
                stdLogger.info(new String(std.readAllBytes(), StandardCharsets.UTF_8));
                Thread.sleep(1000);
            }
            proc.waitFor();
            if(proc.exitValue() != 0) {
                stdLogger.severe("Couldn't exec comand:\n" + command);
                errLogger.severe(new String(err.readAllBytes(), StandardCharsets.UTF_8));
                System.exit(-1);
            }
            return proc.getInputStream();
        } catch (IOException e) {
            errLogger.severe("Unexpected IOException: \n" + e.getMessage());
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Execute given command in blocking mode.
     * @param command Command to execute.
     * @param stdLogger standard logger to print message, when command return status != 0,
     *                  which indicates abnormal termination.
     * @param errLogger error logger to print message, when command return status != 0,
     *                  which indicates abnormal termination.
     */
    public static InputStream execCommand(String command, Logger stdLogger, Logger errLogger){
        return execCommand(command, false, stdLogger, errLogger);
    }

    /**
     * Check if directory exists and create, if it isn't,
     * with all necessary parent directories, which also don't exist.
     * If it's not possible, then message to stdLogger is printed.
     * @param outDir directory, which existence will be checked .
     * @param exitOnFail if this is equal true, then system will write msg also to errLogger
     *                   and will kill JVM with status -1.
     * @param stdLogger standard logger to print message, when creating directory isn't possible.
     * @param errLogger error logger to print message, when creating directory isn't possible.
     * @return true if directory exist or was successfully created. False otherwise.
     */
    public static boolean createDirIfNotExist(File outDir, boolean exitOnFail, Logger stdLogger, Logger errLogger){
        if(!outDir.exists() && !outDir.mkdirs()) {
            stdLogger.severe("Couldn't create directory:\n" + outDir.getAbsolutePath());
            if(exitOnFail) {
                errLogger.severe("Couldn't create directory:\n" + outDir.getAbsolutePath());
                System.exit(-1);
            }
            return false;
        }
        return true;
    }

    /**
     * Check if directory exists and create, if it isn't,
     * with all necessary parent directories, which also don't exist.
     * If it's not possible, then message to stdLogger is printed.
     * @param outDir directory, which existence will be checked .
     * @param stdLogger standard logger to print message, when creating directory isn't possible.
     * @return true if directory exist or was successfully created. False otherwise.
     */
    public static boolean createDirIfNotExist(File outDir, Logger stdLogger){
        return createDirIfNotExist(outDir, false, stdLogger, Utils.errLogger);
    }

    /**
     * Check if directory exists and create, if it isn't,
     * with all necessary parent directories, which also don't exist.
     * If it's not possible, then message is logged by Utils.stdLogger.
     * @param outDir directory, which existence will be checked .
     * @return true if directory exist or was successfully created. False otherwise.
     */
    public static boolean createDirIfNotExist(File outDir){
        return createDirIfNotExist(outDir, false, Utils.stdLogger, Utils.errLogger);
    }

}
