package com.cptingle.MCAdmin.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cptingle.MCAdmin.MCAdmin;
import com.cptingle.MCAdmin.messaging.MSG;
import com.cptingle.MCAdmin.web.WebInterface;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandHandler implements CommandExecutor {
	private MCAdmin plugin;
	private Connection conn;
	private WebInterface wi;

	public CommandHandler(MCAdmin plugin) {
		this.plugin = plugin;
		this.conn = plugin.getConnection();
		this.wi = plugin.getWeb();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("mca") || label.equalsIgnoreCase("mcadmin")) {

			if (args[0] != null && !args[0].equals("")) {
				String command = args[0];
				
				switch(command.toLowerCase()) {
				case "register":
					return registerCommand(sender, cmd, label, args);
				default:
					break;
				}
			}
			
			/*if (args[0] != null) {
				if (args[0].equalsIgnoreCase("register")) {
					if (sender instanceof Player) {
						if (args[1] != null && args[2] != null) {
							Player p = (Player) sender;
							String name = args[1];
							String token = args[2];
							String uuid = p.getUniqueId().toString();
							storePlayer(uuid, name, token);
							return true;
						}
					}
				}
			}*/
		}
		return false;
	}
	
	private boolean registerCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1)
			return false;
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(MSG.ONLY_PLAYER.toString());
			return true;
		}
		
		Player p = (Player) sender;
		
		if (!plugin.has(p, "mcadmin.register")) {
			p.sendMessage(MSG.NO_PERMISSION.toString());
			return true;
		}
		
		
		
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM mca_app_users WHERE uuid='" + p.getUniqueId().toString() + "'");
			
			if (!rs.next()) {
				String token = "";
				do {
					token = plugin.getTokenizer().nextString();
					ResultSet rs2 = st.executeQuery("SELECT * FROM mca_app_users WHERE app_link_token");
					if (!rs2.next()) {
						break;
					}
				} while (true);
				
				st.executeUpdate("INSERT INTO mca_app_users (uuid, app_link_token) VALUES ('" + p.getUniqueId().toString() + "', '" + token + "')");
				
				TextComponent msg = new TextComponent( token );
						msg.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, ("https://www.cwru.club/api/echo?token=" + token)));
						msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Click to copy" ).create() ) );
				
				p.sendMessage("You are now registered for the app\nClick below to take you to a page where you can copy the token:");
			    p.spigot().sendMessage(msg);
			} else {
				do {
					String token = rs.getString("app_link_token") + "";
					TextComponent msg = new TextComponent( token );
							msg.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, ("https://www.cwru.club/api/echo?token=" + token)));
							msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Click to copy" ).create() ) );
					
					p.sendMessage("You already are registered\nClick below to take you to a page where you can copy the token:");
				    p.spigot().sendMessage(msg);
			    } while (rs.next());
			}
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return true;
	}
	
	private boolean getAppCodeCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}

	private void storePlayer(String uuid, String name, String token) {
		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE mca_players SET name='" + name + "' WHERE name='" + name + "'" + 
					"IF @@ROWCOUNT=0" + 
					"INSERT INTO mca_players (uuid, token, name) VALUES ('" + uuid + "','" + token + "','" + name + "')";
			int res = st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
