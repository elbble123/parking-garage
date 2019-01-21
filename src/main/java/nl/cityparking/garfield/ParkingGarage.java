package nl.cityparking.garfield;

import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import nl.cityparking.garfield.gui.PrimaryView;
import nl.cityparking.garfield.gui.simulator.GarageView;
import nl.cityparking.garfield.gui.simulator.ParkingLotController;
import nl.cityparking.garfield.gui.simulator.SimulatorControls;
import nl.cityparking.garfield.simulator.Simulator;
import nl.cityparking.garfield.simulator.SimulatorService;
import nl.cityparking.garfield.simulator.SimulatorState;
import nl.cityparking.garfield.simulator.config.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.URL;

public class ParkingGarage extends Application {
	private Simulator simulator;
	private SimulatorState state = new SimulatorState();
	private SimulatorService service;
	private GarageView garageView = null;

	private PrimaryView primaryViewController;
	private Pane primaryView;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		try {
			URL configResource = this.getClass().getResource("/config/SimulatorConfig.xml");

			JAXBContext context = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Configuration configuration = (Configuration) unmarshaller.unmarshal(configResource);

			simulator = new Simulator(configuration);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread simulatorThread = new Thread(simulator, "Simulator Thread");
		simulatorThread.start();

		// Start our simulator service, too.
		this.service = new SimulatorService(simulator);
		service.setPeriod(Duration.millis(10));
		service.setOnSucceeded(this::updateSimulatorState);
		service.start();

		stage.setTitle("Parking Garage");

		try {
			FXMLLoader loader = this.createLoader("/views/primaryView.fxml");
			this.primaryView = loader.load();
			this.primaryViewController = loader.getController();
		} catch(Exception e) {
			e.printStackTrace();
		}

		if (this.primaryView != null) {
			Scene scene = new Scene(this.primaryView, 0, 0);
			stage.setScene(scene);
			scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());

			stage.show();

			try {
				FXMLLoader loader = this.createLoader("/views/garageView.fxml");
				Pane view = loader.load();
				garageView = loader.getController();
				garageView.setFloors(simulator.getParkingManager().getFloors());

				this.primaryViewController.setMainView(view);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				FXMLLoader loader = this.createLoader("/views/simulatorControls.fxml");
				SimulatorControls simulatorControlsController = new SimulatorControls(this.state);
				loader.setController(simulatorControlsController);
				Pane simulatorInfoView = loader.load();
				this.primaryViewController.setInfoBar(simulatorInfoView);

				simulatorControlsController.simulatorSpeedValueProperty().addListener((obs, ov, nv) -> {
					simulator.getSimulationTime().setTickSpeed(nv.longValue());
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void updateSimulatorState(WorkerStateEvent workerStateEvent) {
		SimulatorState newState = (SimulatorState) workerStateEvent.getSource().getValue();
		state.setSimulatorMinutes(newState.getSimulatorMinutes());
		state.setCarsTotalIn(newState.getCarsTotalIn());
		state.setCarsTotalOut(newState.getCarsTotalOut());

		garageView.update();
	}

	@Override
	public void stop() {
		if (simulator != null) {
			simulator.stop();
		}
	}

	public FXMLLoader createLoader(String filename) {
		URL resource = this.getClass().getResource(filename);

		if (resource != null) {
			return new FXMLLoader(resource);
		}

		return null;
	}
}
