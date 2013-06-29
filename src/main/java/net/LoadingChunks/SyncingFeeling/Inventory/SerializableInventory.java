package net.LoadingChunks.SyncingFeeling.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.LoadingChunks.SyncingFeeling.util.SQLWrapper;
import net.LoadingChunks.SyncingFeeling.util.Slots;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SerializableInventory {
	private HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
	private Player player;
	
	public static SerializableInventory fromInventory(Player p, PlayerInventory inv) {
		SerializableInventory ret = new SerializableInventory();
		ret.player = p;
		Integer i = 0;
		for(ItemStack stack : inv.getContents()) {
			ret.addStack(i, stack);
			i++;
		}
		
		ret.addStack(Slots.HELMET.slotNum(), inv.getHelmet());
		ret.addStack(Slots.CHEST.slotNum(), inv.getChestplate());
		ret.addStack(Slots.LEGGINGS.slotNum(), inv.getLeggings());
		ret.addStack(Slots.BOOTS.slotNum(), inv.getBoots());

		return ret;
	}
	
	void addStack(Integer i, ItemStack stack) {
		if(stack != null)
			this.slots.put(i, stack);
	}
	
	public HashMap<Integer, ItemStack> getSlots() {
		return slots;
	}
	
	public void commit() {
		this.commitInventory(true);
		SQLWrapper.commitSlots(this.player, this);
	}
	
	public void commitInventory(boolean clear) {
		SQLWrapper.commitInventory(this, player, clear);
	}
	
	public HashMap<Integer, Object> serialize() {
		HashMap<Integer, Object> inv = new HashMap<Integer, Object>();
		
		for(Entry<Integer, ItemStack> stack : slots.entrySet()) {
			inv.put(stack.getKey(), stack.getValue().serialize());
		}
		
		return inv;
	}
	
	public static void recover(Player p) {
		String latest = SQLWrapper.checkLatest(p);
		if(!latest.equalsIgnoreCase(SQLWrapper.getPlugin().getConfig().getString("general.server.name")) && !latest.equals((""))) {
			p.sendMessage(ChatColor.AQUA + "Updating your inventory with data from another zone, please wait...");
			SQLWrapper.recoverLatest(p, latest);
			p.sendMessage(ChatColor.AQUA + "Your inventory has been updated!");
		}
	}
	
	public static List<Map<String, Object>> serializeItemList(List<ConfigurationSerializable> list) {
	    List<Map<String, Object>> returnVal = new ArrayList<Map<String, Object>>();
	    for (ConfigurationSerializable cs : list) {
	        returnVal.add(serialize(cs));
	    }
	    return returnVal;
	}
	 
	public static Map<String, Object> serialize(ConfigurationSerializable cs) {
	    Map<String, Object> serialized = recreateMap(cs.serialize());
	    for (Entry<String, Object> entry : serialized.entrySet()) {
	        if (entry.getValue() instanceof ConfigurationSerializable) {
	            entry.setValue(serialize((ConfigurationSerializable)entry.getValue()));
	        }
	    }
	    serialized.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
	    return serialized;
	}
	 
	public static Map<String, Object> recreateMap(Map<String, Object> original) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    for (Entry<String, Object> entry : original.entrySet()) {
	        map.put(entry.getKey(), entry.getValue());
	    }
	    return map;
	}
	
	@SuppressWarnings("unchecked")
	public static ConfigurationSerializable deserialize(Map<String, Object> map) {
	    for (Entry<String, Object> entry : map.entrySet()) {
	    // Check if any of its sub-maps are ConfigurationSerializable. They need to be done first.
	        if (entry.getValue() instanceof Map && ((Map)entry.getValue()).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
	            entry.setValue(deserialize((Map)entry.getValue()));
	        }
	    }
	    return ConfigurationSerialization.deserializeObject(map);
	}
	 
	public static List<ConfigurationSerializable> deserializeItemList(List<Map<String, Object>> itemList) {
	    List<ConfigurationSerializable> returnVal = new ArrayList<ConfigurationSerializable>();
	    for (Map<String, Object> map : itemList) {
	        returnVal.add(deserialize(map));
	    }
	    return returnVal;
	}
}
