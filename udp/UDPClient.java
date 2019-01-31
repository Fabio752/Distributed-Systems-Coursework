/*
 * Created on 01-Mar-2016
 */
package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import common.MessageInfo;

public class UDPClient {

	// Interval to sleep between two send requests to the server (ms).
	// Needed in order to not overload the receiving socket.
	private final int SLEEP_INTERVAL = 1;

	private DatagramSocket sendSoc;

	public static void main(String[] args) {
		InetAddress	serverAddr = null;
		int	recvPort;
		int countTo;

		// Get the parameters.
		if (args.length < 3) {
			System.err.println(
				"Arguments required: server name/IP, recv port, message count");
			System.exit(-1);
		}

		try {
			serverAddr = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println(
				"Bad server address in UDPClient, " + args[0] +
				" caused an unknown host exception " + e);
			System.exit(-1);
		}
		recvPort = Integer.parseInt(args[1]);
		countTo = Integer.parseInt(args[2]);

		// Construct UDP client class and try to send messages.
		UDPClient udpClient = new UDPClient();
		udpClient.testLoop(serverAddr, recvPort, countTo);
	}

	public UDPClient() {
		// Initialise the UDP socket for sending data.
		try {
			// Automatically select the port for the socket.
			sendSoc = new DatagramSocket();
		} catch(SocketException e) {
			System.out.println("Socket exception: " + e.getMessage());
			System.exit(-1);
		}
	}

	private void testLoop(InetAddress serverAddr, int recvPort,
												int countTo) {
		// Send the messages to the server.
		for (int tries = 0; tries < countTo; tries++) {
			String payload = countTo + ";" + tries;
			send(payload, serverAddr, recvPort);
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				System.out.println(e);
				System.out.println("Quitting...");
				System.exit(-1);
			}
		}
	}

	private void send(String payload, InetAddress destAddr,
										int destPort) {
		// Build the datagram packet and send it to the server.
		int payloadSize = payload.length();
		byte[] pktData = payload.getBytes();
		DatagramPacket pkt = new DatagramPacket(pktData, payloadSize, 
																						destAddr, destPort);
		try {
			sendSoc.send(pkt);
		} catch(IOException e) {
			System.out.println("Send exception: " + e.getMessage());
		}
	}
}
