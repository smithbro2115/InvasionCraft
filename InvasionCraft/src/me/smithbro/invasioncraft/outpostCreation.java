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
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class outpostCreation implements CommandExecutor {

	InvasionCraft plugin;

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

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		Player p = (Player) sender;

		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD
					+ "/outpost create <number (0-12>: Creates an outpost, you must have a WorldEdit selection");
			sender.sendMessage(ChatColor.GOLD + "/outpost delete <number (0-12): Deletes an outpost");
		} else if (args[0].equalsIgnoreCase("create")) {
			if (args[1] != null) {
				Selection s = getWorldEdit().getSelection(p);

				if (s == null) {
					sender.sendMessage("You need to make a World Edit selection!");
				}

				String outpostNum = "outpost" + args[1];
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
