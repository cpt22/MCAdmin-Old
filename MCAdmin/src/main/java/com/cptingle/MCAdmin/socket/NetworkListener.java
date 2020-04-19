package com.cptingle.MCAdmin.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
	private Queue<Object> disconnectedQueue;

	// Used to execute object send requests
	private ExecutorService executor;

	/**
	 * Constructors
	 */
	public NetworkListener(Client c) {
		this.client = c;
		this.connected = false;
		
		this.disconnectedQueue = new LinkedList<Object>();

		this.executor = Executors.newFixedThreadPool(2);

		this.setName("MCAdmin socket thread");
		this.start();
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean val) {
		this.connected = val;
		if (!val) {
			try {
				outS.close();
				inS.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {

			}
		} else {

		}
	}

	/**
	 * Run method
	 */
	public void run() {
		boolean hasLoggedConnectionError = false;
		while (client.getPlugin().getIsEnabled()) {
			while (!isConnected() || socket == null || outS == null || inS == null || socket.isClosed()) {
				try {
					socket = tryConnect();

					if (socket != null) {
						outS = new ObjectOutputStream(socket.getOutputStream());
						inS = new ObjectInputStream(socket.getInputStream());
						client.getPlugin().getLogger().info("Connected to remote server");
						setConnected(true);
						hasLoggedConnectionError = false;
						onConnect();
						break;
					}
					sleep(5000);
				} catch (IOException e) {
					client.getPlugin().getLogger().severe("Stream Error");
					setConnected(false);
					e.printStackTrace();
				} catch (FailedConnectionException e) {
					if (!hasLoggedConnectionError)
						client.getPlugin().getLogger().warning(e.getMessage());
					hasLoggedConnectionError = true;
					setConnected(false);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				processIncoming(inS.readObject());
			} catch (IOException e) {
				// throw new DisconnectedFromServerException("");
				client.getPlugin().getLogger().warning("Disconnected from remote server");
				setConnected(false);
			} catch (ClassNotFoundException e) {
				client.getPlugin().getLogger()
						.severe("You are running an outdated version of MCAdmin, please update immediately!");
				break;
			}
		}
		client.getPlugin().getLogger().severe("Something broke / Lost connection to server");
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
			case SERVER_VALIDATED:
				client.getPlugin().getLogger().info("Server validated, valid token");
			default:
				break;
			}
		} else if (obj instanceof KickRequest) {
			client.executeKick((KickRequest) obj);
		} else if (obj instanceof BanRequest) {
			client.executeBan((BanRequest) obj);
		}
	}
	
	public void onConnect() {
		while (disconnectedQueue.peek() != null) {
			send(disconnectedQueue.poll());
		}
	}

	/**
	 * 
	 */
	public void send(Object o) {
		if (isConnected()) {
			executor.submit(() -> {
				sendOutgoing(o);
			});
		} else {
			disconnectedQueue.offer(o);
		}
	}

	public void sendOutgoing(Object obj) {
		try {
			outS.writeObject(obj);
			outS.flush();
		} catch (IOException e) {
			client.getPlugin().getLogger().severe("Stream error");
			e.printStackTrace();
		}
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
		s = makeConnection("mcadmin.xyz", 33233);

		// Exit if socket connection not made
		if (s == null) {
			throw new FailedConnectionException("Unable to connect to remote server");
		}

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
