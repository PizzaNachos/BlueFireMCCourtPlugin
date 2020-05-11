package me.pizza_nachos.bluefire.court;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public final class Court extends JavaPlugin implements Listener{

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Court Plugin is Starting");
        getLogger().info("Court Plugin is Starting");
    }

    @EventHandler
    public void onSue(){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("sue")){
            if(args.length == 0 || !(sender instanceof Player)){
                return false;
            }
            Player SenderPlayer  = (Player) sender;
            SenderPlayer.sendMessage("You have tried to sue " + args[1]);
        }

        return super.onCommand(sender, command, label, args);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
