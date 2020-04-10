package com.cptingle.MCAdmin.messaging;

import org.bukkit.ChatColor;

public enum MSG {
	NO_PERMISSION(ChatColor.RED + "You do not have permission to use this command."),
	ONLY_PLAYER(ChatColor.RED + "This command can only be run by players."),
	PLAYER_NOT_FOUND(ChatColor.RED + "Error: Player not found"),
	ERROR_GENERIC(ChatColor.RED + "There was an error executing this command");
	
	private final String msg;
	 
    MSG(String msg) {
        this.msg = msg;
    }
 
    public String toString() {
        return msg;
    }
}