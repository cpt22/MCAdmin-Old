package com.cptingle.MCAdmin.socket;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdminItems.BanRequest;
import com.cptingle.MCAdminItems.KickRequest;

public class Client {
	private MCAdmin plugin;
	
	// Network Stuff
	private NetworkListener nl;

	private String token;
	
	public Client(MCAdmin plugin, String token) {
		this.token = token;
		this.plugin = plugin;
		// Setup Network Listener
		nl = new NetworkListener(this);
	}
	
	public String getToken() {
		return token;
	}
	
	public NetworkListener getNetworkListener() {
		return nl;
	}
	
	public MCAdmin getPlugin() {
		return plugin;
	}
	
	public void send(Object o) {
		nl.send(o);
	}
	
	
	public boolean executeKick(KickRequest r) {
		Player p = Bukkit.getPlayer(r.uuid);
		
		if (p != null) {
			p.kickPlayer(r.reason);
			Bukkit.broadcast("Player " + r.username + " was kicked by " + r.executor, "mcadmin.kick");
			return true;
		}
		return false;
	}
	
	public boolean executeBan(BanRequest r) {
		Player p = Bukkit.getPlayer(r.uuid);
		String pName = "";
		String pUUID = "";
		if (p == null) {
			OfflinePlayer op = Bukkit.getOfflinePlayer(r.uuid);
			pName = op.getName();
			pUUID = op.getUniqueId().toString();
			//pOP = op.isOp();
		} else {
			pName = p.getName();
			pUUID = p.getUniqueId().toString();
			//pOP = p.isOp();
		}
		
		if (!pName.equals("")) {
			if (r.state) {
				Bukkit.getBanList(BanList.Type.NAME).addBan(pName, r.reason, null, null);
				Bukkit.broadcast("Player " + r.username + " was banned by " + r.executor, "mcadmin.ban");
			} else {
				Bukkit.getBanList(BanList.Type.NAME).pardon(pName);
				Bukkit.broadcast("Player " + r.username + " was unbanned by " + r.executor, "mcadmin.unban");
			}
			return true;
		}
		
		return false;
	}

}
