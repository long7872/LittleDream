package Controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WaitControl {
	
	@FXML
    private AnchorPane waitPane;

    @FXML
    private Label numPlayerText;

    @FXML
    private Button readyBtn;
    
    @FXML
    private Label startLabel;

    @FXML
    private Label timeLabel;
    
    @FXML
    private Label serverLabel;
    
    @FXML
    private Label timeServer;
    
    private MediaPlayer mediaPlayer;
    private Socket socketCreate;
    private ObjectInputStream inCreate;
    private ObjectOutputStream outCreate;
    private Socket socketDecision;
    private ObjectInputStream inDecision;
    private ObjectOutputStream outDecision;
    private int NOPs=2;
    private String username;
    
    @FXML
    private void initialize() {
    	String musicFile = getClass().getResource("/Media/waiting.mp3").toString();
		Media sound = new Media(musicFile);
		mediaPlayer = new MediaPlayer(sound);
		
		mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
		mediaPlayer.play();
    	
    	numPlayerText.setText(0+"/"+NOPs);
		Thread serverThread = new Thread(() -> {
			serverHandle();
		}); serverThread.start();
    	
    }
    
    public void setUsername(String username) {
    	this.username=username;
    }
    
    private void serverHandle() {
    	
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String ipAddress = localHost.getHostAddress();
		
			boolean in = false;
			do {
				serverLabel.setVisible(false);
				timeServer.setVisible(false);
				try {
					
					// Kết nối đến server
					socketCreate = new Socket(ipAddress, 1111);
					
					outCreate = new ObjectOutputStream(new BufferedOutputStream(socketCreate.getOutputStream()));
					outCreate.flush(); // Thêm dòng này để đảm bảo stream không bị treo
					
					inCreate = new ObjectInputStream(new BufferedInputStream(socketCreate.getInputStream()));
					
					socketDecision = new Socket(ipAddress, 2222);
					
					outDecision = new ObjectOutputStream(new BufferedOutputStream(socketDecision.getOutputStream()));
					outDecision.flush(); // Thêm dòng này để đảm bảo stream không bị treo
					
					inDecision = new ObjectInputStream(new BufferedInputStream(socketDecision.getInputStream()));
					
//	            npcAppearHandle(socketCreate);
					
					System.out.println("Connected to server.");
					in=false;
					
					for (int i=0; i<NOPs; i++) {
						int l = inCreate.readInt();
						System.out.println(l);
						Platform.runLater(() -> numPlayerText.setText(l + "/" + NOPs));
						System.out.println(l);
						if (l==NOPs) break;
					}
					
					String serverMsg = inCreate.readUTF();
					if (serverMsg.equals("open")) {
						Platform.runLater(() -> {
							startLabel.setVisible(true);
							timeLabel.setVisible(true);
						});
						
						startCountdown();
					}
					
				} catch (IOException e) {
					in = true;
					System.out.println("Error connect server.");
					Platform.runLater(() -> {
						serverLabel.setVisible(true);
						timeServer.setVisible(true);
					});
					
					for (int i = 3; i >= 0; i--) {
		                final int count = i;
		                Semaphore semaphore = new Semaphore(0);
		                Platform.runLater(() -> {timeServer.setText(count + " giây"); semaphore.release();});
		                try {
		                	semaphore.acquire();
		                    TimeUnit.SECONDS.sleep(1);
		                } catch (InterruptedException e1) {
		                    e.printStackTrace();
		                }
		            }
					//
			        
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					//
				}
			} while (in);
			
		} catch (UnknownHostException e) {
			System.out.println("Error InetAddress getLocalHost.");
		}
    }
    
    private void startCountdown() {
        Thread countdownThread = new Thread(() -> {
            for (int i = 3; i >= 1; i--) {
                final int count = i;
                Platform.runLater(() -> timeLabel.setText(count + " giây"));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> switchToMainScene());
        });
        countdownThread.start();
    }
    
    private void switchToMainScene() {
        try {
        	mediaPlayer.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) waitPane.getScene().getWindow();
            stage.setScene(scene);
            Control control = loader.getController();
            control.setupServer(socketCreate, inCreate, outCreate, socketDecision, inDecision, outDecision);
            control.setUsername(username);
            control.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void ready(MouseEvent event) {
    	try {
			outCreate.writeUTF("ready");
			outCreate.flush();
			System.out.println("Ready");
			readyBtn.setDisable(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


}
