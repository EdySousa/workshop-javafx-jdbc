package gui.util;



import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class Utils {
	
	//permite pegar o stage do evento atual
	public static Stage currentStage(ActionEvent event) {
		return (Stage) ((Node) event.getSource()).getScene().getWindow();
	}
	
	// converter o a string no valor inteiro
	public static Integer tryParseToInt(String str) {
		try {
			return Integer.parseInt(str);	
		}catch (NumberFormatException e) {
			return null;
		}
	}

}
