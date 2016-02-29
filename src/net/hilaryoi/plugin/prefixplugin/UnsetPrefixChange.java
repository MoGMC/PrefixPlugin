package net.hilaryoi.plugin.prefixplugin;

import java.util.UUID;

import org.bukkit.Bukkit;

public class UnsetPrefixChange implements Runnable {

	UUID uuid;

	PrefixPlugin plugin;

	protected boolean hasConfirmed;

	public UnsetPrefixChange(UUID uuid, PrefixPlugin plugin) {

		hasConfirmed = false;

		this.uuid = uuid;

		this.plugin = plugin;

	}

	@Override
	public void run() {

		if (!hasConfirmed) {
			plugin.removePrefixChange(uuid);

		}

	}

	public void runChange() {

		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
				String.format("permissions player %s prefix", Bukkit.getPlayer(uuid).getName()));

		hasConfirmed = true;

	}

}
