package me.armar.plugins.autorank;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankChanger implements Listener {

    private Autorank plugin;
    private Config config;
    private DataStorage data;
    private VaultHandler vault;

    public RankChanger(Autorank plugin) {
	this.plugin = plugin;
	config = plugin.getConf();
	data = plugin.getData();
	vault = plugin.getVault();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void OnPlayerJoin(PlayerJoinEvent e) {
	CheckRank(e.getPlayer());
    }

    public void CheckRank(Player player) {

	String playerName = player.getName().toLowerCase();

	if (!data.exists(playerName)) {
	    data.set(playerName, 0);
	}

	if ((Boolean) config.get("Enabled") == null) {
	    plugin.logMessage("Section 'Enabled' was not found in the config, please check that you are not using a pre-1.0 config");
	    return;
	}

	if (!(Boolean) config.get("Enabled")) {
	    return;
	}

	String world = player.getWorld().getName();

	String[] playerGroups = vault.getGroups(player, world);
	if (playerGroups == null) {
	    playerGroups = new String[] { "Default" };
	}

	for (String group : playerGroups) {
	    plugin.debugMessage("Player " + playerName + " is being rank checked in world " + world + " and has rank " + group);
	    int entry = 1;
	    boolean found = false;
	    while (!found && config.get(entry + ".from") != null) {

		if (plugin.getDebug()) {
		    plugin.debugMessage("Testing entry " + entry);
		    plugin.debugMessage("From: " + (String) config.get(entry + ".from"));
		    plugin.debugMessage("To: " + (String) config.get(entry + ".to"));
		    plugin.debugMessage("World: " + (String) config.get(entry + ".world"));
		}

		if (group.equalsIgnoreCase((String) config.get(entry + ".from")) && (config.get(entry + ".world") == null)
			|| world.equalsIgnoreCase((String) config.get(entry + ".world"))) {
		    found = true;
		    plugin.debugMessage("Confirmed entry " + entry);
		} else {
		    entry++;

		}
	    }

	    if (found == true) {

		Integer timePlayed = (Integer) data.get(playerName);

		plugin.debugMessage("Time played: " + timePlayed);
		plugin.debugMessage("Time needed: " + (Integer) config.get(entry + ".required minutes played"));

		if (!(timePlayed == null || timePlayed < (Integer) config.get(entry + ".required minutes played"))) {

		    vault.replaceGroup(playerName, group, (String) config.get(entry + ".to"), world);

		    String message = (String) config.get(entry + ".message");
		    if (message != null)
			player.sendMessage(message.replaceAll("(&([a-f0-9]))", "\u00A7$2"));

		    String commandEntry = (String) config.get(entry + ".commands");
		    String[] commands = null;
		    if (commandEntry != null) {
			commands = commandEntry.split(";");
		    }

		    if (commands != null) {
			for (String cmd : commands) {
			    Server server = Bukkit.getServer();
			    if (cmd != null) {
				cmd = cmd.replace("&p", playerName);
				server.dispatchCommand(server.getConsoleSender(), cmd);
			    }
			}
		    }

		    plugin.debugMessage("Sent message: " + message);

		}
	    }
	}
    }

}
