package me.armar.plugins.autorank;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class VaultHandler {

	public static Permission permission = null;
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

	public String getRank(Player player, String world) {
		if (world == null){
		return permission.getPrimaryGroup(player);
		}else{
		return permission.getPrimaryGroup(world, player.getName());
		}
	}

	public void setRank(String player, String oldRank, String rank, String world) {
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
}
