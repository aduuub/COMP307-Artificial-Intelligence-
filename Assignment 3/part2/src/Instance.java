/**
 * Created by Adam on 4/05/17.
 */
public class Instance {

    private int[] attributes;
    private int outcome;

    /**
     *
     * @param data
     * @param outcome
     */
    public Instance(int[] data, Integer outcome) {
        assert attributes.length == 12 : "Must have 11 attributes";
        this.attributes = data;
        this.outcome = outcome;
    }

    public int[] getAttributes() {
        return attributes;
    }

    public int getAttr(int index){
        return attributes[index];
    }

    public int getOutcome() {
        return outcome;
    }
}
