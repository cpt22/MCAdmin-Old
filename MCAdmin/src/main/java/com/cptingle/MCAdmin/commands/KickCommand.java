package com.cptingle.MCAdmin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdmin.messaging.MSG;
import com.cptingle.MCAdminItems.KickRequest;

import net.md_5.bungee.api.ChatColor;

public class KickCommand implements CommandExecutor {
	private MCAdmin plugin;

	public KickCommand(MCAdmin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("mcadmin.kick")) {
			sender.sendMessage(MSG.NO_PERMISSION.toString());
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Error: No Player Specified!");
			return true;
		}

		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			sender.sendMessage(MSG.PLAYER_NOT_FOUND.toString());
			return true;
		}

		if (label.equalsIgnoreCase("kick")) {
			String message = "You have been kicked!";

			if (p.isOp() && !(sender instanceof ConsoleCommandSender)) {
				sender.sendMessage(ChatColor.RED + "Error: You cant kick that player!");
				return true;
			}

			if (args.length > 1)
				message = allArgs(1, args);

			plugin.getClient()
					.send(new KickRequest(p.getName(), p.getUniqueId().toString(), message, sender.getName()));

			Bukkit.broadcast("Player " + p.getName() + " was kicked by " + sender.getName(), "mcadmin.kick");
			p.kickPlayer(message);
			
			return true;
		}
		return false;
	}

	public String allArgs(int start, String[] args) {
		String temp = "";
		for (int i = start; i < args.length; i++) {
			temp += args[i] + " ";
		}
		return temp.trim();
	}
}
