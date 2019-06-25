package gui;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {

	// Inject the seller service
	private SellerService service;

	@FXML
	private Button btNew;

	@FXML
	private TableView<Seller> tableViewSeller;

	@FXML
	private TableColumn<Seller, Integer> tableColumnId;
	@FXML
	private TableColumn<Seller, String> tableColumnName;
	@FXML
	private TableColumn<Seller, Date> tableColumnBirthDate;
	@FXML
	private TableColumn<Seller, Double> tableColumnBaseSalary;
	@FXML
	private TableColumn<Seller, Department> tableColumnDepartmentName;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;
	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	// this way, we can get on the list on table date and show in view
	private ObservableList<Seller> obsList;

	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();

	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

	private void initializeNodes() {

		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		initializeTableColumnBirthDate();
		initializeTableColumnDepartmentName();


		// this, we can get the main scene
		Stage stage = (Stage) Main.getMainScene().getWindow();
		// allow height until limit the main view
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());

	}

	private void initializeTableColumnBirthDate() {
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

		tableColumnBirthDate.setCellFactory(parm -> new TableCell<>() {

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			@Override
			protected void updateItem(Date date, boolean empety) {
				super.updateItem(date, empety);

				if (date == null) {
					setText(null);
				} else {
					setText(sdf.format(date));
				}

			}
		});
	}

	private void initializeTableColumnDepartmentName() {

		// load all object with Id and Name apartment
		tableColumnDepartmentName.setCellValueFactory(new PropertyValueFactory<>("department"));

		// each department cell show just name or null if the obj is null
		tableColumnDepartmentName.setCellFactory(parm -> new TableCell<>() {
			@Override
			protected void updateItem(Department obj, boolean empety) {
				super.updateItem(obj, empety);

				if (obj == null) {
					setText(null);
				} else {
					setText(obj.getName());
				}
			};
		});

	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("Servie was null");
		}
		List<Seller> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obsList);
		
		initEditButtons();
		initRemoveButtons();

	}

	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setSellerService(new SellerService());
			controller.setDepartmentService(new DepartmentService());
			controller.updateFormData();
			controller.subscribeDataChangeListener(this);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Seller data"); // titulo da view
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false); // a janela é de tamanho fixo
			dialogStage.initOwner(parentStage); // indicar quem é o pai (o principal)
			dialogStage.initModality(Modality.WINDOW_MODAL); // se não fechar não pode aceder a janela anteriorv(criar o
																// modal)
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}

	}
	
	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
			}
		});
	}
	
	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Seller obj) {

		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");

		/*
		 * Optional é um objeto que carrega outro objeto dentro dele, podendo está
		 * presente ou não. Para podermos aceder o objeto dentro do Optional temos que
		 * efetuar o .get()
		 */
		if (result.get() == ButtonType.OK) {

			if (service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);

			}

		}
	}
}
