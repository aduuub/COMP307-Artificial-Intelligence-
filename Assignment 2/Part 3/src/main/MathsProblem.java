package main;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPProblem;
import org.jgap.gp.function.*;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.Variable;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;


/**
 * This is the Problem for the GP algorithm. It stores a configuration and variable and creates a random initial
 * <code>GPGenotype</code>
 */
public class MathsProblem extends GPProblem {

    private GPConfiguration config;
    private Variable[] variables;


    /**
     * Creates a new Maths problem
     *
     * @param config - configuration to use for the problem
     * @param variables - terminal variables
     * @throws InvalidConfigurationException
     */
    public MathsProblem(GPConfiguration config, Variable[] variables) throws InvalidConfigurationException {
        super(config);
        this.config = config;
        this.variables = variables;
    }


    @Override
    public GPGenotype create() throws InvalidConfigurationException {
        Class[] types = {CommandGene.DoubleClass};
        Class[][] argTypes = {{},};

        // Function variables
        CommandGene[] mathsCommands = {
                new Multiply(config, CommandGene.DoubleClass),
                new Divide(config, CommandGene.DoubleClass),
                new Subtract(config, CommandGene.DoubleClass),
                new Add(config, CommandGene.DoubleClass),
                new Terminal(config, CommandGene.DoubleClass, -1.0d, 10.0d, true),

        };

        // Make all commands (variables + maths functions)
        CommandGene[] allCommandGenes = new CommandGene[mathsCommands.length + variables.length];
        // Add variables
        for(int i = 0; i < variables.length; i++){
            allCommandGenes[i] = variables[i];
        }
        // Add maths functions
        for (int i = variables.length; i < allCommandGenes.length; i++) {
            allCommandGenes[i] = mathsCommands[i-variables.length];
        }

        // Make node set
        CommandGene[][] nodeSets = new CommandGene[2][allCommandGenes.length];
        nodeSets[0] = allCommandGenes;
        nodeSets[1] = new CommandGene[0];

        return GPGenotype.randomInitialGenotype(config, types, argTypes, nodeSets, 20, true);
    }


    /**
     * Sets the variables of the problem to reflect the patients attributes
     * @param patient
     */
    public void setVariablesOfPatient(Patient patient){
        // Get patient variables
        int[] patientAttributes = patient.getAttributes();
        assert patientAttributes.length == variables.length;

        // Set variables
        for (int i = 0; i < variables.length; i++) {
            variables[i].set((double)patientAttributes[i]);
        }
    }
}
