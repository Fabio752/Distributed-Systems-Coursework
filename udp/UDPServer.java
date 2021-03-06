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
	private final static int timeout = 10000;
	private long startTime;
	private long endTime;

	private DatagramSocket recvSoc;
	private int totalMessages = -1;
	private int[] receivedMessages = null;
	private boolean close;

	private void run() {
		int	pacSize = 8192;
		byte[] pacData = new byte[pacSize];
		DatagramPacket pac;

		// Receive the messages and process them.
		// Timeout to ensure the program doesn't block forever.
		close = false;
		while(!close) {
			pacData = new byte[pacSize];
			pac = new DatagramPacket(pacData, pacSize);
			try {
				recvSoc.setSoTimeout(timeout);
				recvSoc.receive(pac);
			} catch (IOException e) {
				endTime = System.nanoTime();
				System.out.println(e);
				e.printStackTrace();
				System.out.println("Quitting...");
				printResults(/*timeout=*/true);
				System.exit(-1);
			}

			processMessage(new String(pac.getData()));
		}
	}

	public void processMessage(String data) {
		MessageInfo msg = null;
		// Use the data to construct a new MessageInfo object.
		try {
			msg = new MessageInfo(data.trim());
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.out.println("Quitting...");
			System.exit(-1);
		}

		// On receipt of first message, initialise the receive buffer
		// and update the totalMessages count.
		if (receivedMessages == null) {
			totalMessages = msg.totalMessages;
			receivedMessages = new int[totalMessages];
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
			close = true;
			printResults(/*timeout=*/false);
		}
	}

	private void printResults(boolean timeout) {
		double elapsedTime = (endTime - startTime) / 1000;
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
			"Received: " + (totalMessages - lostCount) + "/" +
			totalMessages + "  ->  " +
			(((Double.valueOf(totalMessages) - Double.valueOf(lostCount))/totalMessages)*100)
			+ "%");
		System.out.println(
			"Lost:     " + lostCount + "/" + totalMessages + "  ->  " + 
			((Double.valueOf(lostCount) / Double.valueOf(totalMessages))*100) + "%");
		
		// Time measurements.
		if (timeout) {
			System.out.println(
				"WARNING: following measurements are not reliable due to timeout.");
		}
		System.out.println(
			"Total time elapsed (micro sec): " +
			String.format("%.3f", elapsedTime));
		System.out.println(
			"Estimate time per package (micro sec): " +
			String.format("%.3f", elapsedTime / totalMessages));
	}

	public UDPServer(int rp) {
		// Initialise UDP socket for receiving data.
		// Use port number passed from cmd.
		try {
			recvSoc = new DatagramSocket(rp);
		} catch (SocketException e) {
			System.out.println(e);
			e.printStackTrace();
			System.out.println("Quitting...");
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
