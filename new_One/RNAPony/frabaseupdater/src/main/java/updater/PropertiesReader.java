package updater;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import models.parameters.PreprocessType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

public class PropertiesReader {

    @Parameter(names = "--help", description = "Display description for all arguments.", help = true)
    private static boolean help = false;
    @Parameter(names = {"--config", "-c"}, description = "Path to .properties file with configuration.")
    private static String path = null;//Path.of(".", "user.properties").toString();
    private static Properties customProperties;
    private static Properties defaultProperties;

    /**
     * Load arguments given to the program
     *
     * @param args list of arguments
     */
    private static void loadArgs(String[] args) {
        JCommander jCommander = JCommander.newBuilder()
                .addObject(new Object[]{new PropertiesReader(), new Preprocessor()})
                .build();
        jCommander.parse(args);
        if (help) {
            jCommander.usage();
            System.exit(0);
        }
    }

    private static void downloadPropertyFile(){
        defaultProperties = new Properties();

        try {
            defaultProperties.load(PropertiesReader.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            Main.errLogger.severe("Unexpected problem with reading file:\n" + e.getMessage());
            System.exit(-1);
        }
        try{
            customProperties = new Properties(defaultProperties);
            if (path != null) {
                FileInputStream file = new FileInputStream(path);
                customProperties.load(file);
                file.close();
            }
        } catch (FileNotFoundException e) {
            Main.verboseInfo("Couldn't find " + path + " file. Using default values.", 1);
            path = null;
        } catch (IOException e) {
            Main.errLogger.severe("Unexpected problem with reading file:\n" + e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Read all properties from file and set
     * their values in program.
     */
    private static void loadPropertyFile(){
        downloadPropertyFile();
        for (Object key : defaultProperties.keySet())
            setProperty(key);
    }

    /**
     * Set single property.
     *
     * @param key key, which represent which property should be set up.
     *            Values are taken from .properties file.
     */
    private static void setProperty(Object key){
        String val;
        val = customProperties.getProperty(key.toString());
        switch (key.toString()) {
            case "preprocessType":
                try {
                    Preprocessor.setPreprocessType(PreprocessType.valueOf(val.toUpperCase()));
                }catch ( IllegalArgumentException e){
                    Main.verboseInfo("Incorrect type of preprocessing: " + val + ". Correct types are: " +
                                    Arrays.toString(PreprocessType.values()) + ". Setting up default type: " +
                                    defaultProperties.getProperty(key.toString()),
                            1,
                            Level.SEVERE);
                    Preprocessor.setPreprocessType(PreprocessType.valueOf(defaultProperties.getProperty(key.toString())));
                }
                break;
            case "dayOfUpdate":
                int day = Integer.parseInt(val);
                if(day >= 1 && day <= 7){
                    Main.dayOfWeek = day;
                } else{
                    Main.verboseInfo("Incorrect day given:" + val + ". Correct day values:" +
                                    "1 = Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday. " +
                                    "Setting up default day: " + defaultProperties.getProperty(key.toString()),
                            1,
                            Level.SEVERE);
                    Main.dayOfWeek = day;
                }
                break;
            case "hourOfUpdate":
                try {
                    Main.time = LocalTime.parse(val);
                }
                catch (DateTimeParseException e){
                    Main.verboseInfo("Incorrect format of time: " + val + ". Correct format is: hh:mm:ss. " +
                                    "Setting up default time: " + defaultProperties.getProperty(key.toString()),
                            1,
                            Level.SEVERE);
                    Main.time = LocalTime.parse(defaultProperties.getProperty(key.toString()));
                }
                break;
        }
    }

    /**
     * Load properties from file and from command line. Arguments from
     * command line has higher priority.
     * @param args arguments given to program.
     */
    public static void loadProperties(String[] args){
        Preprocessor.setLastPreprocessType(Preprocessor.getPreprocessType());
        loadArgs(args);
        loadPropertyFile();
        loadArgs(args);//second time, because properties from file overwritten this properties.
        Main.stdLogger.info("");
    }
}
