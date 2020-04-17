package com.cptingle.MCAdmin.socket;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.cptingle.MCAdminItems.SimpleRequest;
import com.cptingle.MCAdminItems.Token;

public class NetworkListener extends Thread {
	/**
	 * Instance Variables
	 */
	private Client client;

	private Socket socket;
	private OutputStream os;
	private InputStream is;
	private ObjectOutputStream outS;
	private ObjectInputStream inS;

	/**
	 * Constructors
	 */
	public NetworkListener(Client c) {
		this.client = c;

		try {
			socket = connectToServer();

			// Start the thread this is running on
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.start();
	}

	/**
	 * Run method
	 */
	public void run() {
		while (true) {
			try {
				processIncoming(inS.readObject());
			} catch (EOFException e) {
				e.printStackTrace();
				System.out.println("Disconnected from server");
				break;
			}  catch (Exception e) {
				e.printStackTrace();
				System.out.println("Disconnected from server");
				break;
			}
		}
	}

	/**
	 * Connect to Server
	 * 
	 * @return
	 */
	public Socket connectToServer() throws Exception {
		Socket s = null;

		// Try to connect 10 times before failing
		int count = 0;
		while (s == null && count < 10) {
			s = makeConnection("mcadmin.xyz", 33233);
			count++;
		}

		// Exit if socket connection not made
		if (s == null) {
			System.out.println("COULD NOT CONNECT TO SERVER");
			System.exit(0);
		}

		os = s.getOutputStream();
		outS = new ObjectOutputStream(os);
		is = s.getInputStream();
		inS = new ObjectInputStream(is);

		/*outS.reset();
		outS.writeObject(new Open());
		outS.flush();*/

		return s;
	}

	/**
	 * Make socket connection
	 * 
	 * @param ip
	 *            - ip address to connect to server
	 * @param port
	 *            - port server is running on
	 * @return newly made socket or null if socket not made
	 */
	public static Socket makeConnection(String ip, int port) {
		try {
			return new Socket(ip, port);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Process objects being read from object input stream
	 * 
	 * @param obj
	 *            - incoming object
	 */
	public void processIncoming(Object obj) {
		if (obj instanceof SimpleRequest) {
			switch ((SimpleRequest) obj) {
			case SEND_TOKEN:
				client.send(new Token(client.getToken()));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 
	 */
	public boolean sendOutgoing(Object obj) {
		System.out.println("sending");
		try {
			outS.reset();
			outS.writeObject(obj);
			outS.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
