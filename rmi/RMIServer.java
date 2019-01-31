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

	// Default port for RMI registry.
	private final static int REGISTRY_PORT = 1099; 

	private int totalMessages = -1;
	private int[] receivedMessages;

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
		}

		receivedMessages[msg.messageNum] = 1;
		System.out.print(msg.messageNum + "\t");

		// If this is the last expected message, then identify any
		// missing messages.
		if (msg.messageNum == totalMessages - 1) {
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
		}
	}

	protected static void rebindServer(String serverURL, RMIServer server) {
		try {
			// Create registry to bind remote objects to the client.
			// Served at the specified port.
			LocateRegistry.createRegistry(REGISTRY_PORT);

			// Bind the object created on the server to the entry in the
			// registry.
			// The name of the object is absolutely arbitrary. There is no
			// need to have rmi:// and /RMIServer. It could be whatever
			// string. The only constraints:
			// - it has to match the one in the client,
			// - the serverURL has to be a valid and usable address.
			Naming.rebind("rmi://" + serverURL + "/RMIServer", server);
		} catch (RemoteException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		} catch (MalformedURLException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		}
	}

	// Example run: ./rmiserver 127.0.1.1
	public static void main(String[] args) {
		// Check arguments for Server host and number of messages
		if (args.length != 1){
			System.out.println("Needs 1 arguments: ServerHostName/IPAddress");
			System.out.println("Quitting...");
			System.exit(-1);
		}

		// Initialise Security Manager.
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		// Instantiate the server class and bind to RMI registry.
		try {
			RMIServer rmiServer = new RMIServer();
			rebindServer(args[0], rmiServer);
		} catch (RemoteException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		}

		System.out.println("Starting server...");
	}
}
