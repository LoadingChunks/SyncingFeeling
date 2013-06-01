package net.LoadingChunks.SyncingFeeling;

/*
    This file is part of SyncingFeeling

    SyncingFeeling is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SyncingFeeling is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SyncingFeeling. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;

import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;
import net.LoadingChunks.SyncingFeeling.Tasks.RecoverTask;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

public class SyncingFeelingEventListener implements Listener {

	private SyncingFeeling plugin;
	
	private ArrayList<BukkitTask> syncTasks = new ArrayList<BukkitTask>();

	public SyncingFeelingEventListener(SyncingFeeling plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if(event.getPlayer().hasPermission("sync.do"))
			SerializableInventory.fromInventory(event.getPlayer(), event.getPlayer().getInventory()).commit();		
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent event) {
		if(event.getPlayer().hasPermission("sync.do"))
			SerializableInventory.fromInventory(event.getPlayer(), event.getPlayer().getInventory()).commit();
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(event.getEntity().hasPermission("sync.do"))
			SerializableInventory.fromInventory(event.getEntity(), event.getEntity().getInventory()).commit();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		syncTasks.add(new RecoverTask(plugin,event.getPlayer()).runTaskLater(plugin, 40));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStop(PlayerCommandPreprocessEvent event) {
		if((event.getMessage().equalsIgnoreCase("/stop") || event.getMessage().equalsIgnoreCase("stop")) && event.getPlayer().isOp()) {
			plugin.getLogger().info(ChatColor.AQUA + "Server is stopping, sync everyone's inventory!");
			for(Player p : plugin.getServer().getOnlinePlayers()) {
				if(p.hasPermission("sync.do")) {
					SerializableInventory si = SerializableInventory.fromInventory(p, p.getInventory());
					si.commit();
				}
			}
			plugin.getLogger().info(ChatColor.AQUA + "Sync complete!");
		}
	}
}
