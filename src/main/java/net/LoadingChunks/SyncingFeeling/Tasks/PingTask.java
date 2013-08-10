package net.LoadingChunks.SyncingFeeling.Tasks;

import net.LoadingChunks.SyncingFeeling.SyncingFeeling;
import net.LoadingChunks.SyncingFeeling.util.SQLWrapper;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PingTask extends BukkitRunnable {
	
	private SyncingFeeling plugin;
	
	public PingTask(SyncingFeeling plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		SQLWrapper.ping();
	}

}
