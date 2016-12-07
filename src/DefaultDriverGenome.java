
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    public DefaultDriver defaultDriver;

    public DefaultDriverGenome(NeuralNetwork inputNetwork) {
        this.defaultDriver = new DefaultDriver(inputNetwork);
    }
}

