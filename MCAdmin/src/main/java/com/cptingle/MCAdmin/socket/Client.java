package com.cptingle.MCAdmin.socket;

import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdminItems.BanRequest;
import com.cptingle.MCAdminItems.KickRequest;
import com.cptingle.MCAdminItems.Token;

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

	public void doRegistration(String token) {
		send(new Token(token));
	}

	public void send(Object o) {
		nl.send(o);
	}

	public void executeKick(KickRequest r) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ExecuteKick(r), 0);
	}

	public void executeBan(BanRequest r) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ExecuteBan(r), 0);
	}
	
	class ExecuteKick implements Runnable {
		private KickRequest r;
		public ExecuteKick(KickRequest r) {
			this.r = r;
		}
		
		public void run() {
			Player p = Bukkit.getPlayer(UUID.fromString(r.uuid));
			if (p != null) {
				p.kickPlayer(r.reason);
				Bukkit.broadcast("Player " + r.username + " was kicked by " + r.executor, "mcadmin.kick");
			}
		}
	}

	
	class ExecuteBan implements Runnable {
		private BanRequest r;
		public ExecuteBan(BanRequest r) {
			this.r = r;
		}
		
		public void run() {
			Player p = Bukkit.getPlayer(UUID.fromString(r.uuid));
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
					if (p != null)
						p.kickPlayer(r.reason);
				} else {
					Bukkit.getBanList(BanList.Type.NAME).pardon(pName);
					Bukkit.broadcast("Player " + r.username + " was unbanned by " + r.executor, "mcadmin.unban");
				}
			}
		}
	}

}
