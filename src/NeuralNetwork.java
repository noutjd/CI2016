import scr.SensorModel;

import java.io.*;
import java.util.Arrays;

import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork implements Serializable {

    //FIELDS
    private static final long serialVersionUID = -88L;
    int inputs;
    int hidden;
    int outputs;
    double[][] firstLayer;
    double[][] secondLayer;

    //CONSTRUCTOR
    public NeuralNetwork(int inputs, int hidden, int outputs) {
        this.inputs = inputs;
        this.hidden = hidden;
        this.outputs = outputs;
        this.firstLayer = new double[hidden][inputs + 1];
        this.secondLayer = new double[outputs][hidden + 1];
    }

    //IMPORT FROM DATA FILES - THIS SHOULD BECOME A CONSTRUCTOR RETURNING A NEURAL NETWORK
    public NeuralNetwork(String w1FileName, String w2FileName, int inputs, int hidden, int outputs) {
        this.inputs = inputs;
        this.hidden = hidden;
        this.outputs = outputs;

        double[][] W1 = new double[hidden][inputs + 1];
        double[][] W2 = new double[outputs][hidden + 1];
        try{
            InputStream input = getClass().getResourceAsStream(w1FileName);
            BufferedReader opened = new BufferedReader(new InputStreamReader(input));
            String line;
            int to_index = 0;
            int from_index;
            while((line = opened.readLine()) != null) {
                from_index = 0;
                String[] matrixline = line.split(",");
                for(String s: matrixline) {
                    W1[to_index][from_index] = Double.parseDouble(s);
                    from_index ++;
                }
                to_index ++;
            }
        //System.out.println(Arrays.deepToString(W1));
        } catch (IOException e) {
            System.out.println("File is not present");
        }

        try{
            InputStream input = getClass().getResourceAsStream(w2FileName);
            BufferedReader opened = new BufferedReader(new InputStreamReader(input));
            String line;
            int to_index = 0;
            int from_index;
            while((line = opened.readLine()) != null) {
                from_index = 0;
                String[] matrixline = line.split(",");
                for(String s: matrixline) {
                    W2[to_index][from_index] = Double.parseDouble(s);
                    from_index ++;
                }
                to_index ++;
            }
            //System.out.println(Arrays.deepToString(W2));
        } catch (IOException e) {
            System.out.println("File is not present");
        }
        this.firstLayer = W1;
        this.secondLayer = W2;
    }

    public double getOutput(SensorModel a) {
        return 0.5;
    }

    //uniform crossover, does not work well.
    public static NeuralNetwork makeChild(NeuralNetwork mom, NeuralNetwork dad) {
        double x;
        if(mom.inputs == dad.inputs && mom.hidden == dad.hidden && mom.outputs == dad.outputs) {
            NeuralNetwork child = new NeuralNetwork(mom.inputs, mom.hidden ,mom.outputs);
            for(int i = 0; i < child.firstLayer.length; i ++) {
                for(int j = 0; j < child.firstLayer[i].length; j ++) {
                    x = Math.random();
                    if(x < 0.5) {
                        child.firstLayer[i][j] = mom.firstLayer[i][j];
                    } else {
                        child.firstLayer[i][j] = dad.firstLayer[i][j];
                    }
                }
            }
            for(int i = 0; i < child.secondLayer.length; i ++) {
                for(int j = 0; j < child.secondLayer[i].length; j ++) {
                    x = Math.random();
                    if(x < 0.5) {
                        child.secondLayer[i][j] = mom.secondLayer[i][j];
                    } else {
                        child.secondLayer[i][j] = dad.secondLayer[i][j];
                    }
                }
            }
            return child;
        } else {
            System.out.println("Parent dimensions do not agree!");
            return null;
        }
    }

    public static NeuralNetwork makeChildSGA(NeuralNetwork mom, NeuralNetwork dad) {
        if(mom.inputs == dad.inputs && mom.hidden == dad.hidden && mom.outputs == dad.outputs) {
            NeuralNetwork child = new NeuralNetwork(mom.inputs, mom.hidden ,mom.outputs);
            boolean momFirst = Math.random() < 0.5;
            int cutOffPoint1 = ThreadLocalRandom.current().nextInt(0, mom.hidden);
            if(momFirst) {
                for (int i = 0; i < cutOffPoint1; i++) {
                    child.firstLayer[i] = mom.firstLayer[i].clone();
                }
                for (int i = cutOffPoint1; i < mom.hidden; i++) {
                    child.firstLayer[i] = dad.firstLayer[i].clone();
                }
            } else {
                for (int i = 0; i < cutOffPoint1; i++) {
                    child.firstLayer[i] = dad.firstLayer[i].clone();
                }
                for (int i = cutOffPoint1; i < mom.hidden; i++) {
                    child.firstLayer[i] = mom.firstLayer[i].clone();
                }
            }
            momFirst = Math.random() < 0.5;
            int cutOffPoint2 = ThreadLocalRandom.current().nextInt(0, mom.outputs);
            if(momFirst) {
                for (int i = 0; i < cutOffPoint2; i++) {
                    child.secondLayer[i] = mom.secondLayer[i].clone();
                }
                for (int i = cutOffPoint2; i < mom.outputs; i++) {
                    child.secondLayer[i] = dad.secondLayer[i].clone();
                }
            } else {
                for (int i = 0; i < cutOffPoint2; i++) {
                    child.secondLayer[i] = dad.secondLayer[i].clone();
                }
                for (int i = cutOffPoint2; i < mom.outputs; i++) {
                    child.secondLayer[i] = mom.secondLayer[i].clone();
                }
            }
            return child;
        } else {
            System.out.println("Parent dimensions do not agree!");
            return null;
        }
    }

    public static NeuralNetwork makeChildAverage(NeuralNetwork mom, NeuralNetwork dad) {
        if(mom.inputs == dad.inputs && mom.hidden == dad.hidden && mom.outputs == dad.outputs) {
            NeuralNetwork child = new NeuralNetwork(mom.inputs, mom.hidden ,mom.outputs);
            for(int i = 0; i < child.firstLayer.length; i ++) {
                for(int j = 0; j < child.firstLayer[i].length; j ++) {
                    child.firstLayer[i][j] = 0.5 * (mom.firstLayer[i][j] + dad.firstLayer[i][j]);
                }
            }
            for(int i = 0; i < child.secondLayer.length; i ++) {
                for(int j = 0; j < child.secondLayer[i].length; j ++) {
                    child.secondLayer[i][j] = 0.5 * (mom.secondLayer[i][j] + dad.secondLayer[i][j]);
                }
            }
            return child;
        } else {
            System.out.println("Parent dimensions do not agree!");
            return null;
        }
    }

    public double[] feedForward(SensorModel a) throws Exception {
        double[] input_layer = new double[this.inputs + 1];
        input_layer[0] = 1;
        input_layer[1] = a.getSpeed();
        input_layer[2] = a.getTrackPosition();
        input_layer[3] = a.getAngleToTrackAxis();
        double[] temp = a.getTrackEdgeSensors();
        for(int i = 0; i < 19; i ++) {
            input_layer[i + 4] = temp[i];
        }
        double[] hidden_layer = new double[this.hidden + 1];
        hidden_layer[0] = 1;
        for(int i = 0; i < this.hidden; i ++)  {
            hidden_layer[i + 1] = sigmoid(dot(input_layer, this.firstLayer[i]));
        }
        double[] output_layer = new double[this.outputs];
        for(int i = 0; i < this.outputs; i ++) {
            output_layer[i] = sigmoid(dot(hidden_layer,this.secondLayer[i]));
        }
        return output_layer;
    }

    //Store the state of this neural network
    public void storeGenome() {
        ObjectOutputStream out = null;
        try {
            //create the memory folder manually
            out = new ObjectOutputStream(new FileOutputStream("memory/mydriver.mem"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.writeObject(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load a neural network from memory
    public NeuralNetwork loadGenome() {

        // Read from disk using FileInputStream
        FileInputStream f_in = null;
        try {
            f_in = new FileInputStream("memory/mydriver.mem");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Read object using ObjectInputStream
        ObjectInputStream obj_in = null;
        try {
            obj_in = new ObjectInputStream(f_in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read an object
        try {
            if (obj_in != null) {
                return (NeuralNetwork) obj_in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    double dot(double[] x, double[] y) throws Exception {
        if(x.length != y.length) {
            throw new Exception("Vectors not of equal size");
        } else {
            double dotprod = 0;
            for(int i = 0; i < x.length; i ++) {
                dotprod += x[i] * y[i];
            }
            return dotprod;
        }
    }

    double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }



    public String toString() {
        String returnstr = "first layer:\n";
        returnstr += (Arrays.deepToString(this.firstLayer) + "\nSecond layer:\n");
        returnstr += Arrays.deepToString(this.secondLayer);
        return returnstr;
    }

}
