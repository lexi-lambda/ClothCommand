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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class ClothCommand extends JavaPlugin {
    
    private static final Logger log = Logger.getLogger("Minecraft");
    private String[] woolColors;
    private boolean requiresOp;
    private boolean usingPermissions;
    private int defaultStackSize;
    private int stackMultiplier;
    
    public static PermissionHandler permissions;

    @Override
    @SuppressWarnings("LoggerStringConcat")
    public void onDisable() {
        log.info(this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
    }

    @Override
    @SuppressWarnings("LoggerStringConcat")
    public void onEnable() {
        log.info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled.");
        this.setupPermissions();
        this.loadConfig();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!(sender instanceof Player)) return false;
        if(commandLabel.equalsIgnoreCase("cloth")){
            this.processCommand((Player)sender, args);
            return true;
        } else return false;
    }
    
    public void processCommand(Player player, String[] args){
        if(usingPermissions)
            if(!permissions.has(player, "ClothCommand.cloth")) return;
        
        if(!player.isOp() && !usingPermissions && requiresOp) return;
        
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")) listColors(player);
            this.giveCloth(player, args[0], defaultStackSize);
            return;
        } else if(args.length == 2){
            if(new Scanner(args[1]).hasNextInt()){
                this.giveCloth(player, args[0], Integer.parseInt(args[1]) * stackMultiplier);
                return;
            }
        } else if (args.length == 3){
            Player reciever = this.getServer().getPlayer(args[2]);
            if(reciever == null){
                player.sendMessage(ChatColor.RED + "Couldn't find player\"" + args[2] + "\".");
                return;
            }
            
            if(new Scanner(args[1]).hasNextInt()){
                this.giveCloth(reciever, args[0], Integer.parseInt(args[1]) * stackMultiplier);
            }
            return;
        }
        
        player.sendMessage(ChatColor.RED + "/cloth <color> [amount] [player]");
        player.sendMessage(ChatColor.RED + "/cloth list");
    }
    
    public void giveCloth(Player player, String color, int amt){
        if(color.equals("all")) this.giveAll(player, amt);
        if(amt > 0){
            int colorID = Arrays.asList(woolColors).indexOf(color);
            if(colorID != -1){
                player.getInventory().addItem(new ItemStack(Material.WOOL, amt, (byte)colorID));
            }
        } else player.sendMessage(ChatColor.RED + "[amount] must be greater than zero.");
    }
    
    private void giveAll(Player player, int amt){
        for(String color : woolColors){
            this.giveCloth(player, color, amt);
        }
    }
    
    private void listColors(Player player){
        String message = ChatColor.RED + "Possible colors names: ";
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
               log.info("Permissions not detected. Defaulting to OP restrictions.");
               usingPermissions = false;
               return;
           }
        }
        usingPermissions = true;
    }
    
    private void loadConfig(){
        Configuration config = this.getConfiguration();
        initConfig();
        
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
    }
    
    private void initConfig(){
        
        this.makeProperty("woolcolor.white", "white");
        this.makeProperty("woolcolor.orange", "orange");
        this.makeProperty("woolcolor.magenta", "magenta");
        this.makeProperty("woolcolor.lightblue", "lightblue");
        this.makeProperty("woolcolor.yellow", "yellow");
        this.makeProperty("woolcolor.lime", "lime");
        this.makeProperty("woolcolor.pink", "pink");
        this.makeProperty("woolcolor.gray", "gray");
        this.makeProperty("woolcolor.silver", "silver");
        this.makeProperty("woolcolor.cyan", "cyan");
        this.makeProperty("woolcolor.purple", "purple");
        this.makeProperty("woolcolor.blue", "blue");
        this.makeProperty("woolcolor.brown", "brown");
        this.makeProperty("woolcolor.green", "green");
        this.makeProperty("woolcolor.red", "red");
        this.makeProperty("woolcolor.black", "black");
        
        this.makeProperty("requiresop", true);
        this.makeProperty("defaultstacksize", 64);
        this.makeProperty("stackmultiplier", 1);
        
    }
    
    private void makeProperty(String property, Object value){
        if(this.getConfiguration().getProperty(property) == null){
            this.getConfiguration().setProperty(property, value);
            this.getConfiguration().save();
        }
    }
}
