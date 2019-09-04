package ga.raikas.kitpvp;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import net.md_5.bungee.api.ChatColor;

import com.shampaggon.crackshot.CSUtility;

public class A implements Listener{
	
	private static String colorize(String s) {
	     return s == null ? null : ChatColor.translateAlternateColorCodes('&', s);
	   } 
	static int loopedd = 0;
	
	public static Enchantment getEnchant(String name) {
		switch(name) {
			case "SHARPNESS":
				return Enchantment.DAMAGE_ALL;
			case "UNBREAKING":
				return Enchantment.DURABILITY;
		}
		return null;
	}
	static String kitchoosed = P.getInstance().getLangConfig().getString("lang.kits");
	
	static Inventory menu = Bukkit.createInventory(null, 45, ChatColor.AQUA + kitchoosed);
	static {
		
		FileConfiguration config = P.getInstance().getKitsConfig();
		
		List<String> al = new ArrayList<String>();
		for (String key : config.getConfigurationSection("kits").getKeys(false)) {
			
			al.add(config.getString("kits." + key + ".name"));
			String st = config.getString("kits." + key + ".item").toUpperCase();
			Material material = Material.getMaterial(st);
			ItemStack itemstack = new ItemStack(material);
			ItemMeta itemmeta = itemstack.getItemMeta();
			itemmeta.setDisplayName(ChatColor.WHITE + config.getString("kits." + key + ".name"));
			ArrayList<String> lore = new ArrayList<String>();
			itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			if(config.isSet("kits." + key + ".description")) {
			
				lore.add(ChatColor.WHITE + config.getString("kits." + key + ".description"));
				lore.add(" ");
				lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "Items: ");
				//List<String> lista = config.getStringList("kits." + key + ".items");
				for(String nam : P.getInstance().getKitsConfig().getConfigurationSection("kits." + key + ".items").getKeys(false)) {
					int amount = P.getInstance().getKitsConfig().getInt("kits." + key + ".items." + nam + ".amount");
					if(nam.startsWith("crackshot-")) {
						String name = P.getInstance().getKitsConfig().getString("kits."+ key + ".items." + nam + ".guiname");
						lore.add(ChatColor.WHITE + "" + amount + "x " + name);
					} else {
						
						lore.add(ChatColor.WHITE + "" + amount + "x " + nam);
					}
					
					
				}
				
			}
			
 			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			menu.setItem(loopedd, itemstack);
		}
		
		
		
		//The first parameter, is the slot that is assigned to. Starts counting at 0
		}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
	Player player = (Player) event.getWhoClicked(); // The player that clicked the item
	ItemStack clicked = event.getCurrentItem();
	InventoryClickEvent e = event;
	
	
	if((e.getAction().equals(InventoryAction.PICKUP_ALL)) || (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))) {

		event.setCancelled(true);
		player.closeInventory();
		List<String> spawns = P.getInstance().getLocationsConfig().getStringList("spawns");
		
		
		if(spawns.size() == 0) {player.sendMessage("Admin of this server has to add one or more spawnpoints.");return;}
		player.getInventory().clear();
		for (String key : P.getInstance().getKitsConfig().getConfigurationSection("kits").getKeys(false)) {
			
			if(clicked.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
				String langReplace = P.getInstance().getLangConfig().getString("lang.kitchoosed");
				//player.sendMessage(P.getInstance().getLangConfig().getString("lang.kitchoosed"));
				langReplace = langReplace.replace("{kit}", clicked.getItemMeta().getDisplayName());
				player.sendMessage(ChatColor.GRAY + langReplace);
				
				for(String material : P.getInstance().getKitsConfig().getConfigurationSection("kits." + key + ".items").getKeys(false)) {
					ItemStack itemstack;
					Material aterial = Material.getMaterial(material.toUpperCase());
					int amount = P.getInstance().getKitsConfig().getInt("kits." + key + ".items." + material + ".amount");
					if(material.startsWith("crackshot-")) {
						//player.sendMessage(material);
						String wn = P.getInstance().getKitsConfig().getString("kits." + key + ".items." + material + ".weapon");
						CSUtility cu = new CSUtility();
						itemstack = cu.generateWeapon(wn);
						itemstack.setAmount(amount);
						player.getInventory().addItem(itemstack);
					} else {
						
					itemstack = new ItemStack(aterial, amount);
					for(String name : P.getInstance().getKitsConfig().getStringList("kits." + key + ".items." + material + ".enchants")) {
						String[] names = name.split(":");
						itemstack.addEnchantment(getEnchant(names[0]), Integer.parseInt(names[1]));
					}
					if(P.getInstance().getKitsConfig().isSet("kits." + key + ".items." + material + ".name")) {
					String st = P.getInstance().getKitsConfig().getString("kits." + key + ".items." + material + ".name");
					if(!st.equals(null)) {
						ItemMeta im = itemstack.getItemMeta();
						im.setDisplayName(colorize(st));
						itemstack.setItemMeta(im);
					}
					}
					//player.sendMessage("" +P.getInstance().getKitsConfig().getStringList("kits." + key + ".items." + material).size());
					player.getInventory().addItem(itemstack);
					}
					
					
					
					
				}
				
				double intti = Math.random();
				intti = intti*spawns.size();
				int in = (int) intti;
				String lokaatio = spawns.get(in);
				String[] locationsplit = lokaatio.split(",");
				World world = Bukkit.getServer().getWorld(locationsplit[0]);
				player.teleport(new Location(world, Integer.parseInt(locationsplit[1]) +0.5, Integer.parseInt(locationsplit[2]) +0.5, Integer.parseInt(locationsplit[3]) +0.5));
			}
		}
	} else if((e.getAction().equals(InventoryAction.PLACE_ALL)) || (e.getAction().equals(InventoryAction.PLACE_ONE))) {
		event.setCancelled(true);
	}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		P.getInstance().lobby(player);
	}
	
	@EventHandler
	public void respawn(PlayerRespawnEvent e) {
		P.getInstance().lobby(e.getPlayer());
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(P.getInstance(), new Runnable() {
			public void run() {
				P.getInstance().teleportLobby(e.getPlayer());
			}
		}, 10);
		
	}
	@EventHandler
	public void sign(SignChangeEvent e) {
		
		if(e.getLine(0).equals("[Kit]")) {
			e.setLine(0, ChatColor.RED + "[Kit]");
			
		}
	}
	@EventHandler
	public void signRightclick(PlayerInteractEvent e) {
		Player player =  e.getPlayer();
		Block block = e.getClickedBlock();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(block != null && block.getState() instanceof Sign) 
			{
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				if(lines[0].equals(ChatColor.RED + "[Kit]")) {
					P.getInstance().giveKit(player, lines[1]);
				}
				
			}
		}
				
 		
	}
	@EventHandler
	public void playMenu(PlayerInteractEvent e) {
		
		if((e.getAction().equals(Action.RIGHT_CLICK_AIR))) {
			Player player = e.getPlayer();
			ItemStack item = player.getInventory().getItemInMainHand();
			if((item.getType().equals(Material.IRON_SWORD)) && (item.containsEnchantment(Enchantment.ARROW_DAMAGE))) {
				
				player.openInventory(menu);
			}
		}
	}
}
