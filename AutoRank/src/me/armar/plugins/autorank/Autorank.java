package me.armar.plugins.autorank;

import net.milkbowl.vault.Vault;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.logging.Logger;

public class Autorank extends JavaPlugin {

    private Logger log = Logger.getLogger("Minecraft");
    private Config config;
    private DataStorage data;
    private VaultHandler vault;
    private AutorankSaveData save;
    private Leaderboard leaderboard;
    private AutorankUpdateLeaderboard leaderboardUpdate;
    private boolean debug;
    private Timer timer;
    private RankChanger changer;

    public void onEnable() {

	// check if vault is present
	Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
	if (x != null & x instanceof Vault) {
	    vault = new VaultHandler(this);
	} else {
	    logMessage("[%s] Vault was _NOT_ found! Disabling autorank.");
	    getPluginLoader().disablePlugin(this);
	}

	// set up general config
	LinkedHashMap<String, Object> configDefaults = new LinkedHashMap<String, Object>();
	String configPath = this.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
	configDefaults.put("Enabled", false);
	configDefaults.put("Debug mode", false);
	configDefaults.put("Message prefix", "&2");
	configDefaults.put("Leaderboard layout", "&n - &tm");
	configDefaults.put("Essentials AFK integration", false);
	configDefaults.put("Update interval(minutes)", 5);
	configDefaults.put("Leaderboard update interval(minutes)", 30);
	configDefaults.put("Save interval(minutes)", 60);
	
	String[] availableGroups = vault.getGroups();
	for(int i = 0; i < availableGroups.length-1; i++){
	    String from = availableGroups[i];
	    String to = availableGroups[i+1];
	    
		configDefaults.put((i+1) + ".from", from);
		configDefaults.put((i+1) + ".to", to);
		configDefaults.put((i+1) + ".required minutes played", ((i+1)*200));
		configDefaults.put((i+1) + ".message", "&2Congratulations, you are now a " + to +".");
		configDefaults.put((i+1) + ".world", null);
		configDefaults.put((i+1) + ".commands", null);
	}
	config = new Config(this, configPath, configDefaults, "config");

	// set up player data
	data = new DataStorage(this);

	// schedule AutorankUpdate to be run
	int updateInterval = (Integer) config.get("Update interval(minutes)");
	timer = new Timer();
	timer.scheduleAtFixedRate(new AutorankUpdateData(this), updateInterval * 1000 * 60, updateInterval * 1000 * 60);

	// schedule AutorankSave to be run
	save = new AutorankSaveData(this);
	int saveInterval = 60 * 20 * (Integer) config.get("Save interval(minutes)");
	getServer().getScheduler().scheduleSyncRepeatingTask(this, save, saveInterval + 22, saveInterval);

	// make leaderboard
	leaderboard = new Leaderboard(this);

	// schedule AutorankUpdateLeaderboard to be run
	if (config.get("Leaderboard update interval(minutes)") == null) {
	    config.set("Leaderboard update interval(minutes)", 30);
	    logMessage("Updated config to include Leaderboard update interval");
	    getConf().save();
	}

	leaderboardUpdate = new AutorankUpdateLeaderboard(this);
	int leaderboardUpdateInterval = 60 * 20 * (Integer) config.get("Leaderboard update interval(minutes)");
	getServer().getScheduler().scheduleSyncRepeatingTask(this, leaderboardUpdate, 33, leaderboardUpdateInterval);

	if (config.get("Debug mode") != null) {
	    debug = (Boolean) config.get("Debug mode");
	}

	changer = new RankChanger(this);
	Bukkit.getPluginManager().registerEvents(changer, this);

	logMessage("Enabled.");

	if (!(Boolean) config.get("Enabled")) {
	    logMessage("Rank changing disabled. Please set 'enabled' to true if after you set up the ranks.");
	}

	debugMessage("Debug mode ON");

    }

    public void onDisable() {
	getData().save();
	logMessage("Data saved");
	logMessage("Disabled.");
	// stop scheduled tasks from running
	getServer().getScheduler().cancelTasks(this);
	timer.cancel();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	String prefix = "&2";
	if (getConf().get("Message prefix") != null) {
	    prefix = (String) getConf().get("Message prefix");
	}

	prefix = prefix.replaceAll("(&([a-f0-9]))", "\u00A7$2");

	String noPerm = "&cYou do not have permission to use this command.";
	boolean overridePerms = sender.hasPermission("autorank.*");

	    if (args[0].equalsIgnoreCase("reload")) {
		if (!sender.hasPermission("autorank.reload") && !overridePerms) {
		    sender.sendMessage(noPerm);
		    return true;
		}
		getData().save();
		logMessage("Data saved");
		getConf().load();
		sender.sendMessage(prefix + "Autorank config reloaded");
		return true;
	    }
	    else
	    if (args[0].equalsIgnoreCase("help")) {
		sender.sendMessage(prefix + "Actions: check, check [name], leaderboard, set [name] [value], reload");
		return false;
	    }
	    else
	    if (args[0].equalsIgnoreCase("leaderboard")) {
		// check permissions
		if (!sender.hasPermission("autorank.leaderboard") && !overridePerms) {
		    sender.sendMessage(noPerm);
		    return true;
		}
		leaderboard.display(sender, prefix);
		return true;
	    }
	    else
	    if (args[0].equalsIgnoreCase("check")) {
		if (!sender.hasPermission("autorank.check") && !overridePerms) {
		    sender.sendMessage(noPerm);
		    return true;
		}
		if (sender instanceof ConsoleCommandSender) {
		    sender.sendMessage("Cannot check for console.");
		    return true;
		}
		Integer time = (Integer) data.get(sender.getName().toLowerCase());
		if (time == null) {
		    sender.sendMessage(prefix + "No time registered. Something must have gone wrong on startup, please check the console.");
		} else {
		    String[] info = getRankInfo((Player) sender);
		    // 0 - current rank
		    // 1 - next rank
		    // 2 - in world
		    // 3 - time to next rank
		    sender.sendMessage(prefix + "You are a " + info[0] + " and have played for " + time / 60 + " hours and " + time % 60
			    + " minutes.");

		    if (info[1] != null && info[3] != null) {
			if ((Boolean) config.get("Enabled") == false) {
			    sender.sendMessage(prefix + "Automatic rank changes are disabled");
			    return true;
			}
			int reqMins = Integer.parseInt(info[3]);

			if (reqMins > 0) {
			    sender.sendMessage(prefix + "You will be ranked up to " + info[1] + " after " + info[3] + " more minutes.");
			} else {
			    sender.sendMessage(prefix + "You will now be ranked up.");
			    changer.CheckRank((Player) sender);
			}

		    }
		    if (info[2] != null) {
			sender.sendMessage(prefix + "This is specific to the world you are currently in. (" + info[2] + ")");
		    }
		}
		return true;
	    }
	
	    else
	    if(args.length<2){return false;}
	    
	    // set variables
	    String playerName = args[1].toLowerCase();
	    Player player = Bukkit.getPlayer(playerName);
	    // handle CHECK OTHERS command
	    if (args[0].equalsIgnoreCase("check")) {
		// check permissions
		if (!sender.hasPermission("autorank.checkothers") && !overridePerms) {
		    sender.sendMessage(noPerm);
		    return true;
		}

		Integer time = (Integer) data.get(playerName);
		if (time == null) {
		    sender.sendMessage(prefix + "No time registered yet, try again later.");
		} else {
		    sender.sendMessage(prefix + playerName + " has played for " + time / 60 + " hours and " + time % 60 + " minutes.");

		    if (player != null) {
			String[] info = getRankInfo(player);
			// 0 - current rank
			// 1 - next rank
			// 2 - in world
			// 3 - time to next rank
			sender.sendMessage(prefix + playerName + " is a " + info[0] + " and has played for " + time / 60 + " hours and "
				+ time % 60 + " minutes.");
			if (info[1] != null && info[3] != null) {
			    sender.sendMessage(prefix + "He/she will be ranked up to " + info[1] + " after " + info[3] + " more minutes.");
			}
			if (info[2] != null) {
			    sender.sendMessage(prefix + "This is specific to the world he/she is currently in. (" + info[2] + ")");
			}
		    } else {
			sender.sendMessage(prefix + "This player is offline, cannot check future rank");
		    }
		}
		return true;
		// handle CHANGE command
	    }
	    // handle SET command
	    else
	    if (args[0].equalsIgnoreCase("set")) {
		// check permissions
		if (!sender.hasPermission("autorank.set") && !overridePerms) {
		    sender.sendMessage(noPerm);
		    return true;
		}
		try {
		    int value = Integer.parseInt(args[2]);
		    data.set(playerName, (Integer) value);
		    sender.sendMessage(prefix + playerName + "'s playtime has been set to " + value);
		    return true;
		} catch (Exception e) {
		    sender.sendMessage(prefix + "Cannot set to that.");
		    return true;
		}
	    }
	    
	return false;
    }

    public void logMessage(String msg) {
	PluginDescriptionFile pdFile = this.getDescription();
	this.log.info(pdFile.getName() + " " + pdFile.getVersion() + " : " + msg);
    }

    public void debugMessage(String msg) {
	if (debug) {
	    this.log.info("Autorank debug: " + msg);
	}
    }

    public boolean getDebug() {
	return debug;
    }

    public Config getConf() {// name is weird because getConfig is used by
			     // JavaPlugin
	return config;
    }

    public DataStorage getData() {
	return data;
    }

    public VaultHandler getVault() {
	return vault;
    }

    public Leaderboard getLeaderboard() {
	return leaderboard;
    }

    public String[] getRankInfo(Player player) {
	// returns:
	// 0 - current rank
	// 1 - next rank
	// 2 - in world
	// 3 - time to next rank
	String[] res = new String[4];

	String playerName = player.getName().toLowerCase();
	String world = player.getWorld().getName();

	String[] Playergroups = vault.getGroups(player, world);
	if (Playergroups == null) {
	    Playergroups = new String[] { "Default" };
	}
	
	res[0] = "";
	for(String group : Playergroups){
	    if(!res[0].equals("")) res[0] += ", ";
	    res[0] += group;
	}


	boolean found = false;
	int entry = 1; 
	
	for (int i = 0; !found && i < Playergroups.length; i++) {

	    entry = 1;
	    found = false;
	    while (config.get(entry + ".from") != null && !found) {

		if (Playergroups[i].equals((String) config.get(entry + ".from")) && (config.get(entry + ".world") == null)
			|| world.equals((String) config.get(entry + ".world"))) {
		    found = true;
		} else {
		    entry++;
		}
	    }

	}

	if (found == true) {
	    res[1] = (String) config.get(entry + ".to");
	    res[2] = (String) config.get(entry + ".world");
	}

	Integer timePlayed = (Integer) data.get(playerName);

	if (timePlayed != null && config.get(entry + ".required minutes played") != null)
	    res[3] = ((Integer) ((Integer) config.get(entry + ".required minutes played") - timePlayed)).toString();
	;

	return res;
    }
}
