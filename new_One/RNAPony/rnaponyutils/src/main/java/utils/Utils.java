package utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

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
                e.printStackTrace();
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
    /*public static void main(String[] args){
        Utils util = new Utils();
        System.out.println(Path.of(".").toAbsolutePath());
        ClassLoader classLoader = homo.getClass().getClassLoader();
        for(Package p: classLoader.getDefinedPackages())
        homo.info();
        homo.compute(Path.of(classLoader.getResource("ur4_L1_0.txt").getPath()));
        //homo.compute(Path.of(".", "files", "ur4_L1_0.txt"));
    }*/
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
        return tokenMatcher.results().map(matchResult -> matchResult.group());
    }
}
