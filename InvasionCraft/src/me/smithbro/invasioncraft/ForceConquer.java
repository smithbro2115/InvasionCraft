package me.smithbro.invasioncraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ForceConquer implements CommandExecutor {

	InvasionCraft plugin;

	public ForceConquer(InvasionCraft passedPlugin) {
		this.plugin = passedPlugin;
	}

	public String cmd1 = "conquer";
	static Permission canForceConquer = new Permission("invasioncraft.canforceconquer");

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0 && p.hasPermission(canForceConquer)) {
				p.sendMessage(ChatColor.GOLD
						+ "/conquer <number (1-12>: Assigns an outpost to the WorldGuard region your standing in");
			}

			else if (args[0].equalsIgnoreCase("outpost") && p.hasPermission(canForceConquer)) {
				InvasionCraft.invasionProgress = 99;
			} else
				p.sendMessage(ChatColor.RED + "You used this command wrong... Else you don't have permission");

		}

		return true;
	}
}
