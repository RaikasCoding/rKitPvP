package ga.raikas.kitpvp;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.World;
import org.bukkit.Location;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Iterator;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.ArrayList;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.Listener;

public class A implements Listener
{
    static int loopedd;
    static String kitchoosed;
    static Inventory menu;
    
    static {
        A.loopedd = 0;
        A.kitchoosed = P.getInstance().getLangConfig().getString("lang.kits");
        A.menu = Bukkit.createInventory((InventoryHolder)null, 45, ChatColor.AQUA + A.kitchoosed);
        final FileConfiguration config = P.getInstance().getKitsConfig();
        final List<String> al = new ArrayList<String>();
        for (final String key : config.getConfigurationSection("kits").getKeys(false)) {
            al.add(config.getString("kits." + key + ".name"));
            final String st = config.getString("kits." + key + ".item").toUpperCase();
            final Material material = Material.getMaterial(st);
            final ItemStack itemstack = new ItemStack(material);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.WHITE + config.getString("kits." + key + ".name"));
            final ArrayList<String> lore = new ArrayList<String>();
            itemmeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_UNBREAKABLE });
            if (config.isSet("kits." + key + ".description")) {
                lore.add(ChatColor.WHITE + config.getString("kits." + key + ".description"));
                lore.add(" ");
                lore.add(new StringBuilder().append(ChatColor.WHITE).append(ChatColor.BOLD).append("Items: ").toString());
                for (final String nam : P.getInstance().getKitsConfig().getConfigurationSection("kits." + key + ".items").getKeys(false)) {
                    final int amount = P.getInstance().getKitsConfig().getInt("kits." + key + ".items." + nam + ".amount");
                    if (nam.startsWith("crackshot-")) {
                        final String name = P.getInstance().getKitsConfig().getString("kits." + key + ".items." + nam + ".guiname");
                        lore.add(new StringBuilder().append(ChatColor.WHITE).append(amount).append("x ").append(name).toString());
                    }
                    else {
                        lore.add(new StringBuilder().append(ChatColor.WHITE).append(amount).append("x ").append(nam).toString());
                    }
                }
            }
            itemmeta.setLore((List)lore);
            itemstack.setItemMeta(itemmeta);
            A.menu.setItem(A.loopedd, itemstack);
        }
    }
    
    private static String colorize(final String s) {
        return (s == null) ? null : ChatColor.translateAlternateColorCodes('&', s);
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
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        final ItemStack clicked = event.getCurrentItem();
        final InventoryClickEvent e = event;
        if (e.getAction().equals((Object)InventoryAction.PICKUP_ALL) || e.getAction().equals((Object)InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            event.setCancelled(true);
            player.closeInventory();
            final List<String> spawns = (List<String>)P.getInstance().getLocationsConfig().getStringList("spawns");
            if (spawns.size() == 0) {
                player.sendMessage("Admin of this server has to add one or more spawnpoints.");
                return;
            }
            player.getInventory().clear();
            for (final String key : P.getInstance().getKitsConfig().getConfigurationSection("kits").getKeys(false)) {
                if (clicked.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
                    String langReplace = P.getInstance().getLangConfig().getString("lang.kitchoosed");
                    langReplace = langReplace.replace("{kit}", clicked.getItemMeta().getDisplayName());
                    player.sendMessage(ChatColor.GRAY + langReplace);
                    for (final String material : P.getInstance().getKitsConfig().getConfigurationSection("kits." + key + ".items").getKeys(false)) {
                        final Material aterial = Material.getMaterial(material.toUpperCase());
                        final int amount = P.getInstance().getKitsConfig().getInt("kits." + key + ".items." + material + ".amount");
                        if (material.startsWith("crackshot-")) {
                            final String wn = P.getInstance().getKitsConfig().getString("kits." + key + ".items." + material + ".weapon");
                            final CSUtility cu = new CSUtility();
                            final ItemStack itemstack = cu.generateWeapon(wn);
                            itemstack.setAmount(amount);
                            player.getInventory().addItem(new ItemStack[] { itemstack });
                        }
                        else {
                            final ItemStack itemstack = new ItemStack(aterial, amount);
                            for (final String name : P.getInstance().getKitsConfig().getStringList("kits." + key + ".items." + material + ".enchants")) {
                                final String[] names = name.split(":");
                                itemstack.addEnchantment(getEnchant(names[0]), Integer.parseInt(names[1]));
                            }
                            if (P.getInstance().getKitsConfig().isSet("kits." + key + ".items." + material + ".name")) {
                                final String st = P.getInstance().getKitsConfig().getString("kits." + key + ".items." + material + ".name");
                                if (!st.equals(null)) {
                                    final ItemMeta im = itemstack.getItemMeta();
                                    im.setDisplayName(colorize(st));
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
        }
        else if (e.getAction().equals((Object)InventoryAction.PLACE_ALL) || e.getAction().equals((Object)InventoryAction.PLACE_ONE)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        P.getInstance().lobby(player);
    }
    
    @EventHandler
    public void respawn(final PlayerRespawnEvent e) {
        P.getInstance().lobby(e.getPlayer());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)P.getInstance(), (Runnable)new Runnable() {
            @Override
            public void run() {
                P.getInstance().teleportLobby(e.getPlayer());
            }
        }, 10L);
    }
    
    @EventHandler
    public void playMenu(final PlayerInteractEvent e) {
        if (e.getAction().equals((Object)Action.RIGHT_CLICK_AIR)) {
            final Player player = e.getPlayer();
            final ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().equals((Object)Material.IRON_SWORD) && item.containsEnchantment(Enchantment.ARROW_DAMAGE)) {
                player.openInventory(A.menu);
            }
        }
    }
}
