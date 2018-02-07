package me.smithbro.invasioncraft;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class outpostCreation implements CommandExecutor {

	InvasionCraft plugin;
	static WorldGuardPlugin worldGuardPlugin;

	public outpostCreation(InvasionCraft passedPlugin) {
		this.plugin = passedPlugin;
	}

	FileConfiguration config = InvasionCraft.plugin.getConfig();

	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if (p instanceof WorldEditPlugin)
			return (WorldEditPlugin) p;
		else
			return null;
	}

	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	static ArrayList<String> allowedRegions = new ArrayList<>();

	public void getRegion() {
		int i = 1;
		ConfigurationSection configSection = config.getConfigurationSection("Outpost." + i);
		
		for (String key : configSection.getKeys(false)) {
			i++;
			if (!allowedRegions.contains(key)) {
				allowedRegions.add(key);
			}

		}
		
	}

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		worldGuardPlugin = getWorldGuard();
		Player p = (Player) sender;
		getRegion();
		for (String test : allowedRegions) {
			p.sendMessage(test);
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD
					+ "/outpost create <number (1-12>: Assigns an outpost to the WorldGuard region your standing in");
			sender.sendMessage(ChatColor.GOLD + "/outpost delete <number (1-12): Unassigns an outpost");
		}

		else if (args[0].equalsIgnoreCase("create")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.GOLD
						+ "/outpost create <number (1-12>: Assigns an outpost to the WorldGuard region your standing in");
			}

			else if (args.length == 2) {
				LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
				Vector playerVector = localPlayer.getPosition();
				RegionManager regionManager = worldGuardPlugin.getRegionManager(p.getWorld());
				ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(playerVector);
				ProtectedRegion r = null;

				for (ProtectedRegion region : applicableRegionSet) {
					r = region;
				}
				if (r != null) {
					if (!InvasionCraft.outpostRegions.contains(r.getId())) {
						sender.sendMessage(ChatColor.RED + "You need to be in an outpost region!");
					}

					else if (!allowedRegions.contains(r.getId())) {
						config.set("Outpost." + args[1] + ".region", r.getId());
						InvasionCraft.plugin.saveConfig();
						InvasionCraft.plugin.reloadConfig();
						String outpostNum = "" + args[1];
						sender.sendMessage(ChatColor.GREEN + outpostNum + " assigned");
					} else
						sender.sendMessage(ChatColor.RED + r.getId() + " is already assigned to an outpost");

				} else
					sender.sendMessage(ChatColor.RED + "You need to be in a region!");
			} else
				sender.sendMessage(ChatColor.RED + "You need to have a number between 1 and 12!");
		}

		else if (args[0].equalsIgnoreCase("delete")) {
			if (args[1] != null) {
				String outpostNum = "outpost" + args[1];
				sender.sendMessage(ChatColor.GREEN + outpostNum + " Deleted");
				config.set("Outpost." + outpostNum, null);
				InvasionCraft.plugin.saveConfig();
				InvasionCraft.plugin.reloadConfig();

			} else
				sender.sendMessage(ChatColor.RED + "You must enter a number within 0 and 12");
		}

		return true;
	}

}
