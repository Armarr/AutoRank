package me.armar.plugins.autorank;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class VaultHandler {

    private static Permission permission = null;
    private Autorank plugin;
    private boolean usesGM;

    public VaultHandler(Autorank plugin) {
	this.plugin = plugin;
	setupPermissions();

	final PluginManager pluginManager = plugin.getServer().getPluginManager();
	final Plugin GMplugin = pluginManager.getPlugin("GroupManager");

	if (GMplugin != null && GMplugin.isEnabled()) {
	    usesGM = true;
	}
    }

    private Boolean setupPermissions() {
	RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager()
		.getRegistration(net.milkbowl.vault.permission.Permission.class);
	if (permissionProvider != null) {
	    permission = permissionProvider.getProvider();
	}
	return (permission != null);
    }

    public String getPrimaryGroup(Player player, String world) {
	if (world == null) {
	    return permission.getPrimaryGroup(player);
	} else {
	    return permission.getPrimaryGroup(world, player.getName());
	}
    }

    public String[] getGroups(Player player, String world) {
	if (usesGM == true) {
	    return new String[] { getPrimaryGroup(player, world) };
	} else {
	    return permission.getPlayerGroups(world, player.getName());
	}
    }

    public boolean playerRemoveGroup(String world, String player, String group) {
	plugin.debugMessage("Removed " + player + " to group: " + group);
	return permission.playerRemoveGroup(world, player, group);
    }

    public boolean playerAddGroup(String world, String playerName, String group) {
	    plugin.debugMessage("Added " + playerName + " to group: " + group);
	    return permission.playerAddGroup(world, playerName, group);
    }

    public String[] getGroups() {
	return permission.getGroups();
    }
}
