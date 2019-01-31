/*
 * Created on 01-Mar-2016
 */
package rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import common.*;

public class RMIServer extends UnicastRemoteObject implements RMIServerI {

	// Port for RMI registry. Must match the one used by the client.
	// Default is 1099.
	private final static int REGISTRY_PORT = 1099; 

	private int totalMessages = -1;
	private int[] receivedMessages;
	private long startTime;
	private long endTime;

	public RMIServer() throws RemoteException {
		super();
	}

	public void receiveMessage(MessageInfo msg) throws RemoteException {
		// On receipt of first message, initialise the receive buffer.
		if (receivedMessages == null ||
				receivedMessages.length != msg.totalMessages) {
			totalMessages = msg.totalMessages;
			receivedMessages = new int[totalMessages];
			System.out.println("\n\n====================================");
			System.out.println("New set of message with " + totalMessages +
												 " messages.");
			// Start time measurment.
			startTime = System.nanoTime();
		}

		receivedMessages[msg.messageNum] = 1;
		System.out.print(msg.messageNum + "\t");

		// If this is the last expected message, then identify any
		// missing messages.
		if (msg.messageNum == totalMessages - 1) {
			// End time measurment.
			endTime = System.nanoTime();
			double elapsedTime = (endTime - startTime) / 1000000;

			int lostCount = 0;
			System.out.println("\nLost messages: ");
			for (int i = 0; i < receivedMessages.length; i++ ) {
				if (receivedMessages[i] == 0) {
					lostCount++;
					System.out.print(i + " ");
				}
			}
			System.out.println();
			System.out.println(
				"Received: " +  (totalMessages - lostCount) + "/" +
				totalMessages + "\t->  " + 
				(Double.valueOf((totalMessages - lostCount) / totalMessages)*100)
				+ "%");
			System.out.println(
				"Lost:     " + lostCount + "/" + totalMessages + "\t->  " + 
				(Double.valueOf(lostCount / totalMessages)*100) + "%");
			System.out.println(
				"Total time elapsed (ms): " + String.format("%.3f", elapsedTime));
			System.out.println(
				"Estimate time per package (ms): " +
				String.format("%.3f", elapsedTime / totalMessages));
		}
	}

	protected static void rebindServer(String remoteObjectNameOnRegistry,
																		 RMIServer remoteObject) {
		try {
			// Create registry to bind remote objects to the client.
			// The registry is on the server machine (accessible using local
			// or inet IP).
			// Served at the specified port.
			Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);

			// Bind the object created on the server to the entry in the
			// registry.
			registry.rebind(remoteObjectNameOnRegistry, remoteObject);
		} catch (RemoteException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		}
	}

	// Example run: ./rmiserver.sh objectNameonRegistry
	public static void main(String[] args) {
		// Check arguments for Server host and number of messages
		if (args.length != 1){
			System.out.println("Needs 1 argument: RemoteObjectName");
			System.out.println("Quitting...");
			System.exit(-1);
		}

		String remoteObjectNameOnRegistry = args[0];

		// Initialise Security Manager.
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		// Instantiate the server class and bind to RMI registry.
		try {
			RMIServer remoteObject = new RMIServer();
			rebindServer(remoteObjectNameOnRegistry, remoteObject);
		} catch (RemoteException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		}

		System.out.println("Starting server...");
	}
}
