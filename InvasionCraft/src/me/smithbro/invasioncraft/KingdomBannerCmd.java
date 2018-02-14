package me.smithbro.invasioncraft;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

public class KingdomBannerCmd implements CommandExecutor, Listener {

	InvasionCraft plugin;

	public KingdomBannerCmd(InvasionCraft passedPlugin) {
		this.plugin = passedPlugin;
	}

	FileConfiguration config = InvasionCraft.plugin.getConfig();

	public String cmd2 = "patternset";
	static Permission canSetPattern = new Permission("invasioncraft.cansetpattern");
	static boolean bannerReady = false;
	static String kingdom = "";

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.hasPermission(canSetPattern)) {
				ArrayList<String>k = new ArrayList<>();
				k.add("Mythros");
				k.add("Aion");
				k.add("Wrenn");
				if (args.length == 0) {
					p.sendMessage(ChatColor.GOLD + "/setpattern <kingdom>: Assigns a banner pattern to a kingdom");
				}

				else if (k.contains(args[0])) {
					p.sendMessage(ChatColor.GOLD + "Right click on the banner to assign the pattern");
					kingdom = args[0];
					bannerReady = true;

				} else
					p.sendMessage(ChatColor.RED + "You need to enter a valid Kingdom");

			} else
				p.sendMessage(ChatColor.RED + "That command doesn't exist!");
		}

		return true;
	}

}
