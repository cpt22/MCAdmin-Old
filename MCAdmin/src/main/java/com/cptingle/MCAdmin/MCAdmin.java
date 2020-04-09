package com.cptingle.MCAdmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.cptingle.MCAdmin.commands.CommandHandler;
import com.cptingle.MCAdmin.database.Connect;
import com.cptingle.MCAdmin.database.KeepConnAlive;
import com.cptingle.MCAdmin.util.RandomToken;
import com.cptingle.MCAdmin.web.WebInterface;

public class MCAdmin extends JavaPlugin {
	// Connection
	private Connect connection;

	// Website
	private WebInterface wi;

	// Commands
	private CommandHandler ch;
	
	// Tokens
	private RandomToken tokenizer;

	// Configuration
	private File configFile;
	private FileConfiguration config;

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {
		// Initialize config
		configFile = new File(getDataFolder(), "config.yml");
		config = new YamlConfiguration();
		reloadConfig();

		// Set config headers
		// getConfig().options().header(getHeader());
		// saveConfig();
		
		tokenizer = new RandomToken(100);

		// Create connection
		connection = new Connect(this, config);
		wi = new WebInterface();

		// Register event listeners
		registerListeners();
		ch = new CommandHandler(this);
		registerCommands();

		// Announcement enablement
		getLogger().info("v" + this.getDescription().getVersion() + " enabled.");

		loadOnlineUsers();
		
		KeepConnAlive kca = new KeepConnAlive(connection.getConnection());
		BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, kca, 12000L, 36000);
	}

	@Override
	public void onDisable() {
		clearOnlineUsers();

		// Close Connection
		connection.close();
		getLogger().info("disabled");
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}
	
	public RandomToken getTokenizer() {
		return tokenizer;
	}

	/**
	 * Reloads configuration file and its contained information
	 */
	@Override
	public void reloadConfig() {
		// Check for existence of config file
		if (!configFile.exists()) {
			getLogger().info("No config file found, restoring from default...");
			;
			saveDefaultConfig();
		}

		// Check for tab characters in config-file
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(getDataFolder(), "config.yml")));
			int row = 0;
			String line;
			while ((line = in.readLine()) != null) {
				row++;
				if (line.indexOf('\t') != -1) {
					StringBuilder buffy = new StringBuilder();
					buffy.append("Found tab in config-file on line ").append(row).append(".");
					buffy.append('\n').append("NEVER use tabs! ALWAYS use spaces!");
					buffy.append('\n').append(line);
					buffy.append('\n');
					for (int i = 0; i < line.indexOf('\t'); i++) {
						buffy.append(' ');
					}
					buffy.append('^');
					throw new IllegalArgumentException(buffy.toString());
				}
			}

			// Actually reload the config-file
			config.load(configFile);
		} catch (InvalidConfigurationException e) {
			throw new RuntimeException(
					"\n\n>>>\n>>> There is an error in your config-file! Handle it!\n>>> Here is what snakeyaml says:\n>>>\n\n"
							+ e.getMessage());
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Config-file could not be created for some reason! <o>");
		} catch (IOException e) {
			// Error reading the file, just re-throw
			getLogger().severe("There was an error reading the config-file:\n" + e.getMessage());
		} finally {
			// Java 6 <3
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
	}

	public Connection getConnection() {
		return connection.getConnection();
	}

	public WebInterface getWeb() {
		return this.wi;
	}

	/**
	 * Save configuration file
	 */
	@Override
	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Register all listeners
	private void registerListeners() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new PushGlobalListener(this), this);
	}
	
	// Register all commands
	private void registerCommands() {
		// Main plugin commands
		getCommand("mca").setExecutor(ch);
		getCommand("mcadmin").setExecutor(ch);
		
		// Standalone commands
	}

	/*
	 * public void addCommand(String cmd) {
	 * getCommand(cmd).setExecutor(commandHandler); }
	 */

	// Permissions stuff
	public boolean has(Player p, String s) {
		return p.hasPermission(s);
	}

	public boolean has(CommandSender sender, String s) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		}
		return has((Player) sender, s);
	}

	public void loadOnlineUsers() {
		try {
			Statement st = getConnection().createStatement();
			for (Player p : this.getServer().getOnlinePlayers()) {
				st.executeUpdate("INSERT INTO mca_players (uuid, username, status) VALUES " 
						+ "('" + p.getUniqueId().toString() + "','" + p.getName() + "', 1) ON DUPLICATE KEY UPDATE status=1");
			}

			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearOnlineUsers() {
		try {
			Statement st = getConnection().createStatement();
			st.executeUpdate("UPDATE mca_players SET status=0");

			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
