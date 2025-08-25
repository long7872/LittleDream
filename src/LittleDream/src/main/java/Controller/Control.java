package Controller;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Entity.HibernateUtil;
import Entity.UserAccount;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;

public class Control {
	
	private static final int HEIGHT_WINDOW = 900;
	private static final int WIDTH_WINDOW = 900;
	private static final int CELL_SIZE = 60;
	private final String clientId = UUID.randomUUID().toString();
	private String checkwin = null;
	
    @FXML
    private StackPane stackPane;
    
    @FXML
    private AnchorPane npcPane;
	
	@FXML
    private GridPane itemBar;
	
	@FXML
	private VBox itemInfoTable;
	
	@FXML
	private Label itemName;
	
	@FXML
	private Label itemCost;
	
	@FXML
	private Label itemIncomeAuto;	
	
	@FXML
	private Label itemIncomeClick;	

    @FXML
    private ImageView toggleItemBtn;
    
    @FXML
    private GridPane gridPane;
    private Map<Pair<Integer, Integer>, Node> cellMap;
    private Map<Node, ImageView> items;
    
    @FXML
    private ImageView item1;

    @FXML
    private Label moneyLabel;
    
    private MediaPlayer mediaPlayer;
    
    private Socket socketCreate;
    private ObjectInputStream inCreate;
    private ObjectOutputStream outCreate;
    private Socket socketDecision;
    private ObjectInputStream inDecision;
    private ObjectOutputStream outDecision;
    
    private List<ImageView> npcs;
    private Image npcWalkup;
    private Image npcWalkright;
    private Image npcWalkdown;
    private Image npcWalkleft;
    private AnchorPane paneMove;
    private ImageView itemMove;
    private List<ImageView> item1s;
    private int allPath;
    private ImageView disableMove;
    private ImageView zoomOutImg;
    private boolean isBarVisible = false;
    private double xOffset;
    private double yOffset;
    private double scale=10;
    private boolean zoom = false;
    private int NOPs = 2;
    private int npcMax = 1;
    private int condition;
    private int money = 120;
    private String username;
	private int item1Cost = 100;
    
    public void setUsername(String username) {
    	this.username=username;
    }
    public void setStage(Stage stage) {
    	
    	stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
    		@Override
    		public void handle(WindowEvent event) {
    			surrender();
    			try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	});
    }
    
	public void setupServer(Socket socketCreate, ObjectInputStream inCreate, ObjectOutputStream outCreate,
			Socket socketDecision, ObjectInputStream inDecision, ObjectOutputStream outDecision) {
		this.socketCreate = socketCreate;
		this.inCreate = inCreate;
		this.outCreate = outCreate;
		this.socketDecision = socketDecision;
		this.inDecision = inDecision;
		this.outDecision = outDecision;
		
		try {
			
			outCreate.writeUTF("key");
			outCreate.flush();
			
			condition = inDecision.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Thread npcAppear = new Thread(() -> {
			npcAppearHandle();
		});
		npcAppear.start();
	}
    
    @FXML
    private void initialize() {
    	
    	String musicFile = getClass().getResource("/Media/inMap.mp3").toString();
		Media sound = new Media(musicFile);
		mediaPlayer = new MediaPlayer(sound);
		
		mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
		mediaPlayer.play();
    	
    	item1s = new ArrayList<ImageView>();
    	items = new HashMap<>();
    	cellMap = new HashMap<>();
    	
    	Thread autoUpMoney = new Thread(() -> {
    		while (true) {
    			money+=item1s.size();
    			updateMoney(money);
    			try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}); autoUpMoney.start();
    	
    	URL npcUpUrl = Control.class.getResource("/Image/npc1-walkup.gif");
    	npcWalkup = new Image(npcUpUrl.toExternalForm());
    	URL npcRightUrl = Control.class.getResource("/Image/npc1-walkright.gif");
    	npcWalkright = new Image(npcRightUrl.toExternalForm());
    	URL npcDownUrl = Control.class.getResource("/Image/npc1-walkdown.gif");
    	npcWalkdown = new Image(npcDownUrl.toExternalForm());
    	URL npcLeftUrl = Control.class.getResource("/Image/npc1-walkleft.gif");
    	npcWalkleft = new Image(npcLeftUrl.toExternalForm());
    	for (Node node : gridPane.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (col == null) col = 0;
            if (row == null) row = 0;
            cellMap.put(new Pair<>(col, row), node);
    	}
    	
    	allPath = cellMap.size();
    	
    	Thread checkProgress = new Thread(() -> {
    		int cell = (WIDTH_WINDOW/CELL_SIZE)*(HEIGHT_WINDOW/CELL_SIZE);
    		while (true) {
    			if (cell == cellMap.size()) {
    				endGame();
    			}
    			try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}); 
    	checkProgress.setDaemon(true);
    	checkProgress.start();
    	
    }

	private void npcAppearHandle() {
    	npcs = new ArrayList<>();
    	
    	synchronized (inCreate) {
			
    		for (int i=0; i<npcMax; i++) {
    			try {
    				System.out.println("Reading NPC position...");
    				double[] npcPosition = (double[]) inCreate.readObject();
    				Thread handleNPC = new Thread(() -> {
    					handleNpcPosition(npcPosition);
    				});
    				handleNPC.setDaemon(true);
    				handleNPC.start();
    				System.out.println("NPC position received.");
    			} catch (ClassNotFoundException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
		
    	}
    	
    	startListenerThread();
	}
    
    private void handleNpcPosition(double[] npcPosition) {
        System.out.println("Received NPC position: " + npcPosition[1] + ", " + npcPosition[2] + ". Entrance: " + npcPosition[0]);

        int entrance = (int) npcPosition[0];
        int direction;
        URL npcURL;

        switch (entrance) {
            case 0:
            case 1:
                npcURL = Control.class.getResource("/Image/npc1-walkdown.gif");
                direction = 2;
                break;
            case 2:
            case 3:
                npcURL = Control.class.getResource("/Image/npc1-walkright.gif");
                direction = 1;
                break;
            case 4:
            case 5:
                npcURL = Control.class.getResource("/Image/npc1-walkup.gif");
                direction = 0;
                break;
            case 6:
            case 7:
                npcURL = Control.class.getResource("/Image/npc1-walkleft.gif");
                direction = 3;
                break;
            default:
                npcURL = Control.class.getResource("/Image/npc1-walkup.gif");
                direction = 0;
                break;
        }

        Image npcGIF = new Image(npcURL.toExternalForm());

        ImageView npc = new ImageView(npcGIF);
        npc.setFitWidth(10);
        npc.setFitHeight(18);
        npc.setX(npcPosition[1]);
        npc.setY(npcPosition[2]);
        
        
        Platform.runLater(() -> npcPane.getChildren().add(npc));
        
        npcs.add(npc);
        NPCdirection(npc, direction);
    }
    
    private void NPCdirection(ImageView npc, int direction) {
    	final int[] currentDirection = {direction};
    	try {
    		// 0-walkup 1-walkright 2-walkdown 3-walkleft
    		moveNPCInDirection(npc, currentDirection[0], CELL_SIZE/2);
    		
    		
    		// 0-walkup 1-walkright 2-walkdown 3-walkleft
    		boolean inBuilding = false;
    		final boolean[] finalInBuiding = {inBuilding};
    		while (true) {
    			boolean nextIsBuilding = false;
    			double npcX = npc.getX();
    			double npcY = npc.getY();
    			System.out.println("npcx: "+npcX+", npcy: "+npcY);
    			
    			int colIndex = (int) (npcX / gridPane.getWidth() * gridPane.getColumnCount());
    			int rowIndex = (int) (npcY / gridPane.getHeight() * gridPane.getRowCount());
    			
    			System.out.println(colIndex + " " + rowIndex);
    			
    			int rightCellCol = colIndex + 1;
    			int rightCellRow = rowIndex;
    			
    			int bottomCellCol = colIndex;
    			int bottomCellRow = rowIndex + 1;
    			
    			int leftCellCol = colIndex - 1;
    			int leftCellRow = rowIndex;
    			
    			int upperCellCol = colIndex;
    			int upperCellRow = rowIndex - 1;
    			
    			ImageView rightCell = getImageCell(rightCellCol, rightCellRow);
    			ImageView bottomCell = getImageCell(bottomCellCol, bottomCellRow);
    			ImageView leftCell = getImageCell(leftCellCol, leftCellRow);
    			ImageView upperCell = getImageCell(upperCellCol, upperCellRow);
    			
    			
    			List<ImageView> existCell = new ArrayList<>();
    			if (rightCell != null) {existCell.add(rightCell);
    				System.out.println("right notnull");
    			}
    			if (bottomCell != null) {existCell.add(bottomCell);
    				System.out.println("bottom notnull");
    			}
    			if (leftCell != null) {existCell.add(leftCell);
    				System.out.println("left notnull");
    			}
    			if (upperCell != null) {existCell.add(upperCell);
    				System.out.println("upper notnull");
    			}
    			
    			switch (currentDirection[0]) {
    			case 0:
    				if (isEqualImg(colIndex, rowIndex, "/Image/item1.png")) {
    					System.out.println("In building");
    					break;
    				} else {
    					existCell.remove(bottomCell); 
    					System.out.println("remove bottom");
    					break;    					
    				}
    			case 1:
    				if (isEqualImg(colIndex, rowIndex, "/Image/item1.png")) {
    					System.out.println("In building");
    					break;
    				} else {
	    				existCell.remove(leftCell);
	    				System.out.println("remove left");
	    				break;
    				}
    			case 2:
    				if (isEqualImg(colIndex, rowIndex, "/Image/item1.png")) {
    					System.out.println("In building");
    					break;
    				} else {
	    				existCell.remove(upperCell);
	    				System.out.println("remove upper");
	    				break;
    				}
    			case 3:
    				if (isEqualImg(colIndex, rowIndex, "/Image/item1.png")) {
    					System.out.println("In building");
    					break;
    				} else {
	    				existCell.remove(rightCell);
	    				System.out.println("remove right");
	    				break;
    				}
    			}
    			
    			int exist = existCell.size();
    			System.out.println(exist);
    			
    			int pathColIndex = -1;
    			int pathRowIndex = -1;
    			if (exist==0) {
    				Semaphore semaphore5 = new Semaphore(0);
        			Platform.runLater(() -> {
        				Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
        					moveNPC(npc, currentDirection[0]);
        				}));
        				timeline.setCycleCount(CELL_SIZE);
        	            timeline.setOnFinished(event -> semaphore5.release()); // Khi Timeline hoàn thành, giải phóng Semaphore
        				timeline.play();
        			});
                    semaphore5.acquire(); // Chờ cho Semaphore được giải phóng
    			} else if (exist==1) {
    				ImageView path = existCell.get(0);
    				if (GridPane.getRowIndex(path) != null & GridPane.getColumnIndex(path) != null) {
    					pathColIndex = GridPane.getColumnIndex(path);
        				pathRowIndex = GridPane.getRowIndex(path);
        				System.out.println(pathColIndex + " " + pathRowIndex);
        				
        				// 0-walkup 1-walkright 2-walkdown 3-walkleft
        				if (colIndex > pathColIndex) {
        					npc.setImage(npcWalkleft);
        					currentDirection[0] = 3;
        				}
        				else if (colIndex < pathColIndex) {
        					currentDirection[0] = 1;
        					npc.setImage(npcWalkright);
        				}
        				else if (rowIndex > pathRowIndex) {
        					currentDirection[0] = 0;
        					npc.setImage(npcWalkup);
        				}
        				else if (rowIndex < pathRowIndex) {
        					currentDirection[0] = 2;
        					npc.setImage(npcWalkdown);
        				}
    				}
    			}
    			else {
    				ImageView path;
    				if (condition == 1) {
    					System.out.println("existCell: " + existCell.size());
    					outDecision.writeInt(exist);
    					outDecision.flush();
    					System.out.println("send exist");
    					
    					//error
    					int ran = inDecision.readInt();
    					System.out.println("ran: " + ran);
    					path = existCell.get(ran);
    					System.out.println(ran);
//    				Random random = new Random();
//    				int ran = random.nextInt(exist);
//    				ImageView path = existCell.get(ran);
    					
    				} else {
    					int ran = inDecision.readInt();
    					System.out.println("ran: " + ran);
    					path = existCell.get(ran);
    					System.out.println(ran);
    				}
    				
    				
    				if (GridPane.getRowIndex(path) != null & GridPane.getColumnIndex(path) != null) {
    					pathColIndex = GridPane.getColumnIndex(path);
        				pathRowIndex = GridPane.getRowIndex(path);
        				System.out.println(pathColIndex + " " + pathRowIndex);
        				
        				if (isEqualImg(pathColIndex, pathRowIndex, "/Image/item1.png")) {
        					nextIsBuilding = true;
        				}
        				
        				// 0-walkup 1-walkright 2-walkdown 3-walkleft
        				if (colIndex > pathColIndex) {
        					npc.setImage(npcWalkleft);
        					currentDirection[0] = 3;
        				}
        				else if (colIndex < pathColIndex) {
        					currentDirection[0] = 1;
        					npc.setImage(npcWalkright);
        				}
        				else if (rowIndex > pathRowIndex) {
        					currentDirection[0] = 0;
        					npc.setImage(npcWalkup);
        				}
        				else if (rowIndex < pathRowIndex) {
        					currentDirection[0] = 2;
        					npc.setImage(npcWalkdown);
        				}
    				}
    			}
//    			TimeUnit.MILLISECONDS.sleep(500);
    			if ((nextIsBuilding == false && finalInBuiding[0] == false) || (nextIsBuilding == true && finalInBuiding[0] == true)) {
    				
    				moveNPCInDirection(npc, currentDirection[0], CELL_SIZE);    	
    				
    			} else if (nextIsBuilding == true && finalInBuiding[0] == false) {
    				int finalPathColIndex = pathColIndex;
    			    int finalPathRowIndex = pathRowIndex;
    				Semaphore semaphore = new Semaphore(0);
    		        Platform.runLater(() -> {
    		            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
    		                moveNPC(npc, currentDirection[0]);
    		                npc.setOpacity(Math.max(0, npc.getOpacity() - 0.03));
    		            }));
    		            timeline.setCycleCount(CELL_SIZE);
    		            timeline.setOnFinished(event -> {
    		            	ImageView nextItem = getImageCell(finalPathColIndex, finalPathRowIndex);
    		            	if (nextItem.getId().equals(clientId)) {
	    		            	money+=50;
	    		            	moneyLabel.setText(money+"");    		            		
    		            	}
    		            	semaphore.release();
    		            });
    		            timeline.play();
    		            finalInBuiding[0] = true;
    		        });
    		        try {
    		            semaphore.acquire();
    		        } catch (InterruptedException e) {
    		            Thread.currentThread().interrupt();
    		        }
    			} else if (nextIsBuilding == false && finalInBuiding[0] == true) {
    				Semaphore semaphore = new Semaphore(0);
    		        Platform.runLater(() -> {
    		            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
    		                moveNPC(npc, currentDirection[0]);
    		                npc.setOpacity(Math.min(1, npc.getOpacity() + 0.02));
    		            }));
    		            timeline.setCycleCount(CELL_SIZE);
    		            timeline.setOnFinished(event -> semaphore.release());
    		            timeline.play();
    		            finalInBuiding[0] = false;
    		        });
    		        try {
    		            semaphore.acquire();
    		        } catch (InterruptedException e) {
    		            Thread.currentThread().interrupt();
    		        }
    			}
    			
    		}
    	} catch (Exception e) {
    		System.out.println("Error updating UI: " + e.getMessage());
    		e.printStackTrace();
    	}
    	
    }
    
    private void updateMoney(long money) {
    	Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            moneyLabel.setText(money+"");
            semaphore.release();
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void moveNPCInDirection(ImageView npc, int direction, int count) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
                moveNPC(npc, direction);
            }));
            timeline.setCycleCount(count);
            timeline.setOnFinished(event -> semaphore.release());
            timeline.play();
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void moveNPC(ImageView npc, int direction) {
    	
    	switch (direction) {
    	case 0 -> npc.setY((npc.getY() - 1 + HEIGHT_WINDOW) % HEIGHT_WINDOW); // walk up
        case 1 -> npc.setX((npc.getX() + 1) % WIDTH_WINDOW); // walk right
        case 2 -> npc.setY((npc.getY() + 1) % HEIGHT_WINDOW); // walk down
        case 3 -> npc.setX((npc.getX() - 1 + WIDTH_WINDOW) % WIDTH_WINDOW); // walk left
    	}
    	
 	}    

	@FXML
    private void toggleItemBar(MouseEvent event) {
    	displayItemBar();
    }
    
    private void displayItemBar() {
    	TranslateTransition transitionItemBar = new TranslateTransition(Duration.millis(300), itemBar);
    	TranslateTransition transitionItemBtn = new TranslateTransition(Duration.millis(300), toggleItemBtn);
    	RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), toggleItemBtn);

        if (isBarVisible) {
        	transitionItemBar.setToY(0);  // Trượt thanh lên
        	transitionItemBtn.setToY(0);
        	rotateTransition.setByAngle(180);
        } else {
        	transitionItemBar.setToY(55);  // Trượt thanh xuống
        	transitionItemBtn.setToY(55);
        	rotateTransition.setByAngle(180); // Rotate by 360 degrees
        }
        transitionItemBar.play();
        transitionItemBtn.play();
        rotateTransition.play();
        isBarVisible = !isBarVisible;  // Đảo trạng thái của thanh item
    }
    
    @FXML
    void display1Info(MouseEvent event) {
    	int colIndex = (int) (event.getSceneX() / gridPane.getWidth() * gridPane.getColumnCount());
    	int rowIndex = (int) (event.getSceneY() / gridPane.getHeight() * gridPane.getRowCount());
    	
    	itemName.setText("Apartment");
    	itemCost.setText("Cost: "+item1Cost+" Coin");
    	itemIncomeAuto.setText("Income: 0.5 Coin/s");
    	itemIncomeClick.setText("1 Coin per Click");
    	
    	itemInfoTable.setLayoutX(CELL_SIZE*colIndex+CELL_SIZE/2-itemInfoTable.getPrefWidth()/2);
    	itemInfoTable.setLayoutY(CELL_SIZE*rowIndex+CELL_SIZE/2-itemInfoTable.getPrefHeight()/2+5);
    	
    	itemInfoTable.toFront();
    	itemInfoTable.setVisible(true);
    	
    	KeyValue opaque = new KeyValue(itemInfoTable.opacityProperty(), 1);
    	KeyValue yTrans = new KeyValue(itemInfoTable.layoutYProperty(), CELL_SIZE*rowIndex+CELL_SIZE/2-itemInfoTable.getPrefHeight()/2+5+68);
    	
    	KeyFrame transItemInfo = new KeyFrame(Duration.millis(300), opaque, yTrans);
    	Timeline displayIteminfo = new Timeline(transItemInfo);
    	displayIteminfo.play();
    }
    
    @FXML
    void unDisplay1Info(MouseEvent event) {
    	int rowIndex = (int) (event.getSceneY() / gridPane.getHeight() * gridPane.getRowCount());
    	
    	KeyValue opaque = new KeyValue(itemInfoTable.opacityProperty(), 0);
    	KeyValue yTrans = new KeyValue(itemInfoTable.layoutYProperty(), CELL_SIZE*rowIndex+CELL_SIZE/2-itemInfoTable.getPrefHeight()/2+5);
    	
    	KeyFrame transItemInfo = new KeyFrame(Duration.millis(300), opaque, yTrans);
    	Timeline displayIteminfo = new Timeline(transItemInfo);
    	displayIteminfo.play();
    	displayIteminfo.setOnFinished(e -> {
    		itemInfoTable.setLayoutX(0);
    		itemInfoTable.setLayoutY(-itemInfoTable.getPrefHeight());
    		itemInfoTable.setVisible(false);    		
    	});
    }
    
    @FXML
    private void startItem1Handle(MouseEvent event) {
    	if (money >= 100) {
    		startImageFollowing(event, "/Image/item1Move.png", "/Image/item1.png");    		
    	} else {
    		showErr("You don't have enough coins.");
    	}
    }

    private void startImageFollowing(MouseEvent event, String pathMove, String pathInsert) {
    	paneMove = new AnchorPane();
    	itemMove = new ImageView();
    	disableMove = new ImageView();
    	
    	Image moveImage = new Image(Control.class.getResourceAsStream(pathMove));
        itemMove.setImage(moveImage);
        itemMove.setFitWidth(60);
        itemMove.setFitHeight(60);
        
    	Image disableimage = new Image(Control.class.getResourceAsStream("/Image/disable.png"));
    	disableMove.setImage(disableimage);
    	disableMove.setFitWidth(50);
    	disableMove.setFitHeight(50);
        
    	paneMove.getChildren().add(disableMove);
        paneMove.getChildren().add(itemMove);
        stackPane.getChildren().add(paneMove);
        
        itemMove.setVisible(true);
    	itemMove.setLayoutX((WIDTH_WINDOW / 2) - (itemMove.getFitWidth() / 2));
    	itemMove.setLayoutY((HEIGHT_WINDOW / 2) - (itemMove.getFitHeight() / 2));
    	
    	disableMove.setVisible(true);
    	disableMove.setLayoutX(60*10 +5);
    	disableMove.setLayoutY(5);

        // Bắt đầu theo dõi con trỏ chuột
    	System.out.println("check1");
    	
    	itemMove.setOnMousePressed(mouseEvent -> {
            xOffset = mouseEvent.getSceneX() - itemMove.getLayoutX();
            yOffset = mouseEvent.getSceneY() - itemMove.getLayoutY();
        });

        itemMove.setOnMouseDragged(mouseEvent -> {
            itemMove.setLayoutX(mouseEvent.getSceneX() - xOffset);
            itemMove.setLayoutY(mouseEvent.getSceneY() - yOffset);
        });
        
        itemMove.setOnMouseReleased(mouseEvent -> {
        	// Di chuyển ImageView vào vị trí của ô grid và cố định nó
        	System.out.println("check3");
            int colIndex = (int) (mouseEvent.getSceneX() / gridPane.getWidth() * gridPane.getColumnCount());
            int rowIndex = (int) (mouseEvent.getSceneY() / gridPane.getHeight() * gridPane.getRowCount());
            
            System.out.println(colIndex);
            ImageView insert = new ImageView();
            
            URL insertUrl = Control.class.getResource(pathInsert);
            Image imageIn = new Image(insertUrl.toExternalForm());
            insert.setImage(imageIn);
            insert.setFitHeight(60);
            insert.setFitWidth(60);
            
            if (!isCellOccupied(colIndex, rowIndex)) {
            	insert.setId(clientId);
            	item1s.add(insert);
            	//
            	try {
                    String urlSend = insert.getImage().getUrl();
                    outCreate.writeObject(urlSend);
                    outCreate.flush();
                    outCreate.writeObject(clientId);
                    outCreate.flush();
                    outCreate.writeObject(new int[]{colIndex, rowIndex});
                    outCreate.flush();

                    System.out.println("Sentttttttttttttttttttt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            	//
            	String musicFile = getClass().getResource("/Media/buildingSound.wav").toString();
        		Media sound = new Media(musicFile);
        		MediaPlayer mediabuild = new MediaPlayer(sound);
        		
        		mediabuild.play();
        		
            	gridPane.add(insert, colIndex, rowIndex);
            	money = money-item1Cost ;
            	moneyLabel.setText(money+"");
            	cellMap.put(new Pair<Integer, Integer>(colIndex, rowIndex), insert);
            }
            else {
            	insert = null;
            	showErr("There's already a structure, can't build!");
            }
            
            paneMove.getChildren().remove(disableMove);
            disableMove = null;
            paneMove.getChildren().remove(itemMove);
            itemMove = null;
            stackPane.getChildren().remove(paneMove);
            paneMove = null;
        });
        
        disableMove.setOnMouseClicked(mouseEvent -> {
        	paneMove.getChildren().remove(disableMove);
            disableMove = null;
            paneMove.getChildren().remove(itemMove);
            itemMove = null;
            stackPane.getChildren().remove(paneMove);
            paneMove = null;
        });
    }
    
    private void startListenerThread() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (inCreate) {
                        Object urlObjReceive = inCreate.readObject();
                        Object idObjReceive = inCreate.readObject();
                        Object coorObjReceive = inCreate.readObject();
                        
                        System.out.println("Receiveeeeeeeeeeeeeeeee");
                        if (urlObjReceive.equals("Exit")) {
                        	checkwin = (String) idObjReceive;
                        	endGame();
                        }
                        else if (!urlObjReceive.equals("Item Sent")) {
                            System.out.println("Got itttttttt");
                            String urlReceive = (String) urlObjReceive;
                            String idReceive = (String) idObjReceive; 
                            int[] coorReceive = (int[]) coorObjReceive;

                            Platform.runLater(() -> {
                                ImageView itemReceive = new ImageView(urlReceive);
                                itemReceive.setId(idReceive);
                                itemReceive.setFitWidth(60);
                                itemReceive.setFitHeight(60);

                                gridPane.add(itemReceive, coorReceive[0], coorReceive[1]);
                                cellMap.put(new Pair<>(coorReceive[0], coorReceive[1]), itemReceive);
                            });
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });

        listenerThread.setDaemon(true); // Allows the thread to exit when the application exits
        listenerThread.start();
    }
    
    private void showErr(String msg) {
        Label errorMessage = new Label(msg);
        errorMessage.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        errorMessage.getStyleClass().add("error-label");

        StackPane.setAlignment(errorMessage, Pos.BOTTOM_CENTER);
        stackPane.getChildren().add(errorMessage);
        
        // Fade out the error message after a delay
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), errorMessage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> stackPane.getChildren().remove(errorMessage));
        fadeOut.play();
    }
    
    @FXML
    private void zoomBuilding(MouseEvent event) {
    	int colIndex = (int) (event.getSceneX() / gridPane.getWidth() * gridPane.getColumnCount());
    	int rowIndex = (int) (event.getSceneY() / gridPane.getHeight() * gridPane.getRowCount());
    	
    	ImageView inside = zoomHandleIn(colIndex, rowIndex, "/Image/item1.png", "/Image/hotel.gif");
    	if (inside!=null) {
	    	
	    	zoomOutImg.setOnMouseClicked(mouseEvent -> {
	        	zoom=false;
	        	toggleItemBtn.setDisable(false);
	        	
	        	KeyValue kvReverseGridScaleX = new KeyValue(gridPane.scaleXProperty(), 1);
	            KeyValue kvReverseGridScaleY = new KeyValue(gridPane.scaleYProperty(), 1);
	            KeyValue kvReverseGridTranslateX = new KeyValue(gridPane.translateXProperty(), 0);
	            KeyValue kvReverseGridTranslateY = new KeyValue(gridPane.translateYProperty(), 0);
	            
	            KeyValue kvReverseAnchorScaleX = new KeyValue(npcPane.scaleXProperty(), 1);
	            KeyValue kvReverseAnchorScaleY = new KeyValue(npcPane.scaleYProperty(), 1);
	            KeyValue kvReverseAnchorTranslateX = new KeyValue(npcPane.translateXProperty(), 0);
	            KeyValue kvReverseAnchorTranslateY = new KeyValue(npcPane.translateYProperty(), 0);
	            
	            KeyValue kvReverseInsideFitWidth = new KeyValue(inside.fitWidthProperty(), 43);
	            KeyValue kvReverseInsideFitHeight = new KeyValue(inside.fitHeightProperty(), 50);
	            KeyValue kvReverseInsideLayoutX = new KeyValue(inside.layoutXProperty(), CELL_SIZE*colIndex+CELL_SIZE/2-43/2);
	            KeyValue kvReverseInsideLayoutY = new KeyValue(inside.layoutYProperty(), CELL_SIZE*rowIndex+CELL_SIZE/2-50/2);
	            
	            KeyValue kvReverseOpacity = new KeyValue(paneMove.opacityProperty(), 0);
	            
	            KeyFrame krf = new KeyFrame(Duration.seconds(1), kvReverseGridScaleX, kvReverseGridScaleY, kvReverseGridTranslateX, kvReverseGridTranslateY,
	            		kvReverseAnchorScaleX, kvReverseAnchorScaleY, kvReverseAnchorTranslateX, kvReverseAnchorTranslateY,
	            		kvReverseInsideFitWidth, kvReverseInsideFitHeight, kvReverseInsideLayoutX, kvReverseInsideLayoutY, 
	            		kvReverseOpacity);
	            Timeline reverseTimeline = new Timeline(krf);
	            reverseTimeline.play();
	            reverseTimeline.setOnFinished(e -> {
	            	inside.setVisible(false);
	            	paneMove.getChildren().remove(inside);
	            	paneMove.getChildren().remove(zoomOutImg);
	            	zoomOutImg = null;
	            	stackPane.getChildren().remove(paneMove);
	            	paneMove = null;
	            });
	            
	        });
    	}
    }
    
    private ImageView zoomHandleIn(int colIndex, int rowIndex, String pathItem, String pathOpen) {
    	Node node = cellMap.get(new Pair<>(colIndex, rowIndex));
    	if (node instanceof ImageView) {
    		
    		ImageView check = (ImageView) node;
    		URL itemUrl = Control.class.getResource(pathItem);
            Image imageItem = new Image(itemUrl.toExternalForm());
            
    		if (check.getImage().getUrl().equals(imageItem.getUrl()) && !zoom) {
    			zoom = true;
    			
    			paneMove = new AnchorPane();
    			ImageView inside;
    			if (items.get(node)==null) {
    				inside = new ImageView();
    				URL openUrl = Control.class.getResource(pathOpen);
    				Image imageOpen = new Image(openUrl.toExternalForm());
    				inside.setImage(imageOpen);   
    				inside.setOnMouseClicked(event -> {
    					if (check.getId().equals(clientId))
    						coinFly(event);
    				});
    				
    				items.put(node, inside);
    			} else {
    				inside = items.get(node);
    			}
    			
    			inside.setVisible(true);
        		
        		toggleItemBtn.setDisable(true);
        		if (isBarVisible) {
                    displayItemBar();
                }
        		KeyValue kvGridScaleX = new KeyValue(gridPane.scaleXProperty(), scale);
                KeyValue kvGridScaleY = new KeyValue(gridPane.scaleYProperty(), scale);
                KeyValue kvGridTranslateX = new KeyValue(gridPane.translateXProperty(), CELL_SIZE * (gridPane.getColumnCount() / 2 - colIndex) * scale);
                KeyValue kvGridTranslateY = new KeyValue(gridPane.translateYProperty(), CELL_SIZE * (gridPane.getRowCount() / 2 - rowIndex) * scale);
                
                KeyValue kvAnchorScaleX = new KeyValue(npcPane.scaleXProperty(), scale);
                KeyValue kvAnchorScaleY = new KeyValue(npcPane.scaleYProperty(), scale);
                KeyValue kvAnchorTranslateX = new KeyValue(npcPane.translateXProperty(), CELL_SIZE * (gridPane.getColumnCount() / 2 - colIndex) * scale);
                KeyValue kvAnchorTranslateY = new KeyValue(npcPane.translateYProperty(), CELL_SIZE * (gridPane.getRowCount() / 2 - rowIndex) * scale);
                
                inside.setFitWidth(43);
                inside.setFitHeight(50);
                inside.setLayoutX(CELL_SIZE*colIndex+CELL_SIZE/2-43/2);
                inside.setLayoutY(CELL_SIZE*rowIndex+CELL_SIZE/2-43/2);
                KeyValue kvInsideFitWidth = new KeyValue(inside.fitWidthProperty(), inside.getFitWidth() * scale);
                KeyValue kvInsideFitHeight = new KeyValue(inside.fitHeightProperty(), inside.getFitHeight() * scale);
                KeyValue kvInsideLayoutX = new KeyValue(inside.layoutXProperty(), WIDTH_WINDOW/2 - inside.getFitWidth()/2 * scale);
                KeyValue kvInsideLayoutY = new KeyValue(inside.layoutYProperty(), HEIGHT_WINDOW/2 - inside.getFitHeight()/2 * scale);
                
                // Tạo KeyFrame cho hiệu ứng zoom
                KeyFrame kf = new KeyFrame(Duration.seconds(1), kvGridScaleX, kvGridScaleY, kvGridTranslateX, kvGridTranslateY,
                		kvAnchorScaleX, kvAnchorScaleY, kvAnchorTranslateX, kvAnchorTranslateY,
                		kvInsideFitWidth, kvInsideFitHeight, kvInsideLayoutX, kvInsideLayoutY);

                Image disableimage = new Image(Control.class.getResourceAsStream("/Image/disable.png"));
                zoomOutImg = new ImageView(disableimage);
                zoomOutImg.setFitWidth(60);
                zoomOutImg.setFitHeight(60);
            	
                paneMove.getChildren().add(zoomOutImg);
                zoomOutImg.setLayoutX(WIDTH_WINDOW-zoomOutImg.getFitWidth()-10);
                zoomOutImg.setLayoutY(HEIGHT_WINDOW-zoomOutImg.getFitHeight()-10);
                
                paneMove.getChildren().add(inside);
                stackPane.getChildren().add(paneMove);
                // Tạo Timeline và chơi hiệu ứng
                Timeline timeline = new Timeline(kf);
                timeline.play();
                
                return inside;
    		} else return null;
    	} else return null;
    }
    
    @FXML
    void exitButton(MouseEvent event) {
    	surrender();
    }
    
    private void surrender() {
    	// Create the alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Surrender");
        alert.setHeaderText("End Game?");
        alert.setContentText("Are you sure you want to proceed?");
        alert.initOwner(stackPane.getScene().getWindow());

        // Add buttons
        ButtonType buttonConfirm = new ButtonType("Confirm");
        ButtonType buttonCancel = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonConfirm, buttonCancel);

        // Show the alert and wait for a response
        Optional<ButtonType> result = alert.showAndWait();

        // Handle the response
        if (result.isPresent() && result.get() == buttonConfirm) {
            System.out.println("User choose to Confirm");
            // Add your confirmation logic here
            try {
				outCreate.writeObject("Exit");
				outCreate.flush();
				outCreate.writeObject(clientId);
				outCreate.flush();
				outCreate.writeObject(new int[] {0,0});
				outCreate.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
            System.out.println("User choose to Cancel or closed the dialog");
            // Add your cancellation logic here
        }
    }
    
    private void endGame() {
    	
    	int win=0, count=0;
    	boolean draw=false;
    	for (ImageView structure : item1s) {
    		if (structure.getId()==clientId) count++;
    	}
    	if (count > ((cellMap.size()-allPath) - count*(NOPs-1))) win++;
    	else if (count == ((cellMap.size()-allPath) - count*(NOPs-1))) draw=true;
    	System.out.println(count);
    	System.out.println(((cellMap.size()-allPath) - count*(NOPs-1)));
    	
    	if (!checkwin.equals(clientId)) {
    		System.out.println(checkwin);
    		System.out.println(clientId);
    		win = 0;
    		win++;
    	} else {
    		win = 0;
    		draw = false;
    	}
    	
    	try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Bắt đầu một transaction
            session.beginTransaction();

            // Lấy đối tượng UserAccount từ cơ sở dữ liệu bằng username
            UserAccount userAccount = session.get(UserAccount.class, username);

            // Commit transaction
//            session.getTransaction().commit();
            if (userAccount != null) {
                // Cập nhật các thuộc tính cần thiết
                userAccount.setHighestCoin(Math.max(money, userAccount.getHighestCoin()));
                System.out.println(Math.max(money, userAccount.getHighestCoin()));
                userAccount.setTotalCoin(money + userAccount.getTotalCoin());
                System.out.println(money + userAccount.getTotalCoin());
                userAccount.setWinTime(win + userAccount.getWinTime());
                System.out.println(win + userAccount.getWinTime());

                // Lưu đối tượng đã cập nhật
                session.update(userAccount);

            }
            
            session.getTransaction().commit();
            
            toXML();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	Semaphore semaphore = new Semaphore(0);
        	if (win==0) {
        		Platform.runLater(() -> {
        			showAlert("End Game", "Defeat", "Thank you for playing", AlertType.INFORMATION);        		
        			semaphore.release();
        		});
        	}
        	else if (win==1) {
        		Platform.runLater(() -> {    
        			showAlert("End Game", "Victory", "Thank you for playing", AlertType.INFORMATION);
        			semaphore.release();
        		});
        	}
        	else if (draw) {
        		Platform.runLater(() -> {
        			showAlert("End Game", "Draw", "Thank you for playing", AlertType.INFORMATION);
        			semaphore.release();
        		});
        	}
        	
        	try {
        		inCreate.close();
        		outCreate.close();
				socketCreate.close();
				inDecision.close();
				outDecision.close();
				socketDecision.close();
				semaphore.acquire();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
        	Platform.exit();
        	System.exit(0);
		}
    	
	}
    
    private void toXML() {
    	File XML = createFile("src/main/resources/logGame", ".xml");
    	List<UserAccount> users = getData();
    	
    	//XML
    	try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("users");
            doc.appendChild(rootElement);

            for (UserAccount user : users) {
                // User element
                Element userElement = doc.createElement("user");
                rootElement.appendChild(userElement);

                // Username element
                Element username = doc.createElement("username");
                username.appendChild(doc.createTextNode(user.getUsername()));
                userElement.appendChild(username);

                // HighestCoin element
                Element highestCoin = doc.createElement("highestCoin");
                highestCoin.appendChild(doc.createTextNode(user.getHighestCoin().toString()));
                userElement.appendChild(highestCoin);

                // TotalCoin element
                Element totalCoin = doc.createElement("totalCoin");
                totalCoin.appendChild(doc.createTextNode(user.getTotalCoin().toString()));
                userElement.appendChild(totalCoin);

                // WinTime element
                Element winTime = doc.createElement("winTime");
                winTime.appendChild(doc.createTextNode(user.getWinTime().toString()));
                userElement.appendChild(winTime);
            }

            // Write the content into XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(XML);

            transformer.transform(source, result);

            System.out.println("File saved to " + XML.getAbsolutePath());

        } catch (ParserConfigurationException | TransformerException eDom) {
        	eDom.printStackTrace();
        }
    }
    
    private List<UserAccount> getData() {
    	try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			Query<UserAccount> query = session.createQuery("From UserAccount", UserAccount.class);
			List<UserAccount> allUser = query.list();

			session.getTransaction().commit();
			
			return allUser;
		} catch (HibernateException e) {
			showAlert("Database Error", "An error occurred while accessing the database.", e.getMessage(), AlertType.ERROR);
		}
    	return null;
    }
    
    public static File createFile(String dirpath, String fileExtension) {
    	try {
			File dir = new File(dirpath);
			
			if (!dir.exists()) {
				boolean dirCreated = dir.mkdirs();
				if (dirCreated) {
					System.out.println("Directory created successfully: " + dir.getAbsolutePath());
				} else {
					System.err.println("Failed to create directory: " + dir.getAbsolutePath());
					return null;
				}
			}
			
			boolean success = false;
			int i=0;
			
			while (!success) {
				String filename = "log" + i + fileExtension;
				File fileXML = new File(dir, filename);
				success = fileXML.createNewFile();
				
				if (success) {
					System.out.println("File created successfully: " + fileXML.getAbsolutePath());
					return fileXML;
				} else {
					i++;
					System.out.println("File already exists: " + fileXML.getAbsolutePath());
				}
			}
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
            e.printStackTrace();
        }
		return null;
    }
    
    private void coinFly(MouseEvent event) {
    	money++;
    	moneyLabel.setText(money+"");
		Image imageCoin = new Image(Control.class.getResourceAsStream("/Image/coin.png"));
		ImageView coinUp = new ImageView(imageCoin);
		coinUp.setFitHeight(50); coinUp.setFitWidth(50);
		
		KeyValue flyUp = new KeyValue(coinUp.layoutYProperty(), coinUp.getLayoutY() + coinUp.getFitHeight()*3);
		KeyValue blur = new KeyValue(coinUp.opacityProperty(), 0);
		
		KeyFrame coinFlyUp = new KeyFrame(Duration.seconds(1), flyUp, blur);
		
		coinUp.setLayoutX(event.getSceneX() - coinUp.getFitWidth()/2);
		coinUp.setLayoutY(event.getSceneY() - coinUp.getFitHeight()/2);
		System.out.println(coinUp.getLayoutX() + " " + coinUp.getLayoutY());
		paneMove.getChildren().add(coinUp);
		Timeline coinTimeline = new Timeline(coinFlyUp);
		coinTimeline.play();
		coinTimeline.setOnFinished(e -> {
			paneMove.getChildren().remove(coinUp);			
		});
    }
    
    private boolean isCellOccupied(int colIndex, int rowIndex) {
    	Node node = cellMap.get(new Pair<>(colIndex, rowIndex));
        return node instanceof ImageView;
    }
    
    private ImageView getImageCell(int colIndex, int rowIndex) {
        Pair<Integer, Integer> key = new Pair<>(colIndex, rowIndex);
        if (cellMap.containsKey(key)) {
            Node node = cellMap.get(key);
            if (node instanceof ImageView) {
                return (ImageView) node;
            }
        }
        return null;
    }
    
    public boolean isEqualImg(int colIndex, int rowIndex, String imageUrl) {
        ImageView imageView = getImageCell(colIndex, rowIndex);
        if (imageView != null) {
            Image image = imageView.getImage();
            URL itemUrl = Control.class.getResource(imageUrl);
            Image imageItem = new Image(itemUrl.toExternalForm());
            if (image != null && image.getUrl() != null && image.getUrl().equals(imageItem.getUrl())) {
                return true;
            }
        }
        return false;
    }
    
    private void showAlert(String title, String header, String content, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.initOwner(stackPane.getScene().getWindow());
		alert.showAndWait();
	}

}
