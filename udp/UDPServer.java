/*
 * Created on 01-Mar-2016
 */
package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import common.MessageInfo;

public class UDPServer {

	// Timeout to receive a message, in milliseconds.
	private final static int timeout = 30000;

	private DatagramSocket recvSoc;
	private int totalMessages = -1;
	private int[] receivedMessages;
	private boolean close;

	private void run() {
		int	pacSize = 1024;
		byte[] pacData = new byte[pacSize];
		DatagramPacket pac;

		// Receive the messages and process them.
		// Timeout to ensure the program doesn't block forever.
		close = false;
		while(!close) {
			pac = new DatagramPacket(pacData, pacSize);
			try {
				recvSoc.setSoTimeout(timeout);
				recvSoc.receive(pac);
			} catch (SocketTimeoutException e) {
				System.out.println("Socket timeout exception: " +
													 e.getMessage());
				System.out.println("Quitting server.");
				System.exit(-1);
			} catch (IOException e) {
				System.out.println("Socket IOException: " +
													 e.getMessage());
				System.out.println("Quitting server.");
				System.exit(-1);
			}
			processMessage(new String(pac.getData()));
		}
	}

	public void processMessage(String data) {
		MessageInfo msg = null;
		
		// Use the data to construct a new MessageInfo object.
		try {
			msg = new MessageInfo(data);
		} catch (Exception e) {
			System.out.println("Message exception: " + e.getMessage());
		}

		// On receipt of first message, initialise the receive buffer
		// and update the totalMessages count.
		if (receivedMessages.length == 0) {
			totalMessages = msg.totalMessages;
			receivedMessages = new int[totalMessages];
			System.out.println("New set of message with " + totalMessages +
												 " messages.");
		}

		receivedMessages[msg.messageNum] = 1;
		System.out.println(msg.toString());

		// If this is the last expected message, then identify any
		// missing messages.
		if (msg.messageNum == totalMessages - 1) {
			close = true;
			for (int i = 0; i < receivedMessages.length; i++ ) {
				System.out.print(i + " ");
			}
			System.out.println();
		}
	}

	public UDPServer(int rp) {
		// Initialise UDP socket for receiving data.
		// Use port number passed from cmd.
		try {
			recvSoc = new DatagramSocket(rp);
		} catch (SocketException e) {
			System.out.println("Socket exception: " + e.getMessage());
			System.exit(-1);
		}

		// Done Initialisation.
		System.out.println("UDPServer ready");
	}

	public static void main(String args[]) {
		int	recvPort;

		// Get the parameters from command line.
		if (args.length < 1) {
			System.err.println("Arguments required: recv port");
			System.exit(-1);
		}
		recvPort = Integer.parseInt(args[0]);

		// Construct Server object and start it.
		UDPServer udpServer = new UDPServer(recvPort);
		udpServer.run();
	}

}
