package gui;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	// Injection Service
	private Seller entity;
	private SellerService service;
	private DepartmentService departmentService;

	// Text field
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtEmail;
	@FXML
	private TextField txtBirthDate;
	@FXML
	private TextField txtBaseSalary;
	@FXML
	private TextField txtDepartmentName;

	// Error label
	@FXML
	private Label labelErrorName;
	@FXML
	private Label labelErrorEmail;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private Label labelErrorBaseSalary;
	@FXML
	private Label labeltErrorDepartmentName;

	// Button action
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	// OnChanges
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}

	}

	// Manual injection
	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	// Manual injection
	public void setSellerService(SellerService service) {
		this.service = service;
	}

	// Manual injection
	public void setDepartmentService(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}

		if (departmentService == null) {
			throw new IllegalStateException("departmentService was null");
		}
		try {
			entity = getFromData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();

		} catch (ValidationException e) {
			setErrorMessage(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}

	}

	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	private Seller getFromData() {

		clearErrorLabels();

		Seller obj = new Seller();
		Department department = new Department();
		Date date = null;

		ValidationException exception = new ValidationException("Validation error");

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("Name", "Field can't be empty");
		}
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("Email", "Field can't be empty");
		}
		if (txtBirthDate.getText() == null || txtBirthDate.getText().trim().equals("")) {
			exception.addError("BirthDate", "Field can't be empty");
		}
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("BaseSalary", "Field can't be empty");
		} else {
			date = Utils.tryParteToDate(txtBirthDate.getText());
			if (date == null) {
				exception.addError("Date", "Invalid format! should be: dd-MM-yyyy");
			}
		}
		if (txtDepartmentName.getText() == null || txtDepartmentName.getText().trim().equals("")) {
			exception.addError("DepartmentName", "Field can't be empty");
		} else {
			department = departmentService.findByName(txtDepartmentName.getText());
			if (department == null) {
				exception.addError("Department", "This department doesn't exist!");
			}
		}

		obj.setId(Utils.tryParseToInt(txtId.getText()));
		obj.setName(txtName.getText());
		obj.setEmail(txtEmail.getText());
		obj.setBirthDate(date);
		obj.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));
		obj.setDepartment(department);

		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return obj;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
		Constraints.setTextFieldMaxLength(txtEmail, 30);
		Constraints.setTextFieldMaxLength(txtBirthDate, 30);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtDepartmentName, 30);

	}

	private void setErrorMessage(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		if (fields.contains("Name")) {
			labelErrorName.setText(errors.get("Name"));
		}
		if (fields.contains("Email")) {
			labelErrorEmail.setText(errors.get("Email"));
		}
		if (fields.contains("BirthDate")) {
			labelErrorBirthDate.setText(errors.get("BirthDate"));
		}
		if (fields.contains("Date")) {
			labelErrorBirthDate.setText(errors.get("Date"));
		}
		if (fields.contains("BaseSalary")) {
			labelErrorBaseSalary.setText(errors.get("BaseSalary"));
		}
		if (fields.contains("DepartmentName")) {
			labeltErrorDepartmentName.setText(errors.get("DepartmentName"));
		}
		if (fields.contains("Department")) {
			labeltErrorDepartmentName.setText(errors.get("Department"));
		}

	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		txtBaseSalary.setText(String.valueOf(entity.getBaseSalary()));

		if (entity.getBirthDate() != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			txtBirthDate.setText(sdf.format(entity.getBirthDate()));
		}
		if (entity.getDepartment() != null) {
			txtDepartmentName.setText(entity.getDepartment().getName());
		}

	}

	private void clearErrorLabels() {
		labelErrorName.setText("");
		labelErrorEmail.setText("");
		labelErrorBirthDate.setText("");
		labelErrorBaseSalary.setText("");
		labeltErrorDepartmentName.setText("");
	}

}
