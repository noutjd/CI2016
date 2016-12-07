import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork neuralNetworkDad;
    private NeuralNetwork neuralNetworkMom;
    private NeuralNetwork neuralNetwork;

    public DefaultDriver() {
        initialize();
        neuralNetworkDad = new NeuralNetwork("W1_alldata2.csv", "W2_alldata2.csv", 22, 100, 3);
        neuralNetwork = new NeuralNetwork("W1_alldata1.csv", "W2_alldata1.csv", 22, 100, 3);
        //neuralNetwork = NeuralNetwork.makeChildSGA(neuralNetworkDad, neuralNetworkMom);
        neuralNetwork = new NeuralNetwork("W1_reallyalldata16.csv", "W2_reallyalldata16.csv", 22, 100, 3);
        System.out.println(neuralNetworkMom);
        System.out.println(neuralNetworkDad);
        System.out.println(neuralNetwork);
//        neuralNetwork = neuralNetwork.loadGenome();
    }

    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    @Override
    public double getAcceleration(SensorModel sensors) {
        double[] sensorArray = new double[4];
        try {
            double output = neuralNetwork.getOutput(sensors);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        try {
            Double output = neuralNetwork.getOutput(sensors);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.5;
    }

    @Override
    public String getDriverName() {
        return "Euler Forward";
    }

    @Override
    public Action controlWarmUp(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlQualification(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlRace(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
        if (action == null) {
            action = new Action();
        }
        double[] outputs = new double[3];
        try {
            outputs = neuralNetwork.feedForward(sensors);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(outputs[0] > 0.9 && outputs[1] < 0.1) {
            outputs[0] = 1;
            outputs[1] = 0;
            //System.out.println("Acceleration correction, full trottle!");
        }

        if(outputs[0] < 0.1 && outputs[1] > 0.5) {
            outputs[0] = 0;
            //System.out.println("Braking correction, no throttle here!");
        }
        action.accelerate = outputs[0];
        action.brake = outputs[1];
        action.steering = 2 * outputs[2] - 1;

        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        if(trackEdgeSensors[1] < 1.5) action.steering = action.steering - 0.15;
        if(trackEdgeSensors[17] < 1.5) action.steering = action.steering + 0.15;

        action.steering = action.steering + 0.3 * DriversUtils.alignToTrackAxis(sensors, 0.5);

/*        action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
        if (sensors.getSpeed() > 60.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() > 70.0D) {
            action.accelerate = 0.0D;
            action.brake = -1.0D;
        }

        if (sensors.getSpeed() <= 60.0D) {
            action.accelerate = (80.0D - sensors.getSpeed()) / 80.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() < 30.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }*/
        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-----------------------------------------------");
        /*
        double[] wheelSpeeds = sensors.getWheelSpinVelocity();
        double currSpeed = sensors.getSpeed();
        for(int i = 0; i < wheelSpeeds.length; i ++) {
            System.out.println(wheelSpeeds[i]);
        }
        System.out.println(currSpeed);
        */
        double maxLatSpeed = 0;
        double latSpeed = sensors.getLateralSpeed();
        if(latSpeed > maxLatSpeed) {
            maxLatSpeed = latSpeed;
        }
        System.out.println(latSpeed);
        System.out.println(maxLatSpeed);
        return action;
    }
}