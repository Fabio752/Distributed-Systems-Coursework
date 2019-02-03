/*
 * Created on 01-Mar-2016
 */
package rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import common.MessageInfo;

public class RMIClient {

	// Port for RMI registry on the server.
	// Must match the one used by the server.
	// Default is 1099.
	private final static int REGISTRY_PORT = 1099;

	// Example run: ./rmiclient.sh 127.0.1.1 objectNameonRegistry 200
	public static void main(String[] args) {
		// Check arguments for Server host and number of messages.
		if (args.length < 3){
			System.out.println(
				"Needs 2 arguments: ServerHostName/IPAddress, " +
				"RemoteObjectName, TotalMessageCount");
			System.out.println("Quitting...");
			System.exit(-1);
		}

		String serverAddress = args[0];
		String remoteObjectName = args[1];
		int numMessages = Integer.parseInt(args[2]);

		// Initialise Security Manager.
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			// Get the registry located at the specified port on the server
			// and then bind a local object to the remote object.
			RMIServerI remoteObject =
				(RMIServerI)Naming.lookup("//" + serverAddress + ":" +
																	REGISTRY_PORT + "/" +
																	remoteObjectName);

			// Attempt to send messages the specified number of times.
			for (int i = 0; i < numMessages; i++) {
				remoteObject.receiveMessage(new MessageInfo(numMessages, i));
			}
		} catch (RemoteException | NotBoundException | MalformedURLException e) {
			System.out.println(e);
			e.printStackTrace();
			System.out.println("Quitting...");
			System.exit(-1);
		}
	}
}
