package network;

/**
 * class derived from the official Java documentation available at:
 * http://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
 * I have modified the program by removing the implicit try-with-resources block
 * and replacing it with a traditional try-catch block and a finally
 * block to explicitly free all the resources that were being freed implicitly before.
 */


import java.lang.Runnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.lang. Class;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.lang.Runnable;

public class Server {
	// default port for initial communication
	public static final int INITIAL_PORT = 2222;
	// default port for heart beat
	public static final int HEARTBEAT_PORT =3333; 
	//default initial host name
	public static String HOSTNAME = "127.0.0.1";
	
	// hashmap for storing info about clients
	// it maps a autogenerated key to all the information about a client
	public static HashMap<Integer,ClientInfo> clients; //= new HashMap<Integer, ClientInfo>();
	
	// location consisting of ip and port for each client
	public Location location;
	
	//get initial server port for communication
	public int getInitialPort(){
		return INITIAL_PORT;
	}
	
	//default constructor for Server
	public Server(String ip, int port){
		this.location = new Location(ip,port);
	}
	
	//this method gives a new int key for new clients in the HashMap
	public static int getNewClientKey(){
		Random rand = new Random();
		return 123456 + rand.nextInt(65321);
	}

	public static void main(String args[]) throws IOException{
		int portNumber = Server.INITIAL_PORT;  
		if (args.length != 1) {
	            System.err.println("No port specified. Using default port 2222");
	        }
		else
			portNumber = Integer.parseInt(args[1]);
			
		clients = new HashMap<Integer, ClientInfo>();
		
		// setup the hearbeat socket for the server that listens to clients 
		ServerSocket serverSocket1 = new ServerSocket(Server.HEARTBEAT_PORT);
		new Thread(new ReceiveHearBeats(serverSocket1)).start();
	        
			        try {
			        	// listen at initial connection port
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			            
			            while(true){
					            //accept a new client connection by listening to port
					            Socket clientSocket = serverSocket.accept();    
					            
					            //get a hashmap key for the new client
					            int key = 0;
					            while(true){
					            	key = getNewClientKey();
					            	if(!clients.containsKey(key))
					            		break; //break since we got a unique key					            	
					            }
					            
					            // read initial message containing client side receiving port 
							    // for other clients to contact this particular client 
					            PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
					            // open in stream
					            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
					            		clientSocket.getInputStream()));
					            String receiverPortInString = inputStream.readLine();
							    String[] port = receiverPortInString.split(" ");
							    //if the client did not inform me of its listening port
							    if(!port[0].equalsIgnoreCase("MyReceiver")){
							    	System.out.println("First Message is not not as excepted"+ receiverPortInString);
							    	System.exit(0);
							    }
							    outputStream.flush();
							    outputStream.println("YOURKEY "+key);
							    
							    int receiverPort = Integer.parseInt(port[1]);
					            
							    
					            //once client connects, create a output stream at the socket
					            Thread pp = new ClientHandler(clientSocket,key);
					            Server.clients.put(key,new ClientInfo(key, pp,clientSocket,receiverPort));
					            Server.displayClients();
					            pp.start();
					          
			            }
			            
			        } catch (IOException e) {
			            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			            System.out.println(e.getMessage());
			        }
			          catch (Exception e) {
			        	  System.out.println("Exception occured in Server. Trace:");
			        	System.out.print(e.getStackTrace() + e.toString());
			          }    
	} // end of main()
	
	// displays all the clients connected to the server 
	public static void displayClients(){
		
		System.out.println("\nTotal clients="+Server.clients.size());
		for(int i : Server.clients.keySet())
			System.out.println("Client " +i+": "+ Server.clients.get(i)+" Location: ip="+clients.get(i).location.ipAddress+" Connected to port="+clients.get(i).location.socketNumber+" listening on port="+clients.get(i).receiverPort+" Timestamp:"+clients.get(i).currenttimeInMillis);
	}
	
} // end of server class


//this class handles each client separately as a thread
class ClientHandler extends Thread{

	public DataInputStream inputStream=null;
	public PrintWriter printStream = null;
	public Socket clientSocket = null;
	private int threadIdentifier;
	
	public ClientHandler(Socket clientSocket, int clientId) {
	    this.clientSocket = clientSocket;
	    this.threadIdentifier = clientId;
	  }
	  
	@Override
	public void run() {
		try{
			//create streams for given socket
			inputStream = new DataInputStream(clientSocket.getInputStream());
		    printStream =  new PrintWriter(clientSocket.getOutputStream(), true);
		    
		    
		    while (true) {
		        String line = inputStream.readLine();
		        printStream.flush();
		        String words[]=line.split(" ");
		        System.out.println("Called by Client:"+ line);
		        //check if message from ProcessManager
		        if(words[0].equalsIgnoreCase("ProcessManager") && words.length>0){
		        	switch(Integer.parseInt(words[1])){
			        	case(StatusMessages.LIST_CLIENTS):
			        		System.out.println("Called by Message Manager: display clients below..");
			        		Server.displayClients();
			        		break;
			        	case(StatusMessages.LIST_PROCESSES):{
			        		break;
			        	}
			        	//case(StatusMessages.)
		        	}
		        }
		        //if not process manager, then it is a regular client
		        else{
			        if (line.startsWith("/quit")) {
			          break;
			        }
			        printStream.println("Received at Server: "+line);
		        }
		    }
		    //free allocated communication paths
		    inputStream.close();
			printStream.close();
			clientSocket.close();
		} catch(IOException e){
			System.out.println("Thread ended for client");
		}
		
	}
		
}

class ReceiveHearBeats extends Thread{
	
	ServerSocket heartbeatSocket;
	
	public ReceiveHearBeats(ServerSocket s){
		heartbeatSocket = s;
	}
	
	@Override
	public void run(){
		 
		
		while(true){
            //accept a new client connection by listening to port
			try {
				
				Socket clientSocket1 = heartbeatSocket.accept();
				// read heartbeat 
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(
						clientSocket1.getInputStream()));
				String receiverPortInString = inputStream.readLine();
				HandleHeartBeat hhb = new HandleHeartBeat(receiverPortInString);
				new Thread(hhb).start();
				clientSocket1.close();
			    
			}catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
				continue;
			}    
		} // end of while		
	}//end of run
}// end of Receive Heart Beats 


class HandleHeartBeat extends Thread {
	String hbMsg;
	java.util.Date currentDate = new java.util.Date();
	
	public HandleHeartBeat(String receiverPortInString) {
		hbMsg = receiverPortInString;
	}

	@Override
	public void run(){
		//split input to receive client key
		String[] keyArray = hbMsg.split(" ");
		//extract client key
	    if(!keyArray[0].equalsIgnoreCase("HEARTBEAT")||keyArray.length<2){
	    	System.out.println("First Message is not not as excepted"+ hbMsg);
	    	System.exit(0);
	    }
	    
	    int clientKey = Integer.parseInt(keyArray[1]);
        //TODO implement client handler after updating client info
	    if(!Server.clients.containsKey(clientKey)){
	    	System.out.println("Client with key "+clientKey+" does not exist");
	    	System.exit(0);
	    }
	    else{
	    	Server.clients.get(clientKey).currenttimeInMillis = currentDate.getTime();
	    }
	    
	}
	
	
}// end of handle heart beats