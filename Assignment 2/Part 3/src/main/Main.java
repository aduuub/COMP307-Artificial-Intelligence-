package main;

import com.sun.deploy.panel.ExceptionListDialog;
import com.sun.tools.corba.se.idl.InvalidArgument;
import org.jgap.FitnessFunction;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.DefaultGPFitnessEvaluator;
import org.jgap.gp.impl.DeltaGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Variable;
import org.jgap.util.SystemKit;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is the main entry point of the Regression algorithm. It loads the data, initialises a configuration and runs it.
 *
 * @author Adam Wareing
 */
public class Main {
    // Constants
    public static final Object[] NO_ARGUMENTS = new Object[0];
    public static final double MIN_ACCEPTABLE_ERROR = 0.02;
    public static final int MAX_EVOLUTIONS = 300;

    // Fields
    private MathsProblem problem;
    private GPConfiguration config;

    private final Parser parser;
    private final List<Patient> trainingPatients;
    private final List<Patient> testPatients;


    /**
     * Start the algorithm. It starts by loading in the data.
     * @param trainingFile
     * @param testFile
     * @param nameFile
     */
    private Main(String trainingFile, String testFile, String nameFile) {
        parser = new Parser(trainingFile,testFile, nameFile);
        trainingPatients = parser.getTrainingPatients();
        testPatients = parser.getTestPatients();
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
        config.setFitnessFunction(new PatientFitnessFunction(trainingPatients));

        // Set properties
        config.setMaxInitDepth(4);
        config.setPopulationSize(1000);
        config.setMaxCrossoverDepth(6);
        config.setStrictProgramCreation(true);
        config.setCrossoverProb(0.9f);
        config.setMutationProb(0.2f);
        config.setReproductionProb(0.05f);

        // Create variables and problem
        Variable[] vars = parser.createVariables(config);
        problem = new MathsProblem(config, vars);
    }


    /**
     * Evolve the problem and get the best solution
     *
     * @throws Exception
     */
    private void run() throws Exception {
        GPGenotype gp = problem.create();
        gp.setGPConfiguration(config);
        gp.setVerboseOutput(true);
        evolve(gp);

        // Print the best solution so far to the console.
        gp.outputSolution(gp.getAllTimeBest());
        // Create a graphical tree of the best solution's program and write it to a PNG file.
        problem.showTree(gp.getAllTimeBest(), "best-solution.png");

        // test
        testAlgorithm(gp);
    }


    /**
     * Evolves the program. This way the program evolves until the accuracy of the best program is less than
     * <code>MIN_ACCEPTABLE_ERROR</code>. It also prints the output of its performance every 25 evolutions
     *
     * @param program
     */
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
     * Run the algorithm on the test data and see the performance
     * @param gp
     */
    private void testAlgorithm(GPGenotype gp) {
        // Training accuracy
        PatientFitnessFunction fitnessFunction = new PatientFitnessFunction(trainingPatients);
        double result = fitnessFunction.evaluate(gp.getAllTimeBest()) * 100; // convert incorrect to percentage
        result = 100 - result; // percentage correct
        System.out.println("\nPercentage of training instances correctly classified: " + String.format( "%.4f", result) + "%");

        // Test accuracy
        fitnessFunction = new PatientFitnessFunction(testPatients);
        result = fitnessFunction.evaluate(gp.getAllTimeBest()) * 100; // convert incorrect to percentage
        result = 100 - result; // percentage correct
        System.out.println("\nPercentage of test instances correctly classified: " + String.format( "%.4f", result) + "%");
    }


    /**
     * The function that evaluates the fitness of a program
     */
    public class PatientFitnessFunction extends GPFitnessFunction {

        private List<Patient> instances;

        public PatientFitnessFunction(List<Patient> instances){
            this.instances = instances;
        }

        @Override
        protected double evaluate(IGPProgram igpProgram) {
            double correct = 0;

            // Evaluate the program for all inputs
            for (Patient patient : instances) {

                // Set the variables to be the patients properties
                problem.setVariablesOfPatient(patient);

                // See how it performs and add its error to the total error
                double result = igpProgram.execute_double(0, NO_ARGUMENTS);

                int predictedClass;
                if (result < 0) {
                    predictedClass = 2;
                } else {
                    predictedClass = 4;
                }

                if (predictedClass == patient.getCondition()) {
                    correct++;
                }
            }

            if (correct < MIN_ACCEPTABLE_ERROR) {
                correct = 0.0;
            }
            return correct / instances.size();
        }
    }


    /**
     * Entry point of the program
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            System.out.println("Invalid program arguments");
            System.out.println("Usage: trainingFile testFile nameFile");
        }
        // Create application and run
        Main main = new Main(args[0], args[1], args[2]);
        main.initConfig();
        main.run();
    }
}
