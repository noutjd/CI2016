import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork neuralNetwork;
    String name = null;
    public double maxDriftAngle = 0;
    int stuck = 0;
    private int stuckstill = 0;

    public DefaultDriver() {
        initialize();
        this.setNeuralNetwork(new NeuralNetwork("W1_alldata2.csv", "W2_alldata2.csv", 22, 100, 3));
        this.name = null;
    }

    public NeuralNetwork getNeuralNetwork() {
        return this.neuralNetwork;
    }

    public void setNeuralNetwork(NeuralNetwork inputNetwork) {
        this.neuralNetwork = inputNetwork;
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
        if (this.name == null) {
            return "Euler Forward";
        } else {
            return this.name;
        }
    }

    public void setDriverName(String name) {
        this.name = name;
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
        //--------------------------------------------------------------------------------
        //Get output from the neural network through feedForward
        double[] outputs = new double[3];
        try {
            outputs = neuralNetwork.feedForward(sensors);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Round off network output: if almost full throttle, go full throttle!
        if(outputs[0] > 0.9 && outputs[1] < 0.1) {
            outputs[0] = 1;
            outputs[1] = 0;
        }//If braking hard, don't accelerate too!
        if(outputs[0] < 0.1 && outputs[1] > 0.5) {
            outputs[0] = 0;
        }
        action.accelerate = outputs[0];
        action.brake = outputs[1];
        action.steering = 2 * outputs[2] - 1;

        //---------------------------------------------------------------------------------
        //Evade other drivers, go into safe mode if it's slippery
        double[] opponentSensors = sensors.getOpponentSensors(); //17, 18 and 19 look ahead
        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        double lateralSpeed = sensors.getLateralSpeed();
        double driftAngle = Math.toDegrees(Math.atan2(lateralSpeed, sensors.getSpeed()));
        if(Math.abs(driftAngle) > this.maxDriftAngle && sensors.getSpeed() > 20) {
            this.maxDriftAngle = Math.abs(driftAngle);
        }
        //System.out.println(action.accelerate + ", " + driftAngle + ", " + this.maxDriftAngle);
        if(Math.abs(driftAngle) > 20) {
            action.accelerate = action.accelerate * 0.5;
            System.out.println("Correcting! Throttle reduced to " + action.accelerate + ", angle " + driftAngle);
        }

        // Opponent proximity - brake if someone is right up front!
        if((opponentSensors[17] < 3 || opponentSensors[18] < 10 || opponentSensors[19] < 3) && sensors.getSpeed() > 40) {
            action.brake = 1;
        }

        action.steering = action.steering + 0.2 * DriversUtils.alignToTrackAxis(sensors, 0.5);

        //-----------------------------------------------------------------------------
        // Apply unstucking if stuck. Copied from AutomatedRecovering into here.
        if(sensors.getSpeed() < 5.0D && sensors.getDistanceFromStartLine() > 0.0D) {
            this.stuckstill = this.stuckstill + 20;
        }

        if(Math.abs(sensors.getAngleToTrackAxis()) > 0.5235987901687622D) {
            if(this.stuck > 0 || Math.abs(sensors.getTrackPosition()) > 0.85D) {
                this.stuck = this.stuck + 10;
            }
        } else if(this.stuck > 0 && Math.abs(sensors.getAngleToTrackAxis()) < 0.3D && sensors.getSpeed() > 10) {
            this.stuck = Math.max(this.stuck - 1, 0);
            this.stuckstill = Math.max(this.stuckstill - 1, 0);
        }

        if(this.stuckstill > 50) {
            this.stuck = 26;
        }

        if(trackEdgeSensors[0] != -1 && sensors.getSpeed() > 10 && Math.abs(sensors.getAngleToTrackAxis()) < 0.3D) {
            this.stuck = 0;
            this.stuckstill = 0;
        }

        if(this.stuck > 25) {
            action.accelerate = 0.7D;
            action.brake = 0.0D;
            action.gear = -1;
            action.steering = -1.0D;
            if(sensors.getAngleToTrackAxis() < 0.0D) {
                action.steering = 1.0D;
            }

            if(sensors.getTrackEdgeSensors()[9] > 3.0D || sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0.0D) {
                action.gear = 1;
                if(sensors.getSpeed() < -0.2D) {
                    action.brake = 1.0D;
                    action.accelerate = 0.0D;
                }
            }

            if(sensors.getSpeed() > 0.0D) {
                action.steering = -action.steering;
            }
        }
        //-------------------------------------------------------------------------------------------
        //Communicate output to console
        boolean PRAATGRAAG = false;
        if(PRAATGRAAG) {
            System.out.println("--------------" + getDriverName() + "--------------");
            System.out.println("Steering: " + action.steering);
            System.out.println("Acceleration: " + action.accelerate);
            System.out.println("Brake: " + action.brake);
            System.out.println("-----------------------------------------------");
        }
        //-------------------------------------------------------------------------------------------
        //Save input/output to klad.csv
        boolean GEN_DATA = false; //Generate data along the way? Write to klad.csv
        if(GEN_DATA) {
            String s = "" + action.accelerate + ", " + action.brake + ", " + action.steering + ", " + sensors.getSpeed()
                    + ", " + sensors.getTrackPosition() + ", " + sensors.getAngleToTrackAxis();
            double[] track_edge_senors = sensors.getTrackEdgeSensors();
            for (int i = 0; i < track_edge_senors.length; i++) {
                s += ", " + track_edge_senors[i];
            }
            s += "\n";
            //System.out.println(s);
            File outFile = new File("klad.csv");
            try {
                outFile.createNewFile();
                FileWriter fstreamWrite = null;
                fstreamWrite = new FileWriter("klad.csv", true);
                BufferedWriter out = new BufferedWriter(fstreamWrite);
                out.write(s);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return action;
    }
}