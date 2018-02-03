package me.smithbro.invasioncraft;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.store.Coll;
import com.massivecraft.massivecore.util.IdUtil;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

public class InvasionCraft extends JavaPlugin implements Listener {
	Logger invasionCraftLogger = Bukkit.getLogger();
	Region getRegion;
	public WorldGuardPlugin worldGuardPlugin;

	/*
	 * if player moves then find what faction the player is in and who the owner of
	 * the land they are in and set a boolean called diffFaction to true if they are
	 * different. Then if it is true find the relation between them and set it to a
	 * string called invralation then if it is equal to enemy check if anyone else
	 * is in that region and set a boolean called otherPlayers to true if so then if
	 * none of those players are an enemy do add 1 to an int called invTeamAmount if
	 * invTeamAmount is > 0 and < 100 then do a while loop that adds 1 to an int
	 * called invasionProgress and Broadcast a
	 * plugin.getServer().broadcastMessage("a " + factionthatsbeinginvadedname +
	 * "outpost is being invaded" + invasionProgress + "%"); then in the same while
	 * loop add Thread.sleep(2000 / invTeamAmount) then do else if invProgress ==
	 * 100 then give that land to the invading faction
	 */

	public static InvasionCraft plugin;

	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;

		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		this.getCommand("outpost").setExecutor(new outpostCreation(this));
		worldGuardPlugin = getWorldGuard();
		runnable();
		runnable2();
		puttingOutpostsInHash();
	}

	@Override
	public void onDisable() {

	}

	public Location getLocation(String outpost, String corner) {
		World world = Bukkit.getWorld((String) plugin.getConfig().get("Outposts." + outpost + ".world"));
		double x = plugin.getConfig().getDouble("Outposts." + outpost + "." + corner + ".x");
		double y = plugin.getConfig().getDouble("Outposts." + outpost + "." + corner + ".y");
		double z = plugin.getConfig().getDouble("Outposts." + outpost + "." + corner + ".z");
		return new Location(world, x, y, z);
	}

	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	private ArrayList<String> outpostRegions = new ArrayList<>(
			Arrays.asList("outpost1", "outpost2", "outpost3", "outpost4", "outpost5", "outpost6", "outpost7",
					"outpost8", "outpost9", "outpost10", "outpost11", "outpost12"));

	static ArrayList<Player> invaders = new ArrayList<>();
	static ArrayList<Player> defenders = new ArrayList<>();
	HashMap<String, Integer> outpostNum = new HashMap<>();
	static int defendingTeam = defenders.size();
	static int invasionProgress = 0;
	static String ocdOutpostNameCh = null;
	static ProtectedRegion ocdOutpostCh = null;
	static Faction occupiedFactionCh = null;
	static Faction invadingFactionCh = null;
	static Permission canInvade = new Permission("invasioncraft.caninvade");
	static Permission canDefend = new Permission("invasioncraft.candefend");

	@EventHandler
	public void onLeave (RegionLeaveEvent e) {
		String regionName = e.getRegion().getId();
		if (regionName.equals(ocdOutpostNameCh)) {
			if(invaders.contains(e.getPlayer())) {
				invaders.remove(e.getPlayer());
			}
		}
	}
	
	public void puttingOutpostsInHash() {
		int i = 0;
		for (String s : outpostRegions) {
			i++;
			outpostNum.putIfAbsent(s, i);
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		MPlayer invader = null;
		MPlayer def = null;
		MPlayer factionI = null;
		// CuboidSelection opst1 = new
		// CuboidSelection(Bukkit.getWorld(plugin.getConfig().getString("Outposts.outpost1.world")),
		// getLocation("outpost1", "cornerA"), getLocation("outpost1", "cornerB"));

		Player invadingPlayer = e.getPlayer();
		invader = MPlayer.get(invadingPlayer);
		Faction invadingFaction = invader.getFaction();
		Location invaderLocation = invadingPlayer.getLocation();
		Faction occupiedFaction = BoardColl.get().getFactionAt(PS.valueOf(invaderLocation));

		if (invadingPlayer.hasPermission(canInvade)) {
			if (invadingFaction != occupiedFaction) {
				if (invadingFaction.getRelationTo(occupiedFaction) == Rel.ENEMY) {
					LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(invadingPlayer);
					Vector playerVector = localPlayer.getPosition();
					RegionManager regionManager = worldGuardPlugin.getRegionManager(invadingPlayer.getWorld());
					ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(playerVector);

					for (ProtectedRegion region : applicableRegionSet) {
						String ocdOutpostName = region.getId();
						if (outpostRegions.contains(ocdOutpostName)) {
							if (defenders.size() == 0 && !invaders.contains(invadingPlayer)) {
								ocdOutpostCh = region;
								invaders.add(invadingPlayer);
								for (Player i : invaders) {

									factionI = MPlayer.get(i);
									invadingFactionCh = factionI.getFaction();
									Location invaderLocationCh = i.getLocation();
									occupiedFactionCh = BoardColl.get().getFactionAt(PS.valueOf(invaderLocationCh));
									LocalPlayer iLP = worldGuardPlugin.wrapPlayer(i);
									Vector iVector = iLP.getPosition();
									RegionManager iManager = worldGuardPlugin.getRegionManager(i.getWorld());
									ApplicableRegionSet iRegionSet = iManager.getApplicableRegions(iVector);
									for (ProtectedRegion iRegion : iRegionSet) {
										ocdOutpostNameCh = iRegion.getId();
									}
								}

							}
							

						} else if (invaders.contains(invadingPlayer))
							invaders.remove(invadingPlayer);
					}

				} else if (invaders.contains(invadingPlayer))
					invaders.remove(invadingPlayer);

			} else if (invaders.contains(invadingPlayer))
				invaders.remove(invadingPlayer);
		} else if (invaders.contains(invadingPlayer))
			invaders.remove(invadingPlayer);

		if (invaders.size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				LocalPlayer otherPlayer = worldGuardPlugin.wrapPlayer(p);
				Vector otherVector = otherPlayer.getPosition();
				RegionManager regionManager = worldGuardPlugin.getRegionManager(invadingPlayer.getWorld());
				ApplicableRegionSet otherRegionSet = regionManager.getApplicableRegions(otherVector);
				for (ProtectedRegion r : otherRegionSet) {
					def = MPlayer.get(p);
					Faction defFaction = def.getFaction();

					if (p.hasPermission(canDefend)) {
						if (defFaction == occupiedFaction && r.getId().equals(ocdOutpostNameCh)) {
							invaders.clear();
						}

					}
				}

			}
		}
	}

	public void runnable() {

		new BukkitRunnable() {

			int ticks = 0, delay = 40 / (1 + invaders.size());

			@Override
			public void run() {

				delay = 40 / (1 + invaders.size());
				ticks++;
				if (ticks % delay != 0)
					return;

				if (invasionProgress >= 100) {
					Bukkit.broadcastMessage(ChatColor.GOLD + ocdOutpostNameCh + " has been conquered!");

					invaders.get(0).setOp(true);
					invaders.get(0).chat("/f admin");
					invaders.get(0).chat("/f unclaim square 3");
					invaders.get(0).chat("/f claim square 2");
					invaders.get(0).chat("/f admin");
					invaders.get(0).setOp(false);
					invasionProgress = 0;
					invaders.clear();
				}

				if (invaders.size() > 0 && invasionProgress <= 100) {
					invasionProgress++;
					Bukkit.broadcastMessage(
							ChatColor.RED + occupiedFactionCh.getName() + "'s outpost " + ocdOutpostNameCh + ", is: "
									+ invasionProgress + "% occupied by: " + invadingFactionCh.getName());
				}
			}
		}.runTaskTimer(this, 0, 1);

	}

	public void runnable2() {
		new BukkitRunnable() {
			@Override
			public void run() {

				if (invaders.size() == 0 && invasionProgress > 0) {
					invasionProgress--;
					Bukkit.broadcastMessage(ChatColor.GREEN + occupiedFactionCh.getName() + "s outpost "
							+ ocdOutpostNameCh + ", is being restored " + invasionProgress + "%");
					if (invasionProgress == 0) {
						Bukkit.broadcastMessage(ChatColor.GOLD + ocdOutpostNameCh + " has been restored!");
					}
				}
			}
		}.runTaskTimer(this, 0, 10);
	}

}
