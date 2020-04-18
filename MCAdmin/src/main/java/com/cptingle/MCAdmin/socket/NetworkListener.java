package com.cptingle.MCAdmin.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.cptingle.MCAdmin.exceptions.FailedConnectionException;
import com.cptingle.MCAdminItems.BanRequest;
import com.cptingle.MCAdminItems.KickRequest;
import com.cptingle.MCAdminItems.SimpleRequest;
import com.cptingle.MCAdminItems.Token;

public class NetworkListener extends Thread {
	/**
	 * Instance Variables
	 */
	private Client client;

	private static Socket socket;
	private static ObjectOutputStream outS;
	private static ObjectInputStream inS;

	private boolean connected;

	/**
	 * Constructors
	 */
	public NetworkListener(Client c) {
		this.client = c;
		this.connected = false;

		createConnection();

		this.start();
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean val) {
		this.connected = val;
	}

	/**
	 * Run method
	 */
	public void run() {
		while (client.getPlugin().getIsEnabled()) {
			while (!isConnected() || socket == null || outS == null || inS == null || socket.isClosed()) {

			}
			try {
				processIncoming(inS.readObject());
			} catch (IOException e) {
				// throw new DisconnectedFromServerException("");
				client.getPlugin().getLogger().warning("Disconnected from remote server");
				setConnected(false);
				break;
			} catch (ClassNotFoundException e) {
				client.getPlugin().getLogger()
						.severe("You are running an outdated version of MCAdmin, please update immediately!");
				break;
			}
		}
	}

	/**
	 * Connect to Server
	 * 
	 * @return
	 * @throws IOException
	 */
	public void createConnection() {
		new Thread(new NewConnection(client));
	}

	/**
	 * Process objects being read from object input stream
	 * 
	 * @param obj - incoming object
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
		} else if (obj instanceof KickRequest) {
			client.executeKick((KickRequest) obj);
		} else if (obj instanceof BanRequest) {
			client.executeBan((BanRequest) obj);
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

	private class NewConnection implements Runnable {

		private Client c;

		private OutputStream os;
		private InputStream is;

		public NewConnection(Client c) {
			this.c = c;
		}

		@Override
		public void run() {
			do {
				try {
					NetworkListener.socket = tryConnect();
				} catch (IOException e) {
					c.getPlugin().getLogger().severe("Stream Error");
					e.printStackTrace();
				} catch (FailedConnectionException e) {
					c.getPlugin().getLogger().warning(e.getMessage());
				}
			} while (socket == null && client.getPlugin().getIsEnabled());
		}

		/**
		 * Attempts to connect to server and open streams
		 * 
		 * @return
		 * @throws IOException
		 * @throws FailedConnectionException
		 */
		public Socket tryConnect() throws IOException, FailedConnectionException {
			Socket s = null;

			// Try to connect 10 times before failing
			int count = 0;
			while (s == null && count < 10) {
				s = makeConnection("mcadmin.xyz", 33233);
				count++;
			}

			// Exit if socket connection not made
			if (s == null) {
				throw new FailedConnectionException("Unable to connect to remote server");
			}

			os = s.getOutputStream();
			NetworkListener.outS = new ObjectOutputStream(os);
			is = s.getInputStream();
			NetworkListener.inS = new ObjectInputStream(is);

			return s;
		}

		/**
		 * Make socket connection
		 * 
		 * @param ip   - ip address to connect to server
		 * @param port - port server is running on
		 * @return newly made socket or null if socket not made
		 */
		public Socket makeConnection(String ip, int port) {
			try {
				return new Socket(ip, port);
			} catch (IOException e) {
				return null;
			}
		}

	}
}
