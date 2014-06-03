package network;


import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;


public class ProcessHashMap implements Serializable {

	/**
	 * Autogenerated
	 */
	private static final long serialVersionUID = -5075224285871032747L;
	public int processId;
	public ConcurrentHashMap<Integer,ProcessInfo> processInfo;
	//
	public ProcessHashMap(){
		processInfo = new ConcurrentHashMap<Integer, ProcessInfo>();
	}

}



class ProcessInfo implements java.io.Serializable {
	/**
	 * Autogenerated
	 */
	private static final long serialVersionUID = 1432283007962668575L;
	public int processId; //number automatically generated by Server
	public int clientId; //client currently running this process 
	public String processName; //
	public Thread controllerThread;
	
}