package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exception.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService sellerService;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtBaseSalary;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;

	@FXML
	private Label labelErrorBirthDate;

	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService sellerService, DepartmentService departmentService) {
		this.sellerService = sellerService;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity estava vazio");
		}
		if (sellerService == null) {
			throw new IllegalStateException("Service estava vazio");
		}
		try {
			entity = getFormData();
			sellerService.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();

		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Erro para salvar Objeto", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {

		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {
		Seller objSeller = new Seller();

		ValidationException exception = new ValidationException("Validation error");

		objSeller.setId(Utils.tryParseToInt(txtId.getText()));

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		objSeller.setName(txtName.getText());
		
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("email", "Field can't be empty");
		}
		objSeller.setEmail(txtEmail.getText());
		
		if(dpBirthDate.getValue() == null) {
			exception.addError("birthDate","Field can't be empty");
		}
		else {
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault())); // Forma de pegar uma data de um DatePicker FX
			objSeller.setBirthDate(Date.from(instant));
		}
		
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "Field can't be empty");
		}
		objSeller.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));
		
		objSeller.setDepartment(comboBoxDepartment.getValue());
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return objSeller;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {

		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldMaxLength(txtEmail, 100);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		Constraints.setTextFieldDouble(txtBaseSalary);
		initializeComboBoxDepartment();

	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity estava vazia");
		}

		txtId.setText(String.valueOf(entity.getId())); // TextField recebe somente em forma��o em String, por isso �
														// necess�ria a convers�o
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));

		if (entity.getBirthDate() != null) {
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		
		if(entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
		
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null");
		}
		List<Department> list = departmentService.findall();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		labelErrorName.setText(fields.contains("name") ? errors.get("name") : "");
		labelErrorEmail.setText(fields.contains("email") ? errors.get("email") : "");
		labelErrorBaseSalary.setText(fields.contains("baseSalary") ? errors.get("baseSalary") : "");
		labelErrorBirthDate.setText(fields.contains("birthDate") ? errors.get("birthDate") : "");
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
