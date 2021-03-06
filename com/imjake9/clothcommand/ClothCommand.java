package com.imjake9.clothcommand;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class ClothCommand extends JavaPlugin {
    
    private static final Logger log = Logger.getLogger("Minecraft");
    private String[] woolColors;
    private boolean requiresOp;
    private int defaultStackSize;
    private int stackMultiplier;
    private boolean useBukkitPermissions = true;
    
    public static PermissionHandler permissions;

    @Override
    @SuppressWarnings("LoggerStringConcat")
    public void onDisable() {
        log.info("[ClothCommand] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
    }

    @Override
    @SuppressWarnings("LoggerStringConcat")
    public void onEnable() {
        log.info("[ClothCommand] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled.");
        this.loadConfig();
        if(!useBukkitPermissions)this.setupPermissions();
        this.registerPermissions();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!(sender instanceof Player) && args.length != 3) return false;
        this.processCommand(sender, args);
        return true;
    }
    
    public void processCommand(CommandSender player, String[] args){
        if(!hasPermission(player, "ClothCommand.cloth")) return;
        
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")) {
                listColors(player);
                return;
            }
            this.validateGive(player, (Player)player, args[0], defaultStackSize);
            return;
        } else if(args.length == 2){
            if(new Scanner(args[1]).hasNextInt()){
                this.validateGive(player, (Player)player, args[0], Integer.parseInt(args[1]) * stackMultiplier);
                return;
            }
        } else if (args.length == 3){
            if(!hasPermission(player, "ClothCommand.cloth.player")) return;
            Player reciever = this.getServer().getPlayer(args[2]);
            if(reciever == null){
                player.sendMessage(ChatColor.RED + "Couldn't find player\"" + args[2] + "\".");
                return;
            }
            
            if(new Scanner(args[1]).hasNextInt()){
                this.validateGive(player, reciever, args[0], Integer.parseInt(args[1]) * stackMultiplier);
            }
            return;
        }
        
        player.sendMessage(ChatColor.RED + "/cloth <color> [amount] [player]");
        player.sendMessage(ChatColor.RED + "/cloth list");
    }
    
    public void validateGive(CommandSender sender, Player player, String color, int amt) {
        if(amt == -1) {
            if(!hasPermission(player, "ClothCommand.cloth.unlimited")) {
                sender.sendMessage(ChatColor.RED + "[amount] must be greater than zero.");
                return;
            } else giveCloth(sender, player, color, amt);
        } else if(amt > 0) giveCloth(sender, player, color, amt);
        else sender.sendMessage(ChatColor.RED + "[amount] must be greater than zero.");
    }
    
    public void giveCloth(CommandSender sender, Player player, String color, int amt){
        if(color.equals("all")) this.giveAll(sender, player, amt);
        else {
            int colorID = Arrays.asList(woolColors).indexOf(color);
            if(colorID != -1){
                player.getInventory().addItem(new ItemStack(Material.WOOL, amt, (byte)colorID));
            } else sender.sendMessage(ChatColor.RED + "Invalid color. Type /cloth list for a list of colors.");
        }
    }
    
    private void giveAll(CommandSender sender, Player player, int amt){
        for(String color : woolColors){
            this.giveCloth(sender, player, color, amt);
        }
    }
    
    private void listColors(CommandSender player){
        String message = ChatColor.RED + "Possible color names: ";
        for (int i=0; i < woolColors.length; i++){
            message +=  woolColors[i] + ", ";
        }
        message += "all.";
        player.sendMessage(message);
    }
    
    private void setupPermissions() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
        
        if(permissions == null){
           if(plugin != null){
               permissions = ((Permissions)plugin).getHandler();
           } else {
               log.info("[ClothCommand] Permissions not detected. Defaulting to Bukkit permissions.");
               return;
           }
        }
    }
    
    private void loadConfig(){
        Configuration config = this.getConfiguration();
        
        woolColors = new String[] {
            config.getString("woolcolor.white", "white"),
            config.getString("woolcolor.orange", "orange"),
            config.getString("woolcolor.magenta", "magenta"),
            config.getString("woolcolor.lightblue", "lightblue"),
            config.getString("woolcolor.yellow", "yellow"),
            config.getString("woolcolor.lime", "lime"),
            config.getString("woolcolor.pink", "pink"),
            config.getString("woolcolor.gray", "gray"),
            config.getString("woolcolor.silver", "silver"),
            config.getString("woolcolor.cyan", "cyan"),
            config.getString("woolcolor.purple", "purple"),
            config.getString("woolcolor.blue", "blue"),
            config.getString("woolcolor.brown", "brown"),
            config.getString("woolcolor.green", "green"),
            config.getString("woolcolor.red", "red"),
            config.getString("woolcolor.black", "black")
        };
        
        requiresOp = config.getBoolean("requiresop", true);
        defaultStackSize = config.getInt("defaultstacksize", 64);
        stackMultiplier = config.getInt("stackmultiplier", 1);
        useBukkitPermissions = config.getBoolean("usebukkitpermissions", (config.getProperty("requiresop") == null));
        
        config.save();
    }
    
    private void registerPermissions() {
        PluginManager pm = this.getServer().getPluginManager();
        PermissionDefault op = (requiresOp) ? PermissionDefault.OP : PermissionDefault.TRUE;
        pm.addPermission(new Permission("ClothCommand.cloth", op));
        pm.addPermission(new Permission("ClothCommand.cloth.player", op));
        pm.addPermission(new Permission("ClothCommand.cloth.unlimited", op));
    }
    
    public boolean hasPermission(CommandSender sender, String permission) {
        if(!(sender instanceof Player)) return true;
        Player player = (Player)sender;
        if(useBukkitPermissions || permissions == null) {
            return player.hasPermission(permission);
        } else {
            return permissions.has(player, permission);
        }
    }
}
