package ga.raikas.kitpvp;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.Iterator;
import java.io.IOException;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.ItemFlag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class P extends JavaPlugin
{
    private File customKitsFile;
    private FileConfiguration customConfig;
    public static P instance;
    private File customLocationFile;
    private FileConfiguration customLocationConfig;
    private File customLangFile;
    private FileConfiguration customLangConfig;
    
    public void onEnable() {
        this.createKitsConfig();
        this.createLocationConfig();
        this.createLangConfig();
        Bukkit.getServer().getPluginManager().registerEvents((Listener)new A(), (Plugin)this);
        this.getLogger().info("rKitPvP has been enabled");
    }
    
    public static Enchantment getEnchant(final String name) {
        switch (name) {
            case "SHARPNESS": {
                return Enchantment.DAMAGE_ALL;
            }
            case "UNBREAKING": {
                return Enchantment.DURABILITY;
            }
            default:
                break;
        }
        return null;
    }
    
    public void lobby(final Player player) {
        player.getInventory().clear();
        final ItemStack is = new ItemStack(Material.IRON_SWORD);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(new StringBuilder().append(ChatColor.AQUA).append(ChatColor.BOLD).append(this.getLangConfig().getString("lang.choosekit")).toString());
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
        final ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.WHITE + this.getLangConfig().getString("lang.choosekit-description"));
        im.setLore((List)lore);
        im.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        is.setItemMeta(im);
        this.teleportLobby(player);
        player.getInventory().setItem(0, is);
    }
    
    public void teleportLobby(final Player player) {
        if (!this.getLocationsConfig().isSet("lobby")) {
            player.sendMessage(ChatColor.RED + "Admin of this server has to set a place for lobby");
            return;
        }
        final String lokaatio = this.getLocationsConfig().getString("lobby");
        final String[] locationsplit = lokaatio.split(",");
        final World world = Bukkit.getServer().getWorld(locationsplit[0]);
        player.teleport(new Location(world, Integer.parseInt(locationsplit[1]) + 0.5, Integer.parseInt(locationsplit[2]) + 0.5, Integer.parseInt(locationsplit[3]) + 0.5));
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final String command = cmd.getName();
        final Player player = (Player)sender;
        if (!command.equalsIgnoreCase("rkitpvp")) {
            return false;
        }
        final List<String> spawns = (List<String>)getInstance().getLocationsConfig().getStringList("spawns");
        if (args.length == 0) {
            player.sendMessage("/rkitpvp kit <kit>");
            return true;
        }
        if (args[0].equalsIgnoreCase("kit")) {
            if (args.length > 1) {
                final String kit = args[1].toLowerCase();
                if (!this.getKitsConfig().getConfigurationSection("kits").getKeys(false).contains(kit)) {
                    player.sendMessage(this.getLangConfig().getString("lang.kitnotfound"));
                }
                else {
                    for (final String material : getInstance().getKitsConfig().getConfigurationSection("kits." + kit + ".items").getKeys(false)) {
                        final Material aterial = Material.getMaterial(material.toUpperCase());
                        final int amount = getInstance().getKitsConfig().getInt("kits." + kit + ".items." + material + ".amount");
                        if (material.startsWith("crackshot-")) {
                            final String wn = getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".weapon");
                            final CSUtility cu = new CSUtility();
                            final ItemStack itemstack = cu.generateWeapon(wn);
                            itemstack.setAmount(amount);
                            player.getInventory().addItem(new ItemStack[] { itemstack });
                        }
                        else {
                            final ItemStack itemstack = new ItemStack(aterial, amount);
                            for (final String name : getInstance().getKitsConfig().getStringList("kits." + kit + ".items." + material + ".enchants")) {
                                final String[] names = name.split(":");
                                itemstack.addEnchantment(getEnchant(names[0]), Integer.parseInt(names[1]));
                            }
                            if (getInstance().getKitsConfig().isSet("kits." + kit + ".items." + material + ".name")) {
                                final String st = getInstance().getKitsConfig().getString("kits." + kit + ".items." + material + ".name");
                                if (!st.equals(null)) {
                                    final ItemMeta im = itemstack.getItemMeta();
                                    im.setDisplayName(st);
                                    itemstack.setItemMeta(im);
                                }
                            }
                            player.getInventory().addItem(new ItemStack[] { itemstack });
                        }
                    }
                    double intti = Math.random();
                    intti *= spawns.size();
                    final int in = (int)intti;
                    final String lokaatio = spawns.get(in);
                    final String[] locationsplit = lokaatio.split(",");
                    final World world = Bukkit.getServer().getWorld(locationsplit[0]);
                    player.teleport(new Location(world, Integer.parseInt(locationsplit[1]) + 0.5, Integer.parseInt(locationsplit[2]) + 0.5, Integer.parseInt(locationsplit[3]) + 0.5));
                }
            }
            else {
                player.sendMessage(ChatColor.BOLD + this.getLangConfig().getString("lang.kitsavailable"));
            }
            for (final String key : getInstance().getKitsConfig().getConfigurationSection("kits").getKeys(false)) {
                player.sendMessage(this.getKitsConfig().getString("kits." + key + ".name"));
            }
            player.sendMessage("-x-x-x-x-x-x-x-x-x");
        }
        else if (args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("rkitpvp.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute that command");
            }
            else {
                if (args[1].equalsIgnoreCase("setlobby")) {
                    this.getLocationsConfig().set("lobby", (Object)(String.valueOf(player.getWorld().getName()) + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()));
                    player.sendMessage(this.getLangConfig().getString("lang.lobbyset"));
                    try {
                        this.getLocationsConfig().save(this.customLocationFile);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                if (args[1].equalsIgnoreCase("addspawn")) {
                    final List<String> lista = (List<String>)this.getLocationsConfig().getStringList("spawns");
                    lista.add(String.valueOf(player.getWorld().getName()) + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ());
                    this.getLocationsConfig().set("spawns", (Object)lista);
                    player.sendMessage(this.getLangConfig().getString("lang.spawnadded"));
                    try {
                        this.getLocationsConfig().save(this.customLocationFile);
                    }
                    catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return true;
                }
                player.sendMessage("Syntax Error! \n/rkitpvp admin setlobby/addspawn");
            }
        }
        else {
            player.sendMessage("Syntax Error! /rkitpvp (kit <name>|admin <setlobby/addspawn>)");
        }
        return true;
    }
    
    public FileConfiguration getKitsConfig() {
        return this.customConfig;
    }
    
    public P() {
        P.instance = this;
    }
    
    public static P getInstance() {
        return P.instance;
    }
    
    public void createKitsConfig() {
        this.customKitsFile = new File(this.getDataFolder(), "kits.yml");
        if (!this.customKitsFile.exists()) {
            this.customKitsFile.getParentFile().mkdirs();
            this.saveResource("kits.yml", false);
        }
        this.customConfig = (FileConfiguration)new YamlConfiguration();
        try {
            this.customConfig.load(this.customKitsFile);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
    }
    
    public FileConfiguration getLocationsConfig() {
        return this.customLocationConfig;
    }
    
    public void createLocationConfig() {
        this.customLocationFile = new File(this.getDataFolder(), "locations.yml");
        if (!this.customLocationFile.exists()) {
            this.customLocationFile.getParentFile().mkdirs();
            this.saveResource("locations.yml", false);
        }
        this.customLocationConfig = (FileConfiguration)new YamlConfiguration();
        try {
            this.customLocationConfig.load(this.customLocationFile);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
    }
    
    public FileConfiguration getLangConfig() {
        return this.customLangConfig;
    }
    
    public void createLangConfig() {
        this.customLangFile = new File(this.getDataFolder(), "lang.yml");
        if (!this.customLangFile.exists()) {
            this.customLangFile.getParentFile().mkdirs();
            this.saveResource("lang.yml", false);
        }
        this.customLangConfig = (FileConfiguration)new YamlConfiguration();
        try {
            this.customLangConfig.load(this.customLangFile);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
    }
}
