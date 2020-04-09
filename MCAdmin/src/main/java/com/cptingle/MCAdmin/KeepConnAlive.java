package com.cptingle.MCAdmin;

import java.sql.Connection;
import java.sql.Statement;

public class KeepConnAlive implements Runnable {
	private Connection conn;
	
	public KeepConnAlive(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void run() {
		try {
			Statement st = conn.createStatement();
			st.executeQuery("SELECT * FROM mca_player_log");
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
