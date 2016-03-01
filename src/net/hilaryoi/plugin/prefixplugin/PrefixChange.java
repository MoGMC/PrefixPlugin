package net.hilaryoi.plugin.prefixplugin;

import java.util.UUID;

import org.bukkit.Bukkit;

public class PrefixChange extends UnsetPrefixChange {

	String prefix;

	public PrefixChange(UUID uuid, PrefixPlugin plugin, String prefix) {

		super(uuid, plugin);

		this.prefix = prefix;

	}

	@Override
	public void runChange() {

		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
				String.format("permissions player %s prefix \"%s \"", Bukkit.getPlayer(uuid).getName(), prefix));

		plugin.setChangeDate(uuid);

		hasConfirmed = true;

	}

}
