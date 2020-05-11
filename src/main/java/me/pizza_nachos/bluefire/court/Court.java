package me.pizza_nachos.bluefire.court;

import me.pizza_nachos.bluefire.court.commands.sue;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static java.lang.Integer.parseInt;

public final class Court extends JavaPlugin implements Listener {
    ArrayList<Player> judges = new ArrayList<Player>();

    private int tally = 0;
    private int votes = 0;
    private int sueAmmount;
    private String reason;

    Location courtLocation;

    private Player prosecuter;
    private Player defendednt;
    private Player judge;

    public Court(Player p, Player d, int amount, String r) {
        prosecuter = p;
        defendednt = d;
        sueAmmount = amount;
        reason = r;

        selectJudge();



    }

    private void selectJudge(){
        int timeOutCounter = 0;
        Random rand = new Random();
        while(!judge.isOnline()){
            timeOutCounter++;
            judge = judges.get(rand.nextInt(judges.size()));
            if(timeOutCounter >= 10){ Bukkit.broadcastMessage("Unable to find a suitable judge..."); return;}
        }

        judge.sendMessage("You are the judge! Do you want to hear this case? </courtyes /courtno>");
    }




    private static Economy econ = null;


    @Override
    public void onEnable() {
        getCommand("sue").setExecutor(new sue());


        // Plugin startup logic
        if (!setupEconomy()) {
            this.getLogger().severe("Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        System.out.println("Court Plugin version 1.0.1 BETA is Starting");
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("courtaddjudge")) {
            if (args.length != 1) {
                return false;
            }

            Player newJudge = Bukkit.getPlayer(args[0]);
            judges.add(newJudge);
        }
        if (command.getName().equals("courtyes")) {
            if (args.length != 0 || sender != judge) {
                return false;
            }
            Bukkit.broadcastMessage(sender.getName() + " is the presiding judge, court is in session");
            startCourt();
        }
        if (command.getName().equals("courtno")) {
            if (args.length != 0 || sender != judge) {
                return false;
            }
            Bukkit.broadcastMessage("The judge has thrown out the case...");
        }
        if (command.getName().equals("setCourtLocation")) {
            if (args.length != 4) {
                return false;
            }
            courtLocation = new Location(Bukkit.getWorld(args[0]), parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));
        }


        return super.onCommand(sender, command, label, args);
    }

    public void startCourt(){
        int numberOfJurers = Bukkit.getOnlinePlayers().size() / 2;
        if(numberOfJurers < 2){Bukkit.broadcastMessage("Not enough players for a jury..."); return; }

        ArrayList<Player> jurors = new ArrayList<Player>();

        Random rand = new Random();
        Object[] OnlinePlayers = Bukkit.getOnlinePlayers().toArray();
        while(jurors.size() < numberOfJurers){
            Player newJuror = (Player)OnlinePlayers[rand.nextInt(OnlinePlayers.length)];
            if(newJuror != defendednt && newJuror != prosecuter && newJuror != judge){
                newJuror.sendMessage("You have been selected to be part of the jury");
                jurors.add(newJuror);
            }
        }

        ArrayList<Player> courtPersons = new ArrayList<>();
        courtPersons.addAll(jurors);
        courtPersons.add(judge);
        courtPersons.add(prosecuter);
        courtPersons.add(defendednt);

        for(int i = 0; i < courtPersons.size(); ++i){
            courtPersons.get(i).teleport(courtLocation);
        }

    }
}