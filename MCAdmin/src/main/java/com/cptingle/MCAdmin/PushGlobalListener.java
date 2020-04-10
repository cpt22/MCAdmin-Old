package com.cptingle.MCAdmin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cptingle.MCAdmin.web.WebInterface;

public class PushGlobalListener implements Listener {

	private MCAdmin plugin;
	private Connection conn;
	private WebInterface wi;
	
	public PushGlobalListener(MCAdmin plugin) {
		this.plugin = plugin;
		this.conn = plugin.getConnection();
		this.wi = plugin.getWeb();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void playerJoin(PlayerJoinEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "join", 1);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerKick(PlayerKickEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "kick", 0);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "quit", 0);
	}
	
	private void storeEvent(String uuid, String username, String eventType, Integer onlineStatus) {
		try {
			Statement st = conn.createStatement();
			int res = st.executeUpdate("INSERT INTO mca_player_log (uuid, username, event) VALUES ('" + uuid + "','" + username + "','" + eventType + "')");
			
			st.executeUpdate("INSERT INTO mca_players (uuid, username, status) VALUES " 
					+ "('" + uuid + "','" + username + "', " + onlineStatus + ") ON DUPLICATE KEY UPDATE status=" + onlineStatus);
			
			st.close();
			
			
			Map<Object, Object> params = new HashMap<>();
			params.put("name", username);
			params.put("status", onlineStatus);
			
			if (eventType.equalsIgnoreCase("join") || eventType.equalsIgnoreCase("quit"))
				wi.sendPost("http://localhost/apns/sendEvent.php", params);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
