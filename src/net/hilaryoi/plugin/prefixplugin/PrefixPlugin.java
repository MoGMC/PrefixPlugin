package net.hilaryoi.plugin.prefixplugin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PrefixPlugin extends JavaPlugin implements Listener {

	HashMap<UUID, UnsetPrefixChange> prefixChanges = new HashMap<UUID, UnsetPrefixChange>();

	FileConfiguration config;

	private int WAIT_DAYS, WAIT_MS;

	@Override
	public void onEnable() {

		this.saveDefaultConfig();

		config = this.getConfig();

		WAIT_DAYS = config.getInt("cooldown");

		WAIT_MS = WAIT_DAYS * 86400000;

		getServer().getPluginManager().registerEvents(this, this);

	}

	@Override
	public void onDisable() {

		this.saveConfig();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equalsIgnoreCase("prefix")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players may set a prefix.");
				return true;

			}

			UUID uuid = ((Player) sender).getUniqueId();

			if (args.length != 2) {

				if (args.length == 1) {

					if (args[0].equalsIgnoreCase("confirm")) {

						UnsetPrefixChange change = prefixChanges.get(uuid);

						if (change == null) {
							sender.sendMessage(ChatColor.RED + "There was nothing for you to confirm. Type "
									+ ChatColor.AQUA + "/prefix" + ChatColor.RED + " for usage.");
							return true;

						}

						change.runChange();
						prefixChanges.remove(uuid);

						sender.sendMessage(ChatColor.GOLD + "Successfully confirmed your prefix change!");

						return true;

					} else if (args[0].equalsIgnoreCase("unset")) {

						schedulePrefixChange(uuid, new UnsetPrefixChange(uuid, this));

						sender.sendMessage(ChatColor.GOLD
								+ "Are you sure you want to unset your prefix? This may not be undone. Type "
								+ ChatColor.AQUA + "/prefix confirm" + ChatColor.GOLD
								+ " within 30 seconds to confirm your decision.");

						return true;

					}

				}

				sender.sendMessage(ChatColor.RED + "Usage: /prefix <prefix> <color>");
				return true;

			}

			ChatColor color = getColor(args[1]);

			if (color == null) {
				sender.sendMessage(ChatColor.RED + "Could not find a color named `" + args[1]
						+ "`. The currently available colors are yellow and pink.");
				return true;

			}

			long period = System.currentTimeMillis() - getChangeDate(uuid);

			if (period < WAIT_MS) {

				long periodDays = toDays(period);

				sender.sendMessage(String.format(
						"%sYou changed your prefix %s days ago. Please wait %s more days until changing your prefix.",
						ChatColor.RED.toString(), periodDays, WAIT_DAYS - periodDays));

				return true;

			}

			String prefix = color + ChatColor.stripColor(args[0]);

			// Do 12 because color code will take up 2 characters
			if (prefix.length() > 12) {
				sender.sendMessage(ChatColor.RED + "Please keep your prefix's length to a maximum of 10 characters.");
				return true;

			}

			sender.sendMessage(String.format(
					"%sAre you sure you want to set your prefix to `%s%s`? You will not be able to change your prefix for %s days. Type %s/prefix confirm%s within 30 seconds to confirm your decision.",
					ChatColor.GOLD, prefix, ChatColor.GOLD, WAIT_DAYS, ChatColor.AQUA, ChatColor.GOLD));

			schedulePrefixChange(uuid, new PrefixChange(uuid, this, prefix));

			return true;

		}

		return false;

	}

	public long toDays(long ms) {

		return Math.round(ms / 86400000);

	}

	public void schedulePrefixChange(UUID uuid, UnsetPrefixChange change) {

		prefixChanges.put(uuid, change);

		// run the cancellation in 30 seconds
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, change, 600);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		removePrefixChange(e.getPlayer().getUniqueId());

	}

	public void removePrefixChange(UUID uuid) {

		prefixChanges.remove(uuid);

	}

	public void setChangeDate(UUID uuid) {

		config.set("player." + uuid.toString(), System.currentTimeMillis());

	}

	public long getChangeDate(UUID uuid) {

		return config.getLong("player." + uuid.toString());

	}

	public ChatColor getColor(String colorString) {

		switch (colorString.toLowerCase()) {

		case "yellow":
			return ChatColor.YELLOW;

		case "pink":
			return ChatColor.LIGHT_PURPLE;

		default:
			return null;

		}

	}

}
