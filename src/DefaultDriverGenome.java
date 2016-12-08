
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    public DefaultDriver defaultDriver;
    double fitness = 0.0D;

    public DefaultDriverGenome(NeuralNetwork inputNetwork, String name) {
        this.defaultDriver = new DefaultDriver(inputNetwork, name);
    }
}

