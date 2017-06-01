import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.DeltaGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Variable;
import org.jgap.util.SystemKit;

import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * This is the main entry point of the Regression algorithm. It loads the data, initialises a configuration and runs it.
 *
 * @author Adam Wareing
 */
public class Main {
    // Constants
    public static final Object[] NO_ARGUMENTS = new Object[0];
    public static final double MIN_ACCEPTABLE_ERROR = 0.001;
    public static final int MAX_EVOLUTIONS = 1000;
    public static final int INPUT_SIZE = 20;

    // Fields
    private double x[];
    private double y[];
    private Variable vx;
    private MathsProblem problem;
    private GPConfiguration config;

    /**
     * Start the algorithm. It starts by loading in the data.
     *
     * @param file
     */
    private Main(String file) {
        load(file);
    }


    /**
     * Reads in the data from the file and populate <code>x[]</code> and <code>y[]</code>
     *
     * @param file
     * @return size of the inputs
     */
    private void load(String file) {
        // Init
        x = new double[INPUT_SIZE];
        y = new double[INPUT_SIZE];
        Scanner scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file)));

        // Skip heading
        scan.nextLine(); // x y heading
        scan.nextLine(); // -----------

        // Read in data
        for (int i = 0; scan.hasNextDouble(); i++) {
            x[i] = scan.nextDouble();
            y[i] = scan.nextDouble();
        }
    }


    /**
     * Initialise the <code>config</code> used for the program and creates a <code>problem</code>
     *
     * @throws Exception
     */
    private void initConfig() throws Exception {
        // Create configuration
        config = new GPConfiguration();
        // We use a delta fitness evaluator because we compute a defect rate, not a point score!
        config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        // Set fitness function
        config.setFitnessFunction(new MathsFitnessFunction());

        // Set properties
        config.setMaxInitDepth(4);
        config.setPopulationSize(1000);
        config.setMaxCrossoverDepth(8);

        config.setStrictProgramCreation(true);
        config.setCrossoverProb(0.9f);
        config.setMutationProb(35.0f);
        config.setReproductionProb(0.2f);

        // Create variable
        vx = Variable.create(config, "X", CommandGene.DoubleClass);

        // Create problem
        problem = new MathsProblem(config, vx);
    }


    /**
     * Evolve the problem and get the best solution
     *
     * @throws Exception
     */
    private void run() throws Exception {
        // Create problem, set config and set verbose output on
        GPGenotype gp = problem.create();
        gp.setGPConfiguration(config);
        gp.setVerboseOutput(true);

        // Evolve
        evolve(gp);

        // Print the best solution so far to the console.
        gp.outputSolution(gp.getAllTimeBest());

        // Create a graphical tree of the best solution's program and write it to a PNG file.
        problem.showTree(gp.getAllTimeBest(), "best-solution.png");
    }


    private void evolve(GPGenotype program) {
        int offset = program.getGPConfiguration().getGenerationNr();

        int i;
        for (i = 0; i < MAX_EVOLUTIONS; ++i) {
            program.evolve();
            program.calcFitness();
            double fitness = program.getAllTimeBest().getFitnessValue();

            if (fitness < MIN_ACCEPTABLE_ERROR) {
                break;
            }

            // Print output every 25 evolutions
            if (i % 25 == 0) {
                String freeMB = SystemKit.niceMemory(SystemKit.getFreeMemoryMB());
                System.out.println("Evolving generation " + (i + offset) + ", memory free: " + freeMB + " MB, " +
                        "Fittest program: " + fitness);
            }
        }
        System.out.println();
        System.out.println("After " + i + " evolutions the program had a fitness of: " + program.getAllTimeBest().getFitnessValue());
    }


    /**
     * The function that evaluates the fitness of a program
     */
    public class MathsFitnessFunction extends GPFitnessFunction {

        @Override
        protected double evaluate(IGPProgram igpProgram) {
            double totalError = 0;

            // Evaluate the program for all inputs
            for (int i = 0; i < Main.INPUT_SIZE; i++) {
                // Provide the variable vx with the input number.
                vx.set(x[i]);

                // See how it performs and add its error to the total error
                double result = igpProgram.execute_double(0, NO_ARGUMENTS);
                totalError += Math.abs(result - y[i]);

                if (Double.isInfinite(totalError)) {
                    return Double.MAX_VALUE;
                }
            }

            if (totalError < MIN_ACCEPTABLE_ERROR) {
                return 0;
            }

            // Return the result
            return totalError;
        }
    }


    /**
     * Main entry point of the program
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Check valid usage
        if (args.length != 1) {
            System.out.println("Invalid usage. \nArguments: Filename");
            return;
        }

        // Create application and run
        Main main = new Main(args[0]);
        main.initConfig();
        main.run();
    }
}
