package cse;

import java.nio.file.Path;
import java.util.Objects;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import csemodels.parameters.ComputationType;
import csemodels.parameters.converters.ComputationTypeConverter;
import homology.Homology;
import utils.Computable;

public class Main {

    @Parameter(names = {"--path", "-p"}, description = "Path to input file.", required = true)
    private static String inputPath = null;

    @Parameter(names = {"--output", "-o"}, description = "Path to file, where output will be printed. " +
            "Default results are printed to standard output")
    private static String outputPath = null;

    @Parameter(names = {"--database", "-d"}, description = "Path to file with database. " +
            "Default database is taken fromm inner resource of this jar.")
    private static String dbPath = Objects.requireNonNull(Main.class.getResource("/cse.txt")).getPath();

    @Parameter(names = {"--type", "-t"}, converter = ComputationTypeConverter.class,
            description = "Type of computation, which we would like to start. " +
            "Possible values are: loop, hairpin and homology.", required = true)
    private static ComputationType computeType = null;

    @Parameter(names = {"--insertion", "-i"}, description = "Insertion value - parameter for computation " +
            "of type loop and hairpin. This parameter control (but not necessary directly) number " +
            "of iterations of program.")
    private static int insertion = 0;

    @Parameter(names = {"--loop", "-l"}, description = "Insertion value - parameter for computation " +
            "of type loop. Possible values are: true, false.")
    private static boolean openLoop = false;

    @Parameter(names = "--help", description = "Display description for all arguments.", help = true)
    private static boolean help = false;

    /**
     * Load arguments given to the program.
     *
     * @param args list of arguments
     */
    private static void loadArgs(String[] args) {
        JCommander jCommander = JCommander.newBuilder()
                .addObject(new Main())
                .build();
        jCommander.parse(args);
        if (help) {
            jCommander.usage();
            System.exit(0);
        }
    }

    /**
     * Return Computable object based on computation, which user want to make.
     *
     * @return Computable, which is able to process desired file.
     */
    private static Computable getComputable(){
        switch (computeType){
            case LOOP:
                return new Loop(dbPath, insertion, openLoop);
            case HAIRPIN:
                return new Hairpin(dbPath, insertion);
            case HOMOLOGY:
                return new Homology();
            default:
                throw new UnsupportedOperationException("Unknown type of computation");
        }
    }

    public static void main(String[] args){
        loadArgs(args);
        Computable computable = getComputable();
        if (outputPath != null)
            computable.changeLogFile(Path.of(outputPath));
        computable.compute(inputPath);
        /*Hairpin hairpin = new Hairpin("cse.txt", 0);
        hairpin.compute("hairpin.dot");
        Loop loop = new Loop("cse.txt", 0, false);
        //loop.compute("ur4_L2.dot");
        loop.compute("ur4_L1.dot");
        loop.changeLogFile(Path.of("plik.txt"));
        loop.compute("ur4_L1.dot");
        loop.changeLogFile(null);
        loop.compute("ur4_L1.dot");
        Homology homology = new Homology();
        homology.compute("ur4_L1_0.txt");*/
    }
}
