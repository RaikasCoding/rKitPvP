package ga.raikas.kitpvp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.shampaggon.crackshot.CSUtility;

import net.md_5.bungee.api.ChatColor;
public class P extends JavaPlugin{
	public static P instance;
	
	
	public void onEnable() {
		instance = this;
		createKitsConfig();
		createLocationConfig();
		createLangConfig();
		Bukkit.getServer().getPluginManager().registerEvents(new A(), this);
		getLogger().info("rKitPvP has been enabled");
	}
	public static Enchantment getEnchant(String name) {
		switch(name) {
			case "SHARPNESS":
				return Enchantment.DAMAGE_ALL;
			case "UNBREAKING":
				return Enchantment.DURABILITY;
		}
		return null;
	}
	public static P getInstance() {
		return instance;
	}
	
	public void giveKit(Player player, String ki) {
		String kit = ki.toLowerCase();
		List<String> spawns = this.getLocationsConfig().getStringList("spawns");
		for(String material : this.getKitsConfig().getConfigurationSection("kits." + kit + ".items").getKeys(false)) {
			ItemStack itemstack;
			Material aterial = Material.getMaterial(material.toUpperCase());
			int amount = P.getInstance().getKitsConfig().getInt("kits." + kit + ".items." + material + ".amount");
			if(material.startsWith("crackshot-")) {
				//player.sendMessage(material);
				String wn = P.getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".weapon");
				CSUtility cu = new CSUtility();
				itemstack = cu.generateWeapon(wn);
				itemstack.setAmount(amount);
				player.getInventory().addItem(itemstack);
			} else {
				
			itemstack = new ItemStack(aterial, amount);
			for(String name : P.getInstance().getKitsConfig().getStringList("kits." + kit + ".items." + material + ".enchants")) {
				String[] names = name.split(":");
				itemstack.addEnchantment(getEnchant(names[0]), Integer.parseInt(names[1]));
			}
			if(P.getInstance().getKitsConfig().isSet("kits." + kit + ".items." + material + ".name")) {
			String st = P.getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".name");
			if(!st.equals(null)) {
				ItemMeta im = itemstack.getItemMeta();
				im.setDisplayName(st);
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
	public void lobby(Player player) {
		player.getInventory().clear();
		ItemStack is = new ItemStack(Material.IRON_SWORD);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + getLangConfig().getString("lang.choosekit"));
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.WHITE + getLangConfig().getString("lang.choosekit-description"));
		im.setLore(lore);
		im.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
		is.setItemMeta(im);
		teleportLobby(player);
		player.getInventory().setItem(0, is);
		
	}
	public void teleportLobby(Player player) {
		if(!getLocationsConfig().isSet("lobby")) { player.sendMessage(ChatColor.RED + "Admin of this server has to set a place for lobby");return;}
		String lokaatio = getLocationsConfig().getString("lobby");
		String[] locationsplit = lokaatio.split(",");
		World world = Bukkit.getServer().getWorld(locationsplit[0]);
		player.teleport(new Location(world, Integer.parseInt(locationsplit[1]) +0.5, Integer.parseInt(locationsplit[2])+0.5, Integer.parseInt(locationsplit[3]) +0.5));
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	   {
		String command = cmd.getName();
		
		Player player = (Player) sender;
		if(command.equalsIgnoreCase("rkitpvp")) {
			List<String> spawns = P.getInstance().getLocationsConfig().getStringList("spawns");
			if(args.length == 0) {
				player.sendMessage("/rkitpvp kit <kit>");
				return true;
			}
			if(args[0].equalsIgnoreCase("kit")) {
				if(args.length > 1) {
					String kit = args[1].toLowerCase();
					if(!getKitsConfig().getConfigurationSection("kits").getKeys(false).contains(kit)) {
						player.sendMessage(getLangConfig().getString("lang.kitnotfound"));
					} else {
					for(String material : P.getInstance().getKitsConfig().getConfigurationSection("kits." + kit + ".items").getKeys(false)) {
						ItemStack itemstack;
						Material aterial = Material.getMaterial(material.toUpperCase());
						int amount = P.getInstance().getKitsConfig().getInt("kits." + kit + ".items." + material + ".amount");
						if(material.startsWith("crackshot-")) {
							//player.sendMessage(material);
							String wn = P.getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".weapon");
							CSUtility cu = new CSUtility();
							itemstack = cu.generateWeapon(wn);
							itemstack.setAmount(amount);
							player.getInventory().addItem(itemstack);
						} else {
							
						itemstack = new ItemStack(aterial, amount);
						for(String name : P.getInstance().getKitsConfig().getStringList("kits." + kit + ".items." + material + ".enchants")) {
							String[] names = name.split(":");
							itemstack.addEnchantment(getEnchant(names[0]), Integer.parseInt(names[1]));
						}
						if(P.getInstance().getKitsConfig().isSet("kits." + kit + ".items." + material + ".name")) {
						String st = P.getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".name");
						if(!st.equals(null)) {
							ItemMeta im = itemstack.getItemMeta();
							im.setDisplayName(st);
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
				} else {
					player.sendMessage(ChatColor.BOLD + getLangConfig().getString("lang.kitsavailable"));
				}
					for (String key : P.getInstance().getKitsConfig().getConfigurationSection("kits").getKeys(false)) {
                    player.sendMessage(getKitsConfig().getString("kits."+key+".name"));
				}
					player.sendMessage("-x-x-x-x-x-x-x-x-x");
			}
			else if(args[0].equalsIgnoreCase("admin")) {
				if(!player.hasPermission("rkitpvp.admin")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to execute that command");
				} else {
					if(args[1].equalsIgnoreCase("setlobby")) {
						getLocationsConfig().set("lobby", player.getWorld().getName() + "," + player.getLocation().getBlockX() + ","+player.getLocation().getBlockY()+","+player.getLocation().getBlockZ());
						player.sendMessage(getLangConfig().getString("lang.lobbyset"));
						try {
							getLocationsConfig().save(customLocationFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else if(args[1].equalsIgnoreCase("addspawn")) {
						List<String> lista = getLocationsConfig().getStringList("spawns");
						lista.add(player.getWorld().getName() + "," + player.getLocation().getBlockX() + ","+player.getLocation().getBlockY()+","+player.getLocation().getBlockZ());
						getLocationsConfig().set("spawns", lista);
						player.sendMessage(getLangConfig().getString("lang.spawnadded"));
						try {
							getLocationsConfig().save(customLocationFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else {
						player.sendMessage("Syntax Error! \n/rkitpvp admin setlobby/addspawn");
					}
				}
			} else {
				player.sendMessage("Syntax Error! /rkitpvp (kit <name>|admin <setlobby/addspawn>)");
			}
			return true;
		}
		return false;
	   }
	private File customKitsFile;
	private FileConfiguration customConfig;
	public FileConfiguration getKitsConfig() {
		return customConfig;
	}
	
	
	public void createKitsConfig() {
		customKitsFile = new File(getDataFolder(), "kits.yml");
		if(!customKitsFile.exists()) {
			customKitsFile.getParentFile().mkdirs();
			saveResource("kits.yml", false);
		}
	
		customConfig= new YamlConfiguration();
		try {
			customConfig.load(customKitsFile);
		} catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	private File customLocationFile;
	private FileConfiguration customLocationConfig;
	public FileConfiguration getLocationsConfig() {
		return customLocationConfig;
	}
	
	
	public void createLocationConfig() {
		customLocationFile = new File(getDataFolder(), "locations.yml");
		if(!customLocationFile.exists()) {
			customLocationFile.getParentFile().mkdirs();
			saveResource("locations.yml", false);
		}
	
		customLocationConfig= new YamlConfiguration();
		try {
			customLocationConfig.load(customLocationFile);
		} catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	private File customLangFile;
	private FileConfiguration customLangConfig;
	public FileConfiguration getLangConfig() {
		return customLangConfig;
	}
	
	
	public void createLangConfig() {
		customLangFile = new File(getDataFolder(), "lang.yml");
		if(!customLangFile.exists()) {
			customLangFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
	
		customLangConfig= new YamlConfiguration();
		try {
			customLangConfig.load(customLangFile);
		} catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
			
}
