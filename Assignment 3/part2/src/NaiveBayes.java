import java.util.List;

/**
 * @author Adam
 */
public class NaiveBayes {

    private List<Instance> instances;
    private int featureSize; // How many attributes each instance has

    // Total number of spam/ not spam
    private int numberOfSpam;
    private int numOfNotSpam;

    private int[] spamTrue; // Freq of instances that are classified as spam and have true values at that index
    private int[] spamFalse; // Freq of instances that are classified as spam and have false values at that index

    private int[] notSpamTrue; // Freq of instances that are classified as not spam and have true values at that index
    private int[] notSpamFalse; // Freq of instances that are classified as not spam and have false values at that index


    /**
     * Construct a new classifier
     *
     * @param training    - training data
     * @param featureSize - how many features each Instance has
     */
    public NaiveBayes(List<Instance> training, int featureSize) {
        this.instances = training;
        this.featureSize = featureSize;
        learn();
    }


    /**
     * Learns the instances in the training set. Sets:
     * - num of spam/ not spam
     * - the four arrays of attribute values
     */
    private void learn() {
        spamTrue = new int[featureSize];
        spamFalse = new int[featureSize];
        notSpamTrue = new int[featureSize];
        notSpamFalse = new int[featureSize];

        numberOfSpam = 0;
        numOfNotSpam = 0;
        for (Instance f : instances) {
            // Determine if it is spam or not
            if (f.getOutcome() == 1) {
                numberOfSpam++;
            } else {
                numOfNotSpam++;
            }

            // For each feature
            for (int i = 0; i < featureSize; i++) {
                if (f.getOutcome() == 1) {
                    // Spam
                    if (f.getAttr(i) == 1) {
                        spamTrue[i]++;
                    } else {
                        spamFalse[i]++;
                    }
                } else {
                    // Not spam
                    if (f.getAttr(i) == 1) {
                        notSpamTrue[i]++;
                    } else {
                        notSpamFalse[i]++;
                    }
                }
            }
        }
    }


    /**
     * Classifies one instance as spam/ not spam.
     * P(C | D) = (P(C) * P(D | C)) / P(D)
     *
     * @param instance
     * @return - true if spam
     */
    public boolean classify(Instance instance) {
        // Calc probability of spam: P(C)
        double probabilitySpam = (double) numberOfSpam / (double) (numberOfSpam + numOfNotSpam);
        double probabilityNotSpam = (double) numOfNotSpam / (double) (numberOfSpam + numOfNotSpam);

        // Calculate the probability of the attributes given the class: P(D | C)
        for (int i = 0; i < instance.getAttributes().length; i++) {
            if (instance.getAttr(i) == 1) {
                // Attribute true
                probabilitySpam *= (double) spamTrue[i] / (double) (spamTrue[i] + spamFalse[i]);
                probabilityNotSpam *= (double) notSpamTrue[i] / (double) (notSpamTrue[i] + notSpamFalse[i]);
            } else {
                // Attribute false
                probabilitySpam *= (double) spamFalse[i] / (double) (spamTrue[i] + spamFalse[i]);
                probabilityNotSpam *= (double) notSpamFalse[i] / (double) (notSpamTrue[i] + notSpamFalse[i]);
            }
        }

        // Normalise the data by dividing by P(D)
        double normaliser = probabilityNotSpam + probabilitySpam;
        probabilitySpam /= normaliser;
        probabilityNotSpam /= normaliser;

        // Determine if likely spam and print result
        boolean likelySpam = probabilitySpam > probabilityNotSpam;
        String spamOrNot = likelySpam ? "Spam" : "Not Spam";
        System.out.printf("Probability Spam: %3.3f%%, Probability Not spam: %3.3f%%. Probably: %s.\n", probabilitySpam * 100, probabilityNotSpam * 100, spamOrNot);
        return likelySpam;
    }


    /**
     * Prints the values of the features which were generated from training
     */
    private void printOutputOfFeatures() {
        System.out.println("\n-----   Printing features values from the training data    -----\n");
        System.out.printf("                      Spam   Not Spam\n");
        for (int i = 0; i < featureSize; i++) {
            System.out.printf("Feature %2d (True) :   %d/%d  %d/%d \n", i + 1, spamTrue[i], spamTrue[i] + spamFalse[i], notSpamTrue[i], notSpamTrue[i] + notSpamFalse[i]);
            System.out.printf("Feature %2d (False):   %d/%d  %d/%d \n", i + 1, spamFalse[i], spamFalse[i] + spamTrue[i], notSpamFalse[i], notSpamFalse[i] + notSpamTrue[i]);
        }
        System.out.println("\n-----   Finished printing features values    -----\n");
    }


    /**
     * Classifies a list of instances
     *
     * @param instances
     */
    public void classify(List<Instance> instances) {
        if (instances.get(0).getOutcome() != Integer.MAX_VALUE) {
            // Training data - we can check the result
            int correct = 0;

            for (Instance instance : instances) {
                boolean isSpam = classify(instance);
                if ((isSpam && instance.getOutcome() == 1) || (!isSpam && instance.getOutcome() == 0))
                    correct++;
            }
            System.out.printf("\nI got %d correct out of %d. That is %.2f%% correct.\n", correct, instances.size(), (double) correct * 100 / instances.size());

        } else {
            // Test data - run normally
            instances.forEach(this::classify);
        }
    }


    /**
     * Entry point of the program
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid program arguments");
            System.out.println("Usage: trainingFile testFile");
            return;
        }

        // Parse data
        Parser parser = new Parser(args[0], args[1]);
        List<Instance> training = parser.getTraining();
        List<Instance> test = parser.getTest();

        // Create application and run
        NaiveBayes c = new NaiveBayes(training, 12);

        System.out.println("-----   Training Data   -----\n");
        c.classify(training);
        System.out.println("\n-----   Finished Running Training Data   -----");

        c.printOutputOfFeatures();

        System.out.println("-----   Test Data   -----\n");
        c.classify(test);
        System.out.println("\n-----   Finished Running Test Data   -----");
    }
}
