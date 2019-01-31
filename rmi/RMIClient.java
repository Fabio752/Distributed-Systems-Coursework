/*
 * Created on 01-Mar-2016
 */
package rmi;

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
	private final static int REGISTRY_PORT = 5000;

	// Example run: ./rmiclient.sh 127.0.1.1 200
	public static void main(String[] args) {
		// Check arguments for Server host and number of messages.
		if (args.length < 2){
			System.out.println("Needs 2 arguments: ServerHostName/IPAddress, TotalMessageCount");
			System.out.println("Quitting...");
			System.exit(-1);
		}

		String urlServer = new String("rmi://" + args[0] + "/RMIServer");
		int numMessages = Integer.parseInt(args[1]);

		// Initialise Security Manager.
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			// Get the registry located at the specified port on the server
			// and then bind a local object to the remote object.
			Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
			RMIServerI remoteObject = (RMIServerI)registry.lookup(urlServer);

			// Attempt to send messages the specified number of times.
			for (int i = 0; i < numMessages; i++) {
				remoteObject.receiveMessage(new MessageInfo(numMessages, i));
			}
		} catch (RemoteException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		} catch (NotBoundException e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		} catch(Exception e) {
			System.out.println(e);
			System.out.println("Quitting...");
			System.exit(-1);
		}
	}
}
