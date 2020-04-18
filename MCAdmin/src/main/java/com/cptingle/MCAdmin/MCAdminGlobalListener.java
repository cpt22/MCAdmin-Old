package com.cptingle.MCAdmin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cptingle.MCAdminItems.PlayerUpdate;

public class MCAdminGlobalListener implements Listener {

	private MCAdmin plugin;
	//private Connection conn;
	//private WebInterface wi;
	
	public MCAdminGlobalListener(MCAdmin plugin) {
		this.plugin = plugin;
		//this.conn = plugin.getConnection();
		//this.wi = plugin.getWeb();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void playerJoin(PlayerJoinEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "join", true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerKick(PlayerKickEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "kick", false);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		storeEvent(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), "quit", false);
	}
	
	private void storeEvent(String uuid, String username, String eventType, boolean onlineStatus) {
		plugin.getClient().send(new PlayerUpdate(username, uuid, onlineStatus));
	}
}
