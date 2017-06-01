import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads in the files
 * @author Adam
 */
public class Parser {

    private String trainingFileName;
    private String testFileName;

    private List<Instance> training;
    private List<Instance> test;

    /**
     * Parse the training and test data
     *
     * @param trainingFileName
     * @param testFileName
     */
    public Parser(String trainingFileName, String testFileName) {
        this.trainingFileName = trainingFileName;
        this.testFileName = testFileName;
        System.out.println("Loading training files: " + trainingFileName + " and " + testFileName);
        training = parseFile(true);
        test = parseFile(false);
        System.out.println("");
    }


    /**
     * Parses one file in
     * @param isTrainingData
     * @return all instances in the file
     */
    private List<Instance> parseFile(boolean isTrainingData) {
        List<Instance> instances = new ArrayList<>();
        Scanner scan;
        String fileName = isTrainingData ? trainingFileName : testFileName;

        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)));
        } catch (NullPointerException e) {
            throw new RuntimeException("Invalid file specified");
        }

        // Read in data
        while (scan.hasNextInt()) {
            int[] data = new int[12];

            for (int i = 0; i < 12; i++) {
                data[i] = scan.nextInt();
            }

            Integer outcome = isTrainingData ? scan.nextInt() : Integer.MAX_VALUE;
            instances.add(new Instance(data, outcome));
        }

        String type = isTrainingData ? "training" : "test";
        System.out.println("Loaded " + instances.size() + " instances of " + type +  " data.");
        return instances;
    }



    public List<Instance> getTraining() {
        return training;
    }

    public List<Instance> getTest() {
        return test;
    }
}
