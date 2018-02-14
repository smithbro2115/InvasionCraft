package me.smithbro.invasioncraft;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBanner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.Location;
import org.bukkit.Material;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvasionCraft extends JavaPlugin implements Listener {
	Logger invasionCraftLogger = Bukkit.getLogger();
	Region getRegion;
	public WorldGuardPlugin worldGuardPlugin;
	File banners = new File(this.getDataFolder(), "banners.yml");
	File kingdomoutposts = new File(this.getDataFolder(), "kingdomoutposts.yml");

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
		this.getCommand("conquer").setExecutor(new ForceConquer(this));
		this.getCommand("patternset").setExecutor(new KingdomBannerCmd(this));
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

	public static ArrayList<String> outpostRegions = new ArrayList<>(
			Arrays.asList("outpost1", "outpost2", "outpost3", "outpost4", "outpost5", "outpost6", "outpost7",
					"outpost8", "outpost9", "outpost10", "outpost11", "outpost12"));

	static ArrayList<Player> invaders = new ArrayList<>();
	static int invadersAmount = invaders.size();
	static ArrayList<Player> defenders = new ArrayList<>();
	HashMap<Integer, Faction> outpostNumF = new HashMap<>();
	HashMap<String, Integer> outpostNumO = new HashMap<>();
	static int defendingTeam = defenders.size();
	static int invasionProgress = 0;
	static String ocdOutpostNameCh = "";
	static ProtectedRegion ocdOutpostCh = null;
	static Faction occupiedFactionCh = null;
	static Faction invadingFactionCh = null;
	static Permission canInvade = new Permission("invasioncraft.caninvade");
	static Permission canDefend = new Permission("invasioncraft.candefend");
	static int outpostNum = 0;

	@EventHandler
	public void onLeave(RegionLeaveEvent e) {
		String regionName = e.getRegion().getId();
		if (regionName.equals(ocdOutpostNameCh)) {
			if (invaders.contains(e.getPlayer())) {
				invaders.remove(e.getPlayer());
			}
		}
	}

	public void puttingOutpostsInHash() {
		int i = 0;
		for (String s : outpostRegions) {
			i++;
			outpostNumO.putIfAbsent(s, i);
		}
	}

	public int getOutpostNum(String outpostName) {
		int num = 1;
		while (num <= 12) {
			if (plugin.getConfig().getString("Outpost." + num + ".region").equals(outpostName)) {
				break;
			} else
				num += 1;
		}
		return num;
	}

	
	@SuppressWarnings("unchecked")
	public void bannerSwitch(Location min, Location max) {
	    for (int x = min.getBlockX() - 4; x <= max.getBlockX() + 4; x++) {
	        for (int y = min.getBlockY() - 4; y <= max.getBlockY() + 4; y++) {
	            for (int z = min.getBlockZ() - 4; z <= max.getBlockZ() + 4; z++) {
	                Block blk = min.getWorld().getBlockAt(new Location(min.getWorld(), x, y, z));
	                if (blk.getType() == Material.STANDING_BANNER || blk.getType() == Material.WALL_BANNER) {
	                	CraftBanner bM = new CraftBanner(blk);
	                	List<Pattern> p = (List<Pattern>) plugin.getConfig().getList("Kingdom." + invadingFactionCh.getName() + ".banner.patterns");
	                	String dyeString = plugin.getConfig().getString("Kingdom." + invadingFactionCh.getName() + ".banner.color");
	                	DyeColor c = DyeColor.valueOf(dyeString);
	                	bM.setBaseColor(c);
	                	bM.setPatterns(p);
	                	bM.update(true);
	                }
	            }
	        }
	    }
	}
	 

	public void setScoreboardInvasion(Faction f) {
		List<Player> players = f.getOnlinePlayers();

		for (Player player : players) {
			getScoreboardInvasion(player);
		}
	}

	public void getScoreboardInvasion(Player player) {

		if (invasionProgress > 0) {
			ScoreboardManager m = Bukkit.getScoreboardManager();
			Scoreboard b = m.getNewScoreboard();

			Objective o = b.registerNewObjective("Invading Faction", "dummy");
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			o.setDisplayName(ChatColor.BOLD + occupiedFactionCh.getName() + " is being invaded");
			Score invadingF = o.getScore(ChatColor.GOLD + "Invaders: " + ChatColor.RED + invadingFactionCh.getName());
			Score occupiedF = o.getScore(ChatColor.GOLD + "Defenders: " + ChatColor.RED + occupiedFactionCh.getName());
			Score oOutpost = o.getScore(ChatColor.GOLD + "Outpost Name: " + ChatColor.RED + ocdOutpostNameCh);
			Score inProgress = o.getScore(ChatColor.GOLD + "Progress: " + ChatColor.RED + invasionProgress + "%");
			Score blank1 = o.getScore(ChatColor.GOLD.toString());
			Score blank2 = o.getScore(ChatColor.GOLD.toString());
			blank1.setScore(1);
			invadingF.setScore(6);
			occupiedF.setScore(5);
			blank2.setScore(4);
			oOutpost.setScore(3);
			inProgress.setScore(2);

			player.setScoreboard(b);
		} else
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		MPlayer invader = null;
		MPlayer def = null;
		MPlayer factionI = null;

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
					String ocdOutpostName = null;

					for (ProtectedRegion region : applicableRegionSet) {
						ocdOutpostName = region.getId();
					}

					if (ocdOutpostName != null) {
						if (ocdOutpostNameCh.isEmpty() || ocdOutpostName.equals(ocdOutpostNameCh)) {
							if (outpostRegions.contains(ocdOutpostName)) {
								if (defenders.size() == 0 && !invaders.contains(invadingPlayer)) {
									int outpostNum = getOutpostNum(ocdOutpostName);
									int connectedOutpost1 = plugin.getConfig()
											.getInt("Outpost." + outpostNum + ".connected1");
									int connectedOutpost2 = plugin.getConfig()
											.getInt("Outpost." + outpostNum + ".connected2");
									String cFaction1 = plugin.getConfig()
											.getString("Outpost." + connectedOutpost1 + ".faction");
									String cFaction2 = plugin.getConfig()
											.getString("Outpost." + connectedOutpost2 + ".faction");
									if (invadingFaction.getName().equals(cFaction1)
											|| invadingFaction.getName().equals(cFaction2)) {
										invaders.add(invadingPlayer);
										for (Player i : invaders) {

											factionI = MPlayer.get(i);
											invadingFactionCh = factionI.getFaction();
											Location invaderLocationCh = i.getLocation();
											occupiedFactionCh = BoardColl.get()
													.getFactionAt(PS.valueOf(invaderLocationCh));
											LocalPlayer iLP = worldGuardPlugin.wrapPlayer(i);
											Vector iVector = iLP.getPosition();
											RegionManager iManager = worldGuardPlugin.getRegionManager(i.getWorld());
											ApplicableRegionSet iRegionSet = iManager.getApplicableRegions(iVector);
											for (ProtectedRegion iRegion : iRegionSet) {
												if (outpostRegions.contains(iRegion.getId())) {
													ocdOutpostNameCh = iRegion.getId();
													ocdOutpostCh = iRegion;
													outpostNum = getOutpostNum(ocdOutpostNameCh);
													break;
												}

											}
										}

									}

								}

							} else if (invaders.contains(invadingPlayer))
								invaders.remove(invadingPlayer);
						} else if (invaders.contains(invadingPlayer))
							invaders.remove(invadingPlayer);
					} else if (invaders.contains(invadingPlayer))
						invaders.remove(invadingPlayer);

				} else if (invaders.contains(invadingPlayer))
					invaders.remove(invadingPlayer);

			} else if (invaders.contains(invadingPlayer))
				invaders.remove(invadingPlayer);
		} else if (invaders.contains(invadingPlayer))
			invaders.remove(invadingPlayer);

		if (invaders.size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission(canDefend)) {
					def = MPlayer.get(p);
					Faction defFaction = def.getFaction();
					if (defFaction == occupiedFactionCh) {
						LocalPlayer otherPlayer = worldGuardPlugin.wrapPlayer(p);
						Vector otherVector = otherPlayer.getPosition();
						RegionManager regionManager = worldGuardPlugin.getRegionManager(p.getWorld());
						ApplicableRegionSet otherRegionSet = regionManager.getApplicableRegions(otherVector);
						ProtectedRegion region = null;
						for (ProtectedRegion r : otherRegionSet) {
							region = r;
						}

						if (region != null) {
							if (region == ocdOutpostCh) {
								invaders.clear();
								break;
							}

						}
					}

				}

			}
		}
	}

	static String chatDisabled = null;

	/*
	 * @EventHandler public void onPlayerChat(AsyncPlayerChatEvent e) {
	 * 
	 * // DEFINING PLAYER
	 * 
	 * Player p = e.getPlayer();
	 * 
	 * // DISABLING CHAT / CHECKING
	 * 
	 * if (!chatDisabled.equals(null)) { if (chatDisabled.equals(p.getName())) {
	 * 
	 * e.setCancelled(true);
	 * 
	 * // need to cancel receiving messages
	 * 
	 * e.getRecipients().remove(p); }
	 * 
	 * for (Player pl : e.getRecipients()) {
	 * 
	 * if (chatDisabled.equals(pl.getName())) { e.getRecipients().remove(pl);
	 * 
	 * } } } } Didn't work may keep
	 */

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
					Bukkit.broadcastMessage(
							ChatColor.RED + ocdOutpostNameCh + ChatColor.GOLD + " has been conquered by "
									+ ChatColor.RED + invadingFactionCh.getName() + ChatColor.GOLD + "!");
					if (!invaders.get(0).isOp()) {
						invaders.get(0).setOp(true);
						invaders.get(0).chat("/f admin");
						invaders.get(0).chat("/f unclaim square 3");
						invaders.get(0).chat("/f claim square 2");
						invaders.get(0).chat("/f admin");
						invaders.get(0).setOp(false);
						invasionProgress = 0;
						invaders.clear();
					}
					if (invaders.get(0).isOp()) {
						invaders.get(0).chat("/f admin");
						invaders.get(0).chat("/f unclaim square 3");
						invaders.get(0).chat("/f claim square 2");
						invaders.get(0).chat("/f admin");
						invasionProgress = 0;
						invaders.clear();
					}
					int outpostNumL = getOutpostNum(ocdOutpostNameCh);
					plugin.getConfig().set("Outpost." + outpostNumL + ".faction", invadingFactionCh.getName());
					plugin.saveConfig();
					plugin.reloadConfig();
					setScoreboardInvasion(invadingFactionCh);
					setScoreboardInvasion(occupiedFactionCh);
					ocdOutpostNameCh = "";
					double xMi  = ocdOutpostCh.getMinimumPoint().getX();
					double yMi  = ocdOutpostCh.getMinimumPoint().getY();
					double zMi  = ocdOutpostCh.getMinimumPoint().getZ();
					double xMa  = ocdOutpostCh.getMaximumPoint().getX();
					double yMa  = ocdOutpostCh.getMaximumPoint().getY();
					double zMa  = ocdOutpostCh.getMaximumPoint().getZ();
					Location locMi = new Location(Bukkit.getWorld("Builder World VII-"), xMi, yMi, zMi);
					Location locMa = new Location(Bukkit.getWorld("Builder World VII-"), xMa, yMa, zMa);
					bannerSwitch(locMi, locMa);
				}
				else if (invadersAmount > 0 && invasionProgress <= 100) {
					if (invasionProgress == 0) {
						Bukkit.broadcastMessage(ChatColor.RED + occupiedFactionCh.getName() + "'s" + ChatColor.GOLD
								+ " outpost " + ChatColor.RED + ocdOutpostNameCh + ChatColor.GOLD + ", is "
								+ ChatColor.RED + invasionProgress + ChatColor.GOLD + "% occupied by " + ChatColor.RED
								+ invadingFactionCh.getName());
					}
					invasionProgress++;
					setScoreboardInvasion(invadingFactionCh);
					setScoreboardInvasion(occupiedFactionCh);
					if (invasionProgress % 10 == 0) {
						Bukkit.broadcastMessage(ChatColor.RED + occupiedFactionCh.getName() + "'s" + ChatColor.GOLD
								+ " outpost " + ChatColor.RED + ocdOutpostNameCh + ChatColor.GOLD + ", is "
								+ ChatColor.RED + invasionProgress + ChatColor.GOLD + "% occupied by " + ChatColor.RED
								+ invadingFactionCh.getName());
					}
				}
				invadersAmount = invaders.size();
			}
		}.runTaskTimer(this, 0, 1);

	}

	public void runnable2() {
		new BukkitRunnable() {
			@Override
			public void run() {

				if (invaders.size() == 0 && invasionProgress > 0) {
					if (invasionProgress % 10 == 0) {
						Bukkit.broadcastMessage(ChatColor.RED + occupiedFactionCh.getName() + "'s" + ChatColor.GOLD
								+ " outpost " + ChatColor.RED + ocdOutpostNameCh + ChatColor.GOLD
								+ ", is being restored " + ChatColor.RED + invasionProgress + "%");
					}
					invasionProgress--;
					setScoreboardInvasion(invadingFactionCh);
					setScoreboardInvasion(occupiedFactionCh);
					if (invasionProgress == 0) {
						Bukkit.broadcastMessage(
								ChatColor.RED + ocdOutpostNameCh + ChatColor.GOLD + " has been restored!");
						setScoreboardInvasion(invadingFactionCh);
						setScoreboardInvasion(occupiedFactionCh);
						ocdOutpostNameCh = "";
					}
				}
			}
		}.runTaskTimer(this, 0, 10);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (KingdomBannerCmd.bannerReady == true) {
			Player p = event.getPlayer();
			Action a = event.getAction();
			if (a.equals(Action.RIGHT_CLICK_BLOCK)) {
				if (event.getClickedBlock().getType() == Material.STANDING_BANNER
						|| event.getClickedBlock().getType() == Material.WALL_BANNER) {
					Block b = event.getClickedBlock();
					CraftBanner bM = new CraftBanner(b);
					List<Pattern> bP = bM.getPatterns();
					plugin.getConfig().set("Kingdom." + KingdomBannerCmd.kingdom + ".banner.patterns", bP);
					plugin.getConfig().set("Kingdom." + KingdomBannerCmd.kingdom + ".banner.color", bM.getBaseColor().toString());
					plugin.saveConfig();
					plugin.reloadConfig();
					p.sendMessage(ChatColor.GREEN + KingdomBannerCmd.kingdom + "'s banner set");
					KingdomBannerCmd.kingdom = "";
					KingdomBannerCmd.bannerReady = false;
					
				}else p.sendMessage(ChatColor.RED + "You must right click on a Banner!");
			}
		}
	}


}
