package com.cptingle.MCAdmin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdmin.messaging.MSG;

public class CommandHandler implements CommandExecutor {
	private MCAdmin plugin;
	//private Connection conn;
	//private WebInterface wi;

	public CommandHandler(MCAdmin plugin) {
		this.plugin = plugin;
		//this.conn = plugin.getConnection();
		//this.wi = plugin.getWeb();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("mca") || label.equalsIgnoreCase("mcadmin")) {
			if (args.length == 0) {
				sender.sendMessage("Running MCAdmin Version " + ChatColor.GREEN + MCAdmin.VERSION);
				return true;
			}
			
			if (args[0] != null && !args[0].equals("")) {
				String command = args[0];
				
				switch(command.toLowerCase()) {
				case "register":
					return registerCommand(sender, cmd, label, args);
				case "linkserver":
					return linkCommand(sender, cmd, label, args);
				default:
					break;
				}
			}
		}
		return false;
	}
	
	private boolean linkCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
	
	private boolean registerCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 2)
			return false;
		
		
		if (!plugin.has(sender, "mcadmin.register")) {
			sender.sendMessage(MSG.NO_PERMISSION.toString());
			return true;
		}
		
		String token = args[1];
		
		if (token.length() == 100) {
			plugin.getConfig().set("token", token);
			plugin.saveConfig();
			plugin.getClient().doRegistration(token);
		} else {
			return false;
		}
	
		return true;
	}
}
