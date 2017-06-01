package main;

import org.jgap.gp.CommandGene;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.terminal.Variable;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Adam on 7/05/17.
 */
public class Parser {

    private String trainingFileName;
    private String testFileName;
    private String attrNamesFileName;

    private List<Patient> trainingPatients;
    private List<Patient> testPatients;

    public Parser(String trainingFileName, String testFileName, String attrNamesFileName) {
        this.trainingFileName = trainingFileName;
        this.testFileName = testFileName;
        this.attrNamesFileName = attrNamesFileName;
        load();
    }


    /**
     * Reads in the data from the file
     */
    private void load() {
        // Init
       trainingPatients = new ArrayList<>();
       testPatients = new ArrayList<>();

        // LOAD TRAINING DATA

        Scanner scan;
        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(trainingFileName)));
        }catch (NullPointerException e){
            System.out.println("Invalid file specified");
            e.printStackTrace();
            return;
        }

        // Read in data
        for (String line = scan.nextLine(); scan.hasNextLine(); line = scan.nextLine()) {
            String[] data = line.split(",");
            trainingPatients.add(new Patient(data));
        }


        // LOAD TEST DATA

        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(testFileName)));
        }catch (NullPointerException e){
            System.out.println("Invalid file specified");
            e.printStackTrace();
            return;
        }

        // Read in data
        for (String line = scan.nextLine(); scan.hasNextLine(); line = scan.nextLine()) {
            String[] data = line.split(",");
            testPatients.add(new Patient(data));
        }
    }


    /**
     * Create the variables from the <code>attrNamesFileName</code> file
     * @param config
     * @return
     * @throws Exception
     */
    public Variable[] createVariables(GPConfiguration config) throws Exception {
        // Init
        Variable[] variables = new Variable[9];

        Scanner scan;
        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(attrNamesFileName)));
        }catch (NullPointerException e){
            throw new RuntimeException("Invalid file specified");
        }

        System.out.println("Loading variables:");

        // Read in data
        for (int i=0; i < 9; i++) {
            String name = scan.nextLine();
            System.out.println(name);
            variables[i] = new Variable(config, name, CommandGene.DoubleClass);
        }

        System.out.println("---     Finished loading variables      ---");
        return variables;
    }

    public List<Patient> getTrainingPatients() {
        return trainingPatients;
    }

    public List<Patient> getTestPatients() {
        return testPatients;
    }
}
