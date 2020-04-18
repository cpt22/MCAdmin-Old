package com.cptingle.MCAdmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.cptingle.MCAdmin.commands.BanCommand;
import com.cptingle.MCAdmin.commands.CommandHandler;
import com.cptingle.MCAdmin.socket.Client;
import com.cptingle.MCAdmin.util.RandomToken;
import com.cptingle.MCAdminItems.PlayerUpdate;
import com.cptingle.MCAdminItems.SimpleRequest;

public class MCAdmin extends JavaPlugin {

	private Client client;

	// Commands
	private CommandHandler ch;

	// Tokens
	private String serverToken;
	private RandomToken tokenizer;

	// Configuration
	private File configFile;
	private FileConfiguration config;
	
	// Other Vars
	private boolean enabled;

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {
		enabled = true;
		// Initialize config
		configFile = new File(getDataFolder(), "config.yml");
		config = new YamlConfiguration();
		reloadConfig();

		// Set config headers
		// getConfig().options().header(getHeader());
		// saveConfig();

		tokenizer = new RandomToken(100);
		serverToken = config.getString("token", "");

		client = new Client(this, serverToken);

		// Create connection
		// connection = new Connect(this, config);
		// wi = new WebInterface();

		// Register event listeners
		registerListeners();
		ch = new CommandHandler(this);
		registerCommands();

		// Announcement enablement
		getLogger().info("v" + this.getDescription().getVersion() + " enabled.");

		loadOnlineUsers();

		/*
		 * KeepConnAlive kca = new KeepConnAlive(connection.getConnection());
		 * BukkitScheduler scheduler = getServer().getScheduler();
		 * scheduler.scheduleSyncRepeatingTask(this, kca, 12000L, 36000);
		 */
	}

	@Override
	public void onDisable() {
		clearOnlineUsers();
		
		enabled = false;
		client = null;

		// Close Connection
		// connection.close();
		getLogger().info("disabled");
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}

	public RandomToken getTokenizer() {
		return tokenizer;
	}
	
	public boolean getIsEnabled() {
		return enabled;
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

	/*
	 * public Connection getConnection() { return connection.getConnection(); }
	 * 
	 * public WebInterface getWeb() { return this.wi; }
	 */

	public String getServerToken() {
		return this.serverToken;
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
		pm.registerEvents(new MCAdminGlobalListener(this), this);
	}

	// Register all commands
	private void registerCommands() {
		// Main plugin commands
		getCommand("mca").setExecutor(ch);
		getCommand("mcadmin").setExecutor(ch);

		// Standalone commands
		BanCommand bc = new BanCommand(this);
		getCommand("ban").setExecutor(bc);
		getCommand("unban").setExecutor(bc);
		getCommand("pardon").setExecutor(bc);
	}

	/*
	 * public void addCommand(String cmd) {
	 * getCommand(cmd).setExecutor(commandHandler); }
	 */

	public Client getClient() {
		return client;
	}

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
		for (Player p : this.getServer().getOnlinePlayers()) {
			client.send(new PlayerUpdate(p.getName(), p.getUniqueId().toString(), true));
		}
	}

	public void clearOnlineUsers() {
		client.send(SimpleRequest.CLEAR_ONLINE_PLAYERS);
	}

}
