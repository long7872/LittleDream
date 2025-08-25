package Run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import Controller.Control;
import Entity.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Image/icon.png")));
		primaryStage.setTitle("Little Dream");
		
		FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/login.fxml"));
//		FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/waitingRoom.fxml"));
//		FXMLLoader loader3 = new FXMLLoader(getClass().getResource("/main.fxml"));
		
		Parent root = loader1.load();
//		Parent root = loader2.load();
//		Parent root = loader3.load();
		
		Scene scene = new Scene(root, 635, 360);
		
		new Thread(() -> {
			HibernateUtil.buildSessionFactory();
		}).start();
		
//		ScreenController screenController = new ScreenController(scene);
//		screenController.addScreen("waiting", FXMLLoader.load(getClass().getResource( "/waitingRoom.fxml" )));
//		screenController.activate("waiting");
		
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		
//		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                Platform.exit();
//                System.exit(0);
//            }
//        });
		
		primaryStage.show();
		
	}
	
	private static void logConsole() {
		Thread logging = new Thread(() -> {
			try {
				File log = Control.createFile("src/main/resources/logConsoleClient", ".txt");
				PrintStream fileOut = new PrintStream(new FileOutputStream(log, true));
				System.setOut(fileOut);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		});
		logging.setDaemon(true);
		logging.start();
		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		logConsole();
		launch(args);
	}

}
