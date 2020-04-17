package com.cptingle.MCAdmin.socket;

import com.cptingle.MCAdminItems.Token;

public class Client {
	
	// Network Stuff
	private NetworkListener nl;
	
	private String token;
	
	public Client(String token) {
		this.token = token;
		// Setup Network Listener
		nl = new NetworkListener(this);
	}
	
	public String getToken() {
		return token;
	}
	
	public void send(Object o) {
		nl.sendOutgoing(o);
	}

}
