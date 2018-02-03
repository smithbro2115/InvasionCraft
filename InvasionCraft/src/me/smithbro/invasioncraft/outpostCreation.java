	package me.smithbro.invasioncraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class outpostCreation implements CommandExecutor {

	InvasionCraft plugin;
	public WorldGuardPlugin worldGuardPlugin;

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

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		Player p = (Player) sender;

		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD
					+ "/outpost create <number (0-12>: Creates an outpost, you must have a WorldEdit selection");
			sender.sendMessage(ChatColor.GOLD + "/outpost delete <number (0-12): Deletes an outpost");
		} else if (args[0].equalsIgnoreCase("create")) {
			if (args[1] != null) {
				LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
				Vector playerVector = localPlayer.getPosition();
				RegionManager regionManager = worldGuardPlugin.getRegionManager(p.getWorld());
				ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(playerVector);
				ProtectedRegion r;

				for (ProtectedRegion region : applicableRegionSet) {
					r = region;
				}

				if (!InvasionCraft.outpostRegions.contains(r.getId())) {
					sender.sendMessage("You need to be in an outpost region!");
				}

				String outpostNum = "" + args[1];
				sender.sendMessage(ChatColor.GREEN + outpostNum + " Created");
				config.set("Outposts." + outpostNum + ".cornerA", s.getMinimumPoint());
				config.set("Outposts." + outpostNum + ".cornerB", s.getMaximumPoint());
				config.set("Outposts." + outpostNum + ".world", s.getWorld().getName());
				config.set("Outposts." + outpostNum + ".number", args[1]);
				InvasionCraft.plugin.saveConfig();
				InvasionCraft.plugin.reloadConfig();

			} else
				sender.sendMessage(ChatColor.RED + "You must enter a number within 0 and 12");
		} else if (args[0].equalsIgnoreCase("delete")) {
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
