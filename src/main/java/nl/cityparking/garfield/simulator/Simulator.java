package nl.cityparking.garfield.simulator;

import nl.cityparking.garfield.simulator.config.Configuration;
import nl.cityparking.garfield.simulator.parking.CarQueue;

import java.util.Collection;

/**
 * Simulator is an abstraction of a parking garage. It's the primary controller of all the happenings in the simulator
 * and serves as its heart and core. It pumps data around from one place to another, makes sure events are handled and
 * processed properly and makes sure that subsystems of the simulator are called when required.
 *
 * The Simulator puts it all together and presents this data in a way that a frontend such as a GUI can use.
 *
 * @author Jesse
 * @since 1.0
 */
public class Simulator implements Runnable {
	private Configuration configuration;
    private SimulatorTime simulationTime;
    private AgentManager agentManager = new AgentManager();
    private ArrivalManager arrivalManager = new ArrivalManager();
    private ParkingManager parkingManager = new ParkingManager();
    private EconomyManager economyManager = new EconomyManager();
    private CarQueue carQueue = new CarQueue();

    private boolean stopping = false;
    private long carsIn = 0;
    private long carsOut = 0;
    private long queueSize = 0;

	/**
	 * Initializes the Simulator and prepares it for runtime.
	 * @param configuration Configuration object with settings to be used by the simulator runtime.
	 */
	public Simulator(Configuration configuration) {
		this.configuration = configuration;

		simulationTime = new SimulatorTime(1);
		simulationTime.setOnTick(this::onTick);
		simulationTime.setOnMinutePassed(this::onMinutePassed);
		simulationTime.setOnHourPassed(this::onHourPassed);
		simulationTime.setOnDayPassed(this::onDayPassed);
		simulationTime.setOnWeekPassed(this::onWeekPassed);

		parkingManager.generateFromLayout(this.configuration.garageLayout);
	}

	/**
	 * Starts the simulation. This method blocks until stop is called.
	 */
	public void run() {
		stopping = false;

		while (!stopping) {
        	simulationTime.tick();
        }
    }

	/**
	 * Stops the simulation.
	 */
	public void stop() {
		stopping = true;
	}

    private void onTick() {
    }


    private void onMinutePassed() {
    	// Phase one, get leavers:
	    Collection<Departure> departures = parkingManager.getLeavingAgents(simulationTime.getMinutesPassed());
	    economyManager.processPayments(departures);
	    carsOut += departures.size();

	    // Phase two, get arrivals:
    	Collection<Arrival> arrivals = arrivalManager.getArrivals(simulationTime.getMinutesPassed());
    	for (Arrival arrival: arrivals) {
		    carQueue.addToQueue(arrival);
	    }
    	Collection<Arrival> queueArrivals = carQueue.removeFromQueue(4);

    	for(Arrival arrival : queueArrivals) {
    		if (parkingManager.handleArrival(arrival)) {
    			economyManager.processArrival(arrival);
    			carsIn++;
		    }
	    }
    	//System.out.println(carQueue.getQueueLength());
    }

	private void onHourPassed() {
	}

	private void onDayPassed() {
		economyManager.getEconomy().finalizeReport(simulationTime.getMinutesPassed());
    }

    private void onWeekPassed() {
    	arrivalManager.generate(agentManager.getCommuters(), simulationTime.getMinutesPassed());

    }

	public SimulatorTime getSimulationTime() {
		return simulationTime;
	}

	public long getCarsIn() {
		return carsIn;
	}

	public long getCarsOut() {
		return carsOut;
	}

	public ParkingManager getParkingManager() {
    	return parkingManager;
	}

	public EconomyManager getEconomyManager() {
		return economyManager;
	}
}
