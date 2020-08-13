package me.pizza_nachos.bluefire.court;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Integer.parseInt;

public final class Court extends JavaPlugin implements Listener {
    ArrayList<Player> possibleJudges = new ArrayList<>();
    ArrayList<Player> jurors = new ArrayList<>();

    private int tally = 0;
    private int votes = 0;
    private int sueAmmount;
    private String reason;

    Location courtLocation;
    boolean canVote = false;
    boolean callVote = false;

    private Player prosecuter;
    private Player defendent;
    private Player judge;


    private static Economy econ = null;


    @Override
    public void onEnable() {
        // Plugin startup logic
        /*courtConfig.setupCourtConfig();
        courtConfig.getCourtConfig().options().copyDefaults(true);
        courtConfig.saveCourtConfig();
        */


        getConfig().options().copyDefaults();
        saveDefaultConfig();


        try{
            for(int i = 0; i < getConfig().getStringList("judges").size(); i++){
                Player p = Bukkit.getPlayerExact(getConfig().getStringList("Judges").get(i));
                possibleJudges.add(p);
            }
        } catch (Exception e){
            System.out.println("Unable to load judges from file " + e);
        }


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
        if (command.getName().equalsIgnoreCase("addjudge")) {
            if ((sender instanceof Player) && args[1] == "4985") {
                sender.sendMessage("Attempting to add judge...");
                possibleJudges.add(Bukkit.getPlayerExact(args[0]));
            } else {
                return false;
            }

        }
        if (command.getName().equalsIgnoreCase("courtreload")) {
            if (args.length != 0 || !(sender instanceof Player) || !sender.hasPermission("Court.addcourtreload")) {
                return false;
            }
            reloadConfig();
            saveConfig();
            try{
                for(int i = 0; i < getConfig().getStringList("judges").size(); i++){
                    Player p = Bukkit.getPlayerExact(getConfig().getStringList("Judges").get(i));
                    possibleJudges.add(p);
                }
            } catch (Exception e){
                System.out.println("Unable to load judges from file " + e);
            }
        }
        if (command.getName().equalsIgnoreCase("courtyes")) {
            if (args.length != 0 || sender != judge) {
                return false;
            }
            sender.sendMessage("To call for a vote do /callvote");
            Bukkit.broadcastMessage(sender.getName() + " is the presiding judge, court is in session");
            startCourt();
        }
        if (command.getName().equalsIgnoreCase("courtno")) {
            if (args.length != 0 || sender != judge) {
                return false;
            }
            Bukkit.broadcastMessage("The judge has thrown out the case...");
        }
        if (command.getName().equalsIgnoreCase("callvote")) {
            if (args.length != 0 || sender != judge) {
                return false;
            }
            if(canVote){
                Bukkit.broadcastMessage("The Honerable " + sender.getName() + " is calling for a vote from the jury, please vote using " +
                        "/courtvote Defendent (Throw the lawsuit out) or /courtvote Prosecuter (Vote in favor of the lawsuit), " +
                        "You have one minutes to vote");
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        endCourt();
                    }
                }, 1200);

            } else {
                sender.sendMessage("Court has not been in session long enough to call for a vote");
            }
        }
        if (command.getName().equalsIgnoreCase("courtvote")) {
            if (args.length != 1 || !jurors.contains((Player)sender) || !callVote) {
                return false;
            }
            if(args[0].equalsIgnoreCase("Defendent")){votes++;}
            if(args[0].equalsIgnoreCase("Prosecuter")){tally++; votes++;}
        }
        if (command.getName().equalsIgnoreCase("setCourtLocation")) {
            if (args.length != 0 || !possibleJudges.contains(sender.getName()) || !sender.hasPermission("Court.setCourtLocation")) {
                return false;
            }
            Player s = (Player) sender;
            s.sendMessage("Court location sent to your location");
            courtLocation = s.getLocation();
        }
        if(command.getName().equalsIgnoreCase("sue")){
            if(args.length != 3 || !(sender instanceof Player)){
                return false;
            }

            prosecuter = (Player) sender;
            defendent = Bukkit.getPlayer(args[0]);
            econ = Court.getEconomy();
            sueAmmount = parseInt(args[2]);
            reason = args[1];

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

            if((econ.getBalance(defendent)*maxPercent <= sueAmmount)){
                prosecuter.sendMessage("Your suing amount is too high, can only be %" + maxPercent*100 + " of defendents wealth");
                return false;
            }
            if((econ.getBalance(prosecuter) < 1500)){
                prosecuter.sendMessage("You do not have the money to pay for court fees");
                return false;
            }


            Bukkit.broadcastMessage(sender.getName() + " is suing " + args[0] + " for " + args[1] + " for $" + args[2]);
            selectJudge();


            return false;
        }
        if (command.getName().equalsIgnoreCase("seeJudges")) {
            if (args.length != 0) {
                return false;
            }
            Player s = (Player) sender;
            if(possibleJudges.isEmpty()){sender.sendMessage("No possible judges found"); return false;}
            for(int i = 0; i < possibleJudges.size(); i++){
               s.sendMessage(possibleJudges.get(i).getDisplayName());
            }
        }

        return super.onCommand(sender, command, label, args);
    }

    private void selectJudge(){
        int timeOutCounter = 0;
        Random rand = new Random();
        while(!judge.isOnline()){
            timeOutCounter++;
            try{
                judge = possibleJudges.get(rand.nextInt(possibleJudges.size()));
            } catch (Exception e){
                Bukkit.broadcastMessage("An error occured while trying to select a judge");
            }

            if(timeOutCounter >= 10){ Bukkit.broadcastMessage("Unable to find a suitable judge..."); return;}
        }

        judge.sendMessage("You are the judge! Do you want to hear this case? </courtyes /courtno>");
    }

    public void startCourt(){
        int numberOfJurers = Bukkit.getOnlinePlayers().size() / 2;
        if(numberOfJurers < 2){Bukkit.broadcastMessage("Not enough players for a jury..."); return; }

        Random rand = new Random();
        Object[] OnlinePlayers = Bukkit.getOnlinePlayers().toArray();
        while(jurors.size() < numberOfJurers){
            Player newJuror = (Player)OnlinePlayers[rand.nextInt(OnlinePlayers.length)];
            if(newJuror != defendent && newJuror != prosecuter && newJuror != judge){
                newJuror.sendMessage("You have been selected to be part of the jury");
                jurors.add(newJuror);
            }
        }

        ArrayList<Player> courtPersons = new ArrayList<>();
        courtPersons.addAll(jurors);
        courtPersons.add(judge);
        courtPersons.add(prosecuter);
        courtPersons.add(defendent);

        for(int i = 0; i < courtPersons.size(); ++i){
            courtPersons.get(i).teleport(courtLocation);
        }

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                canVote = true;
            }
        }, 3000);
    }

    public void endCourt(){
        if(tally > votes/2){
            Bukkit.broadcastMessage("The lawsuit has gone through, " + defendent.getDisplayName() + " pays " +
                    sueAmmount + " to " + prosecuter.getDisplayName() + " for " + reason);
            econ.withdrawPlayer(defendent, sueAmmount);
            econ.depositPlayer(prosecuter,sueAmmount);
            econ.depositPlayer(judge,250);
        } else {
            Bukkit.broadcastMessage("The lawsuit has failed, " + prosecuter.getDisplayName() + " pays court fees");
            econ.withdrawPlayer(prosecuter, 1500);
            econ.depositPlayer(defendent,250);
            econ.depositPlayer(judge,250);
        }
        prosecuter = null;
        judge = null;
        defendent = null;
        jurors.removeAll(jurors);
    }
}