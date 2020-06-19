

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import maintokenizers.StringTokenizer;
import java.util.logging.*;

public class Homology {
    public final Logger logger;
    private boolean saveToFile = false;
    public Homology(){
        String s = "s";
        s.stripTrailing();
        logger = Logger.getLogger(Homology.class.getName());
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%5$s%n");
        logger.setUseParentHandlers(false);
        changeLogFile(Path.of("./", "default.txt"));
    }

    /**
     * Change file, where logs will be saving
     * @param filePath path to file, to write logs in
     */
    public void changeLogFile(Path filePath){
        for(Handler handler : logger.getHandlers()){//remove old handlers
            handler.close();
            logger.removeHandler(handler);
        }

        Handler handler = null;
        if(isSaveToFile()){
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

    public void bpSet(StringBuilder seq, StringBuilder dot){
        int l = dot.length();
    }

    public void homology(Path fileName){

        try(BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(fileName.toString())
                            ))){
            String line;
            line = reader.readLine();
            StringTokenizer tokenizer;
            tokenizer = new StringTokenizer(line, " ");
            System.out.printf("REFERENCE: %s\n",tokenizer.nextToken());
            bpSet(new StringBuilder(tokenizer.nextToken()), new StringBuilder(tokenizer.nextToken()));
            for(;(line = reader.readLine()) != null;){

                tokenizer = new StringTokenizer(line, " ");
                if(tokenizer.countTokens() == 7){
                    pdb = tokenizer.nextToken();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public void setSaveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
    }
}
