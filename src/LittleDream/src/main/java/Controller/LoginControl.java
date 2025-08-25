package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import Entity.HibernateUtil;
import Entity.UserAccount;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginControl {
	
    @FXML
    private AnchorPane loginPane;
    
	@FXML
    private ImageView npc;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;
	
	@FXML
    private Pane leaderboardPane;
	
	@FXML
    private StackPane stackPane;
	
	@FXML
    private TableView<UserAccount> highestTable;

	@FXML
	private TableColumn<UserAccount, String> userCol3;
	
	@FXML
    private TableColumn<UserAccount, Integer> highestCol;

	@FXML
	private TableView<UserAccount> totalTable;

	@FXML
	private TableColumn<UserAccount, String> userCol2;

	@FXML
    private TableColumn<UserAccount, Integer> totalCoinCol;

    @FXML
    private TableView<UserAccount> winTable;
	
	@FXML
    private TableColumn<UserAccount, String> userCol1;

	@FXML
    private TableColumn<UserAccount, Integer> winCol;
	
	private MediaPlayer mediaPlayer;
	private boolean isPaneVisible = false;
	private boolean isLoad = false;

	
	@FXML
    private void initialize() {
		
		String musicFile = getClass().getResource("/Media/outside.mp3").toString();
		Media sound = new Media(musicFile);
		mediaPlayer = new MediaPlayer(sound);
		
		mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
		mediaPlayer.play();
		
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
			npc.setX((npc.getX() + 1) % 900);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        userCol3.setCellValueFactory(new PropertyValueFactory<>("username"));
        highestCol.setCellValueFactory(new PropertyValueFactory<>("highestCoin"));
        userCol2.setCellValueFactory(new PropertyValueFactory<>("username"));
        totalCoinCol.setCellValueFactory(new PropertyValueFactory<>("totalCoin"));
        userCol1.setCellValueFactory(new PropertyValueFactory<>("username"));
        winCol.setCellValueFactory(new PropertyValueFactory<>("winTime"));
        
	}

    @FXML
    void clickLoginButton(MouseEvent event) {
    	String username = usernameField.getText();
		String password = passwordField.getText();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			String hql = "FROM UserAccount WHERE username = :username";
			Query<UserAccount> query = session.createQuery(hql, UserAccount.class);
			query.setParameter("username", username);
			UserAccount user = query.uniqueResult();

			session.getTransaction().commit();
			if (user != null && encrypt(password).equals(user.getPassword())) {
				
				showAlert("Success", null, "User Sign In successfully. Login Now!", AlertType.INFORMATION);
                switchToWaitScene(username);
                
            } else {
            	showAlert("Login Error", null, "Incorrect password or username does not exist.", AlertType.ERROR);
			}
		} catch (HibernateException e) {
			showAlert("Database Error", "An error occurred while accessing the database.", e.getMessage(), AlertType.ERROR);
		}
		usernameField.clear();
        passwordField.clear();
    }
    
    @FXML
    void clickSignUpButton(MouseEvent event) {
    	String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
        	
        	showAlert("Error Dialog", "Sign Up Error", "Please fill in all text field! (Username and Password)", AlertType.ERROR);
        	
        } else {

            String hashedPassword = encrypt(password);

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();

                UserAccount user = new UserAccount();
                user.setUsername(username);
                user.setPassword(hashedPassword);

                session.save(user);
                session.getTransaction().commit();

                showAlert("Success", null, "User Sign Up successfully. Login Now!", AlertType.INFORMATION);
            } catch (HibernateException e) {
            	showAlert("Error Dialog", "Sign Up Error", "An error occurred while registering user!", AlertType.ERROR);
                throw new RuntimeException(e);
            }
        }
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    void clickHelpButton(MouseEvent event) {
    	showAlert("Forget Password", null, "Please contact admin to retrieve your password!", AlertType.INFORMATION);
    }

    @FXML
    void clickRanking(MouseEvent event) {
    	if (!isPaneVisible) {
    		leaderboardPane.setVisible(true);
    		isPaneVisible = true;
    		if (!isLoad)
    			loadData();
    	} else {
    		leaderboardPane.setVisible(false);
    		isPaneVisible = false;
    	}
    }
    
    @FXML
    void tableToLeft(MouseEvent event) {
    	Node bottomNode = stackPane.getChildren().get(0);
    	bottomNode.toFront();
    }

    @FXML
    void tableToRight(MouseEvent event) {
    	Node topNode = stackPane.getChildren().get(stackPane.getChildren().size() - 1);
    	topNode.toBack();
    }
    
    private void loadData() {
    	isLoad = true;
    	try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			Query<UserAccount> query = session.createQuery("From UserAccount", UserAccount.class);
			List<UserAccount> allUser = query.list();

			session.getTransaction().commit();
			
			ObservableList<UserAccount> highestList = FXCollections.observableArrayList();
			ObservableList<UserAccount> totalList = FXCollections.observableArrayList();
			ObservableList<UserAccount> winList = FXCollections.observableArrayList();
			
			highestList.addAll(allUser);
			totalList.addAll(allUser);
			winList.addAll(allUser);
			
			highestList.sort((a,b) -> Integer.compare(b.getHighestCoin(), a.getHighestCoin()));
			totalList.sort((a,b) -> Integer.compare(b.getTotalCoin(), a.getTotalCoin()));
			winList.sort((a,b) -> Integer.compare(b.getWinTime(), a.getWinTime()));
			
			highestTable.setItems(highestList);
			totalTable.setItems(totalList);
			winTable.setItems(winList);
			
		} catch (HibernateException e) {
			showAlert("Database Error", "An error occurred while accessing the database.", e.getMessage(), AlertType.ERROR);
		}
    }
    
    private String encrypt(String msg) {
    	try {
			byte[] data = msg.getBytes();
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data);
			String base64Hash = Base64.getEncoder().encodeToString(hash);
			return base64Hash;
    	} catch (NoSuchAlgorithmException e) {
    		showAlert("Error Dialog", "Encrypt Error", "Cannot find algorithm.", AlertType.ERROR);
    		e.printStackTrace();
    		return null;
    	}
    }
    
    private void switchToWaitScene(String username) {
        try {
        	mediaPlayer.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/waitingRoom.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            WaitControl control = loader.getController();
            control.setUsername(username);
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
        	showAlert("Error Dialog", "Cannot go to Waiting Room", "Cannot find path.", AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String header, String content, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.initOwner(loginPane.getScene().getWindow());
		alert.showAndWait();
	}

}
