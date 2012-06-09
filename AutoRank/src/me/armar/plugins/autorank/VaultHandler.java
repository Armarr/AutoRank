package me.armar.plugins.autorank;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class VaultHandler {

	private static Permission permission = null;
	private Autorank plugin;

	public VaultHandler(Autorank plugin) {
		this.plugin = plugin;
		setupPermissions();
	}

	private Boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = plugin
				.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	public String getPrimaryGroup(Player player, String world) {
		if (world == null){
		return permission.getPrimaryGroup(player);
		}else{
		return permission.getPrimaryGroup(world, player.getName());
		}
	}
	
	public String[] getGroups(Player player, String world) {
		if (world == null){
		return permission.getPlayerGroups(player);
		}else{
		return permission.getPlayerGroups(world, player.getName());
		}
	}

	public void replaceGroup(String player, String oldRank, String rank, String world) {
		plugin.debugMessage("Old rank: " + oldRank);
		plugin.debugMessage("New rank: " + rank);
		
		if (oldRank != null)
		{
			permission.playerRemoveGroup(world, player, oldRank);
		}else{
		    	permission.playerRemoveGroup(world, player, "default");
		}
		
		permission.playerAddGroup(world, player, rank);
	}

	public String[] getGroups() {
	    return permission.getGroups();
	}
}
