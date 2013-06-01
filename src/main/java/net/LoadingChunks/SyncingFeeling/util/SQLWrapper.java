package net.LoadingChunks.SyncingFeeling.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.LoadingChunks.SyncingFeeling.SyncingFeeling;
import net.LoadingChunks.SyncingFeeling.Inventory.SerializableInventory;

public class SQLWrapper {
	static private SyncingFeeling plugin;
	static private Connection con;
	static private boolean success;
	
	static private String user;
	static private String password;
	static private String host;
	static private String db;
	
	static public void setPlugin(SyncingFeeling plugin) {
		SQLWrapper.plugin = plugin;
	}
	
	static public void setConfig(String user, String password, String host, String db) {
		SQLWrapper.user = user;
		SQLWrapper.password = password;
		SQLWrapper.host = host;
		SQLWrapper.db = db;
	}
	
	public static SyncingFeeling getPlugin() {
		return plugin;
	}
	
	static public boolean connect() {
		success = true;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			SQLWrapper.con = DriverManager.getConnection("jdbc:mysql://" + SQLWrapper.host + ":3306/" + SQLWrapper.db, SQLWrapper.user, SQLWrapper.password);
		} catch(SQLException e) {
			e.printStackTrace();
			SQLWrapper.success = false;
		} catch (ClassNotFoundException e) { e.printStackTrace(); SQLWrapper.success = false; }
		
		return success;
	}
	
	static public void commitSlot(Player p, Integer slot, ItemStack stack) {
		try {
			PreparedStatement stat = con.prepareStatement("REPLACE INTO `inv_slots` (`server`,`player`,`json`,`slot`,`hash`) VALUES (?,?,?,?,MD5(?))");
			
			stat.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
			stat.setString(2, p.getName());
			JSONObject obj = new JSONObject();
			obj.putAll(stack.serialize());
			stat.setString(3, obj.toJSONString());
			stat.setInt(4, slot);
			
			stat.setString(5, obj.toJSONString());
			
			stat.execute();
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	static public void commitSlots(Player p, HashMap<Integer, ItemStack> slots) {
		try {
			String sqlstring = "REPLACE INTO `inv_slots` (`server`,`player`,`json`,`slot`,`hash`) VALUES ";
			
			if(slots.size() == 0)
				return;
			
			for(int i = 0; i < slots.size(); i++) {
				sqlstring = sqlstring.concat("(?,?,?,?,MD5(?))");
				
				if(i < slots.size()-1)
					sqlstring = sqlstring.concat(",");
			}
			
			PreparedStatement stat = con.prepareStatement(sqlstring);
			
			int i = 0;
			String name = SQLWrapper.plugin.getConfig().getString("general.server.name");
			for(Entry<Integer, ItemStack> slot : slots.entrySet()) {
				stat.setString((i*5) + 1, name);
				stat.setString((i*5) + 2, p.getName());
				JSONObject obj = new JSONObject();
				obj.putAll(slot.getValue().serialize());
				stat.setString((i*5) + 3, obj.toJSONString());
				stat.setInt((i*5) + 4, slot.getKey());
				stat.setString((i*5) + 5, obj.toJSONString());
				i++;
			}
			
			if(plugin.isDebugMode)
				plugin.getLogger().info("Committing Slot Array: " + stat.toString());
			
			stat.execute();
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	static public void commitInventory(SerializableInventory inv, Player p, boolean clear) {
		try {
			PreparedStatement stat = con.prepareStatement("REPLACE INTO `inv_inventories` (`server`,`player`,`hash`) VALUES (?,?,MD5(?))");
			
			stat.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
			stat.setString(2, p.getName());
			JSONObject obj = new JSONObject();
			obj.putAll(inv.serialize());
			stat.setString(3, obj.toJSONString());
			
			stat.execute();
			
			if(clear) {
				PreparedStatement statclear = con.prepareStatement("DELETE FROM `inv_slots` WHERE `server` = ? AND `player` = ?");
				statclear.setString(1, SQLWrapper.plugin.getConfig().getString("general.server.name"));
				statclear.setString(2, p.getName());
				statclear.execute();
			}
		} catch(SQLException e) { e.printStackTrace(); }
	}
	
	public static String checkLatest(Player p) {
		try {
			PreparedStatement stat = con.prepareStatement("SELECT `server`,`hash` FROM `inv_inventories` WHERE `player` = ? ORDER BY `timestamp` DESC LIMIT 1");
			stat.setString(1, p.getName());
			stat.execute();
			ResultSet set = stat.getResultSet();
			set.last();
			if(set.getRow() == 0)
				return "";
			
			return set.getString("server");
		} catch(SQLException e) { e.printStackTrace(); return SQLWrapper.plugin.getConfig().getString("general.server.name"); }
	}

	public static void recoverLatest(Player p, String server) {
		try {
			PreparedStatement stat = con.prepareStatement("SELECT * FROM `inv_slots` WHERE `server` = ? AND `player` = ?");
			stat.setString(1, server);
			stat.setString(2, p.getName());
			
			stat.execute();
			
			ResultSet result = stat.getResultSet();
			
			while(result.next()) {
				int slot = result.getInt("slot");
				JSONParser parser = new JSONParser();
				try {
					Map<String, Object> map = (Map<String, Object>) parser.parse(result.getString("json"));
					ItemStack stack = ItemStack.deserialize(map);
					
					if(slot == Slots.HELMET.slotNum()) {
						p.getInventory().setHelmet(stack);
					} else if(slot == Slots.CHEST.slotNum()) {
						p.getInventory().setChestplate(stack);
					} else if(slot == Slots.LEGGINGS.slotNum()) {
						p.getInventory().setLeggings(stack);
					} else if(slot == Slots.BOOTS.slotNum()) {
						p.getInventory().setBoots(stack);
					} else {
						p.getInventory().setItem(slot, stack);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch(SQLException e) { e.printStackTrace(); }
	}
}
