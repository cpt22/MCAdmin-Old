package com.cptingle.MCAdmin.commands;

import java.sql.SQLException;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdmin.messaging.MSG;

import net.md_5.bungee.api.ChatColor;

public class BanCommand implements CommandExecutor {
	
	private MCAdmin plugin;
	
	public BanCommand(MCAdmin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("mcadmin.ban") || !sender.hasPermission("mcadmin.unban")) {
			sender.sendMessage(MSG.NO_PERMISSION.toString());
			return true;
		}
		
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Error: No Player Specified!");
			return true;
		}
		
		Player p = Bukkit.getPlayer(args[0]);
		String pName = "";
		String pUUID = "";
		Boolean pOP = false;
		if (p == null) {
			OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
			pName = op.getName();
			pUUID = op.getUniqueId().toString();
			pOP = op.isOp();
		} else {
			pName = p.getName();
			pUUID = p.getUniqueId().toString();
			pOP = p.isOp();
		}
		
		if (pUUID.equals("")) {
			sender.sendMessage(MSG.PLAYER_NOT_FOUND.toString());
			return true;
		}
		
		if (label.equalsIgnoreCase("ban")) {
			String message = "You have been banned!";
			
			if (!sender.hasPermission("mcadmin.ban")) {
				sender.sendMessage(MSG.NO_PERMISSION.toString());
				return true;
			}
			
			if (pOP && !(sender instanceof ConsoleCommandSender)) {
				sender.sendMessage(ChatColor.RED + "Error: You cant ban that player!");
				return true;
			}
			
			if (args.length > 1)
				message = allArgs(1, args);
			
			Bukkit.getBanList(BanList.Type.NAME).addBan(pName, message, null, null);		
			Bukkit.broadcast("Player " + pName + " was banned by " + sender.getName(), "mcadmin.ban");
			if (p != null)
				p.kickPlayer(message);
			return true;
		} else if (label.equalsIgnoreCase("unban") || label.equalsIgnoreCase("pardon")) {
			if (!sender.hasPermission("mcadmin.unban")) {
				sender.sendMessage(MSG.NO_PERMISSION.toString());
				return true;
			}
			
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Error: No Player Specified!");
				return true;
			}
			
			Bukkit.getBanList(BanList.Type.NAME).pardon(pName);
			Bukkit.broadcast("Player " + pName + " was unbanned by " + sender.getName(), "mcadmin.unban");		
			return true;		
		}
			
		return false;
	}
	
	public String allArgs(int start , String[] args){
	    String temp = "";
	    for(int i = start ; i < args.length ; i++){
	     temp += args[i] + " "; 
	    }
	   return temp.trim();
	}

}
