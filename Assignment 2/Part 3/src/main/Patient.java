package main;

/**
 * Created by Adam on 4/05/17.
 */
public class Patient {

    // Attributes
    private int[] attributes;

    // Other
    private int id;
    private int condition;


    public Patient(String[] attributes) {
        assert attributes.length == 11 : "Must have 11 attributes";

        this.attributes = new int[9];
        for(int i=1; i < 10; i++){
            this.attributes[i-1] = getInt(attributes[i]);
        }

        id = getInt(attributes[0]);
        condition = getInt(attributes[10]);
    }


    /**
     * Turns String <code>s</code> into an integer. If s == "?" it turns it into 1
     * @param s
     * @return
     */
    private static int getInt(String s) {
        int i;
        try {
            i = Integer.valueOf(s); // is a valid number
        } catch (NumberFormatException e) {
            i = -1; // must have been a ? mark- lets set it to have value 1 instead
        }
        return i;
    }


    public int[] getAttributes() {
        return attributes;
    }

    public int getCondition() {
        return condition;
    }
}
