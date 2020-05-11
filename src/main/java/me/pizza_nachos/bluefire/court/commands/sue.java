package me.pizza_nachos.bluefire.court.commands;

import me.pizza_nachos.bluefire.court.Court;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;

public class sue implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length != 3 || !(sender instanceof Player)){
            return false;
        }

        Player prosecuter = (Player) sender;
        Player defendent = Bukkit.getPlayer(args[0]);
        Economy e = Court.getEconomy();
        int ammount = parseInt(args[2]);
        String reason = args[1];

        double maxPercent;

        if(reason.compareToIgnoreCase("stealing") == 0){
            maxPercent = .25;
        } else if(reason.compareToIgnoreCase("griefing") == 0){
            maxPercent = .40;
        } else if(reason.compareToIgnoreCase("borderdispute")== 0){
            maxPercent = .10;
        } else {
            maxPercent = .05;
        }

        if((e.getBalance(defendent)*maxPercent <= ammount)){
            prosecuter.sendMessage("Your suing amount is too high, can only be %" + maxPercent*100 + " of defendents wealth");
            return false;
        }
        if((e.getBalance(prosecuter) < 1000)){
            prosecuter.sendMessage("You do not have the money to pay for court fees");
            return false;
        }


        Bukkit.broadcastMessage(sender + " is suing " + args[0] + " for " + args[1] + " for $" + args[2]);
        Court c = new Court(prosecuter, defendent, ammount, reason);


        return false;
    }
}
