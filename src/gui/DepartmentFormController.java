package gui;

import java.net.URL;
import java.util.ResourceBundle;

import db.DbException;
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
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable{

	private Department entity;
	
	private DepartmentService depService;
	
	@FXML
	private TextField txtId;
	
	@FXML
	private TextField txtName;
	
	@FXML
	private Label labelErrorName;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	public void setDepartment(Department entity ) {
		this.entity = entity;
	}
	
	public void setDeparmentService(DepartmentService depService) {
		this.depService = depService;
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if( entity == null ) {
			throw new IllegalStateException("Entity estava vazio");
		}
		if( depService == null ) {
			throw new IllegalStateException("Service estava vazio");
		}
		try {
			entity = getFormData();
			depService.saveOrUpdate(entity);
			Utils.currentStage(event).close();
			
		}catch (DbException e) {
			Alerts.showAlert("Erro para salvar Objeto", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	private Department getFormData() {
		Department objDep = new Department();
		
		objDep.setId(Utils.tryParseToInt(txtId.getText()));
		objDep.setName(txtName.getText());
		
		return objDep;
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
		Constraints.setTextFieldMaxLength(txtName, 30);
	}
	
	public void updateFormData() {
		if(entity == null) {
			throw new IllegalStateException("Entity estava vazia");
		}
		
		txtId.setText(String.valueOf(entity.getId())); // TextField recebe somente em formação em String, por isso é necessária a conversão
		txtName.setText(entity.getName());
	}

}
