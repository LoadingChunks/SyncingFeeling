package net.LoadingChunks.SyncingFeeling;

/*
    This file is part of SyncingFeeling

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncingFeelingCommandExecutor implements CommandExecutor {

    private SyncingFeeling plugin;

    public SyncingFeelingCommandExecutor(SyncingFeeling plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (command.getName().equalsIgnoreCase("sync") && sender instanceof Player && sender.hasPermission("sync.do")) {
    		SerializableInventory si = SerializableInventory.fromInventory(((Player)sender), ((Player)sender).getInventory());
    		si.commit();
    		sender.sendMessage(ChatColor.AQUA + "Syncing your Inventory...");
    		return true;
    	}
    	
    	if(command.getName().equalsIgnoreCase("sf") && sender.hasPermission("sync.admin")) {	
    		if(args.length > 0 && args[0].equalsIgnoreCase("syncall")) {
    			for(Player p : plugin.getServer().getOnlinePlayers()) {
    				if(p.hasPermission("sync.do")) {
    					SerializableInventory si = SerializableInventory.fromInventory(p, p.getInventory());
    					si.commit();
    					sender.sendMessage(ChatColor.AQUA + "Committing Inventory: " + p.getDisplayName());
    				}
    			}
    			return true;
    		}
    		
    		if(args.length > 0 && args[0].equalsIgnoreCase("debug")) {
    			if(plugin.isDebugMode) {
    				plugin.isDebugMode = false;
    				sender.sendMessage(ChatColor.AQUA + "SyncingFeeling Debug Mode Disabled");
    			} else {
    				plugin.isDebugMode = true;
    				sender.sendMessage(ChatColor.AQUA + "SyncingFeeling Debug Mode Enabled");
    			}
    		}
    	}
        return false;
    }
}
