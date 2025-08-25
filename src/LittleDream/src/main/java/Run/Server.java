package Run;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import Controller.Control;

public class Server extends Thread {
	private static final int SIZE = 900;
	private List<CreateHandler> createHandlers = new ArrayList<>();
//	private List<DecisionHandler> decisionHandlers = new ArrayList<>();
	private List<double[]> npcs = new ArrayList<>();
	private List<ObjectOutputStream> chooseOuts = new ArrayList<>();
	private List<ObjectInputStream> chooseIns = new ArrayList<>();
	private int NOPs =2;
	private int npcMax = 1;
	private int number = NOPs;
	private int choose, ran;
	private int i=0;

	public void initServer() {
		
		for (int i = 0; i <= npcMax; i++)
			npcs.add(NPCAppear());
		
		try (ServerSocket serverSocketCreateNPC = new ServerSocket(1111);
				ServerSocket serverSocketDecisionNPC = new ServerSocket(2222)) {
            System.out.println("Server is listening on port " + 1111);
            System.out.println("Server is listening on port " + 2222);
            
            //
            while (true) {
            	Thread camp = null;
                try {
                	Socket socketCreate = serverSocketCreateNPC.accept();
                    System.out.println("Client join socketCreate");
                    
                    ObjectOutputStream outCreate = new ObjectOutputStream(new BufferedOutputStream(socketCreate.getOutputStream()));
                    outCreate.flush();
                    ObjectInputStream inCreate = new ObjectInputStream(new BufferedInputStream(socketCreate.getInputStream()));
                    
                    CreateHandler createHandler = new CreateHandler(socketCreate, outCreate, inCreate);
//                    createHandler.start();
                    createHandlers.add(createHandler);
                    
                    Socket socketDecision = serverSocketDecisionNPC.accept();
                    System.out.println("Client join socketDecision");
                    
                    ObjectInputStream inDecision = new ObjectInputStream(new BufferedInputStream(socketDecision.getInputStream()));
                    chooseIns.add(inDecision);
                    ObjectOutputStream outDecision = new ObjectOutputStream(new BufferedOutputStream(socketDecision.getOutputStream()));
                    outDecision.flush();
                    chooseOuts.add(outDecision);
                    
//                    DecisionHandler decisionHandler = new DecisionHandler(socketDecision, inDecision, outDecision);
//                    decisionHandler.start();;
//                    decisionHandlers.add(decisionHandler);
                    
                    
					camp = new Thread(() -> {
						try {
							System.out.println("Test");
							synchronized (inCreate) {
								String check = inCreate.readUTF();
								System.out.println(check);
								if (check.equals("ready")) {
									i++;
				                    sendNOPs(i);
				                    System.out.println("Send");
									number--;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}); camp.start();
                    
                } catch (Exception e) {
                	e.printStackTrace();
                }
                
                camp.join();                	
                
				if (number == 0) {
					System.out.println("All players are ready");
					break;
				}
            }
            //
            
            for (CreateHandler  handler : createHandlers) {
            	handler.start();
            	System.out.println("active create");
            }
            
            TimeUnit.SECONDS.sleep(4);
            Thread thread = new Thread(() -> {
            	
            	try {
	            	Random random = new Random();
	            	choose = random.nextInt(chooseOuts.size());
	            	ObjectOutputStream outChoose = chooseOuts.get(choose);
	            	synchronized (outChoose) {
	            		outChoose.writeInt(1);
	            		outChoose.flush();						
					}
	            	for (ObjectOutputStream otherOut : chooseOuts) {
	            		if (otherOut != outChoose) {
	            			synchronized (outChoose) {
	            				otherOut.writeInt(0);
	            				otherOut.flush();								
							}
	            		}
	            	}
	            	
	            	ObjectInputStream inChoose = chooseIns.get(choose);
	            	while (!Thread.currentThread().isInterrupted()) {
	            		
	            		System.out.println("Dec check");
	            		synchronized (inChoose) {
	            			int exist = inChoose.readInt();
	            			System.out.println("exist: " + exist);							
	            			ran = random.nextInt(exist);
	            			System.out.println("ran: " + ran);
						}
	            		
	            		
	            		for (ObjectOutputStream all : chooseOuts) {
	            			synchronized (all) {
	            				all.writeInt(ran);
	            				all.flush();								
							}
	            		}
	            		
	            	}
            	} catch (IOException e) {
            		e.printStackTrace();
            	}
            	
            }); thread.start();
            
//            for (DecisionHandler  handler : decisionHandlers) {
//            	handler.start();
//            	System.out.println("active decision");
//            }
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
	
	private void sendNOPs(int i) {
		for (CreateHandler handler : createHandlers) {
			try {
				ObjectOutputStream outCreate = handler.getOut();
				synchronized (outCreate) {
					outCreate.writeInt(i);
					outCreate.flush();					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private class CreateHandler extends Thread {
	    private Socket socket;
	    private ObjectOutputStream out;
	    private ObjectInputStream in;
		
		public CreateHandler(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
			this.socket = socket; this.out=out; this.in=in;
		}
		
		public ObjectOutputStream getOut() {
			return out;
		}
		
		@Override
		public void run() {
			try {
                System.out.println("Streams initialized for client " + socket.getRemoteSocketAddress());
                
                System.out.println("lock");
                synchronized (out) {
                	out.writeUTF("open");
                	out.flush();					
				}
                synchronized (in) {
                	System.out.println("open");
                	String str = in.readUTF();
                	System.out.println(str);					
				}
				
				synchronized (out) {
					for (int i = 0; i < npcMax; i++) {
						TimeUnit.SECONDS.sleep(3);
						// Create NPC at one of the 8 entrances
						double[] pos = npcs.get(i);
						out.writeObject(pos);	
						out.flush();
						
						System.out.println("NPC created at: (" + pos[1] + ", " + pos[2] + "). Entrance: " + pos[0]);
						System.out.println("npcs: " + npcMax);
						npcMax--;
					}					
				}
				
				Thread thread = new Thread(() -> {
						while (true) {
							try {
								String urlReceive;
								String idReceive;
								int[] coor;
								synchronized (in) {
									urlReceive = (String) in.readObject();
									idReceive = (String) in.readObject();
									coor = (int[]) in.readObject();									
								}
								if (urlReceive.equals("Exit")) {
									for (CreateHandler handler : createHandlers) {
										ObjectOutputStream output = handler.getOut();
										System.out.println("Sending to other");
										System.out.println("Sending to other2222");
										synchronized (output) {
											System.out.println("Sending to other clients: url=" + urlReceive + ", id=" + idReceive);
											output.writeObject(urlReceive);
											output.flush();
											output.writeObject(idReceive);
											output.flush();
											output.writeObject(coor);
											output.flush();
											System.out.println("Sent2222222222222222");
											
										}
									}
								} else {
									synchronized (out) {
										out.writeObject("Item Sent");
										out.flush();
										out.writeObject("ID Sent");
										out.flush();
										out.writeObject("Coor sent");
										out.flush();
									}
									System.out.println("Sent11111111111111111");
									for (CreateHandler handler : createHandlers) {
										ObjectOutputStream output = handler.getOut();
										System.out.println("Sending to other");
										if (output != out) {
											System.out.println("Sending to other2222");
											synchronized (output) {
												System.out.println("Sending to other clients: url=" + urlReceive + ", id=" + idReceive);
												output.writeObject(urlReceive);
												output.flush();
												output.writeObject(idReceive);
												output.flush();
												output.writeObject(coor);
												output.flush();
												System.out.println("Sent2222222222222222");
											}
										}
									}									
								}
							} catch (ClassNotFoundException | IOException e) {
								e.printStackTrace();
								closeAll();
								break;
							}
						}	
				}); thread.start();
				
			} catch (IOException | InterruptedException e) {
				System.out.println("Client leaved");
				e.printStackTrace();
				closeAll();
			}
		}
		private void closeAll() {
			try {
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
                createHandlers.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		
	}
	
	private double[] NPCAppear() {
		Random random = new Random();
        double startX, startY;
        int entrance = random.nextInt(8);
        
        int x1Point = 260, y1Point = 260;
        int x2Point = 620, y2Point = 620;
        
        switch (entrance) {
            case 0: // Top left
                startX = x1Point;
                startY = 0;
                break;
            case 1: // Top right
                startX = x2Point;
                startY = 0;
                break;
            case 2: // Left top
                startX = 0;
                startY = y1Point;
                break;
            case 3: // Left bottom
                startX = 0;
                startY = y2Point;
                break;
            case 4: // Bottom left
                startX = x1Point;
                startY = SIZE;
                break;
            case 5: // Bottom right
                startX = x2Point;
                startY = SIZE;
                break;
            case 6: // Right top
                startX = SIZE;
                startY = x1Point;
                break;
            case 7: // Right bottom
                startX = SIZE;
                startY = y2Point;
                break;
            default: // Bottom left
            	startX = x1Point;
                startY = SIZE;
                break;
        }
        
        return new double[]{entrance,startX, startY};
	}
	
	private static void logConsole() {
		Thread logging = new Thread(() -> {
			try {
				File log = Control.createFile("src/main/resources/logConsoleServer", ".txt");
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
        new Server().initServer();
    }

}
