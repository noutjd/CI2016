import java.io.File;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.controller.Human;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm extends AbstractAlgorithm {

    private int NR_PLAYERS = 10;

    private static final long serialVersionUID = 654963126362653L;

    DefaultDriverGenome[] drivers = new DefaultDriverGenome[NR_PLAYERS];
    int[] results = new int[NR_PLAYERS];

    public Class<? extends Driver> getDriverClass() {
        return DefaultDriver.class;
    }

    public void run(boolean continue_from_checkpoint) {
        if (!continue_from_checkpoint) {
            //init NN

            NeuralNetwork inputNetwork1 = new NeuralNetwork("W1_alldata1.csv", "W2_alldata1.csv", 22, 100, 3);
            DefaultDriverGenome genome1 = new DefaultDriverGenome(inputNetwork1, "Driver 1");

            NeuralNetwork inputNetwork2 = new NeuralNetwork("W1_alldata1.csv", "W2_alldata1.csv", 22, 100, 3);
            DefaultDriverGenome genome2 = new DefaultDriverGenome(inputNetwork2, "Driver 2");

            NeuralNetwork inputNetwork3 = new NeuralNetwork("W1_alldata2.csv", "W2_alldata2.csv", 22, 100, 3);
            DefaultDriverGenome genome3 = new DefaultDriverGenome(inputNetwork3, "Driver 3");

            NeuralNetwork inputNetwork4 = new NeuralNetwork("W1_alldata2.csv", "W2_alldata1.csv", 22, 100, 3);
            DefaultDriverGenome genome4 = new DefaultDriverGenome(inputNetwork4, "Driver 4");

            NeuralNetwork inputNetwork5 = new NeuralNetwork("W1_alldata3.csv", "W2_alldata3.csv", 22, 100, 3);
            DefaultDriverGenome genome5 = new DefaultDriverGenome(inputNetwork5, "Driver 5");

            NeuralNetwork inputNetwork6 = new NeuralNetwork("W1_alldata4.csv", "W2_alldata4.csv", 22, 100, 3);
            DefaultDriverGenome genome6 = new DefaultDriverGenome(inputNetwork6, "Driver 6");

            NeuralNetwork inputNetwork7 = new NeuralNetwork("W1_alldata5.csv", "W2_alldata5.csv", 22, 100, 3);
            DefaultDriverGenome genome7 = new DefaultDriverGenome(inputNetwork7, "Driver 7");

            NeuralNetwork inputNetwork8 = new NeuralNetwork("W1_alldata6.csv", "W2_alldata6.csv", 22, 100, 3);
            DefaultDriverGenome genome8 = new DefaultDriverGenome(inputNetwork8, "Driver 8");

            NeuralNetwork inputNetwork9 = new NeuralNetwork("W1_alldata7.csv", "W2_alldata7.csv", 22, 100, 3);
            DefaultDriverGenome genome9 = new DefaultDriverGenome(inputNetwork9, "Driver9");

            NeuralNetwork inputNetwork10 = new NeuralNetwork("W1_alldata7.csv", "W2_alldata7.csv", 22, 100, 3);
            DefaultDriverGenome genome10 = new DefaultDriverGenome(inputNetwork10, "Driver10");

            drivers[0] = genome1;
            drivers[1] = genome2;
            drivers[2] = genome3;
            drivers[3] = genome4;
            drivers[4] = genome5;
            drivers[5] = genome6;
            drivers[6] = genome7;
            drivers[7] = genome8;
            drivers[8] = genome9;
            drivers[9] = genome10;

            //Start a race
            DefaultRace race = new DefaultRace();
            race.setTrack("aalborg", "road");
            race.laps = 1;


            //for speedup set withGUI to false
            results = race.runRace(drivers, true);

            for(int i = 0; i < drivers.length; i ++) {
                System.out.println("driver[" + i + "] finished with distance: " + results[i]);
            }

            int[] best5 = new int[5];
            for(int j = 1; j < 6; j ++) { // want 5 best, so j should go from 1 through 5
                for (int i = 0; i < NR_PLAYERS; i++) {
                    if (results[i] == j) {
                        best5[j-1] = i;
                        System.out.println(j + ", " + best5[j-1]);

                    }
                }
            }

            DefaultDriverGenome[] secondGenDrivers = new DefaultDriverGenome[NR_PLAYERS];
            int[] secondGenResults = new int[NR_PLAYERS];

            DefaultRace secondRace = new DefaultRace();
            secondRace.setTrack("aalborg", "road");
            secondRace.laps = 1;


            for(int i = 0; i < 5; i ++) {
                secondGenDrivers[i * 2 + 1] = drivers[best5[i]];
                secondGenDrivers[i * 2 + 2] = drivers[best5[i]];
                System.out.println("hoi " + i);
            }

  /*        Ik wil dat de andere vijf een combinatie van de eerste en de eerste vijf zijn. Doet ie niet.
            for(int i = 0; i < 5; i ++) {
                System.out.println("Made it here 1");
                NeuralNetwork tempNet = NeuralNetwork.makeChildAverage(
                        drivers[best5[1]].defaultDriver.getNeuralNetwork(), drivers[best5[i]].defaultDriver.getNeuralNetwork());
                secondGenDrivers[i + 4] = new DefaultDriverGenome(tempNet, "New Driver i");
            }
    */
            secondGenResults = secondRace.runRace(secondGenDrivers, true);

            for(int i = 0; i < secondGenDrivers.length; i ++) {
                System.out.println("driver[" + i + "] finished with distance: " + secondGenResults[i]);
            }

            // Save genome/nn
            //DriversUtils.storeGenome(drivers[0]);
        }
        // create a checkpoint this allows you to continue this run later
        //DriversUtils.createCheckpoint(this);
        //DriversUtils.clearCheckpoint();
    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));
        /*
		 *
		 * Start without arguments to run the algorithm
		 * Start with -continue to continue a previous run
		 * Start with -show to show the best found
		 * Start with -show-race to show a race with 10 copies of the best found
		 * Start with -human to race against the best found
		 *
		 */
        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
        //System.out.println(System.getProperty("user.dir"));
        DriversUtils.registerMemory(algorithm.getDriverClass());
        /*
        if (args.length > 0 && args[0].equals("-show")) {
            new DefaultRace().showBest();
        } else if (args.length > 0 && args[0].equals("-show-race")) {
            new DefaultRace().showBestRace();
        } else if (args.length > 0 && args[0].equals("-human")) {
            new DefaultRace().raceBest();
        } else if (args.length > 0 && args[0].equals("-continue")) {
            if (DriversUtils.hasCheckpoint()) {
                DriversUtils.loadCheckpoint().run(true);
            } else {
                algorithm.run();
            }
        } else {
            algorithm.run();

        }
        */
        algorithm.run();
    }

}