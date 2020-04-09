package com.cptingle.MCAdmin.database;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;

import com.cptingle.MCAdmin.MCAdmin;

public class Connect implements Closeable {
	
	private FileConfiguration config;
	private static Connection conn;
	private MCAdmin plugin;
	
	public Connect(MCAdmin plugin, FileConfiguration config) {
		
		this.config = config;
		this.conn = null;
		this.plugin = plugin;
		
		initializeConnection();
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	private void initializeConnection() {
		String url = "jdbc:mysql://192.168.0.76:3306/cwrumc";
        String user = "cwrumc";
        String password = "jnEwbK8glq7WlaNk";
        
        try {
        	conn = DriverManager.getConnection(url, user, password);
        	if (conn != null) {
        	}
        } catch (SQLException ex) {
		    System.out.println("An error occurred. Maybe user/password is invalid");
		    ex.printStackTrace();
		}
	}
	
	
	public void close() {
		if (conn != null) {
            try {
    			plugin.getLogger().info("Database connection closed");

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
	}
}
