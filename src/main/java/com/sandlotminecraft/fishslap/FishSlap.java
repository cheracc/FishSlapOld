package com.sandlotminecraft.fishslap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

public class FishSlap extends JavaPlugin {

    @Override
    public void onEnable() {
        //register listeners
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new Fishing(), this);
        getServer().getPluginManager().registerEvents(new FishCombat(), this);

        //set up scoreboard
        GameTracker.topScores.setDisplaySlot(DisplaySlot.SIDEBAR);
        GameTracker.topScores.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&e&lTop Scores&r"));
        GameTracker.health.setDisplayName(ChatColor.RED + "\u2665");
        GameTracker.health.setDisplaySlot(DisplaySlot.BELOW_NAME);
//        addDummyScores();
    }

    // for debug/testing - adds dummy players to scoreboard
    private void addDummyScores() {
        Score score;
        String dummy = "Dummy";
        for (int i=1;i<15;i++) {
            score = GameTracker.topScores.getScore(dummy + i);
            score.setScore(1000 - ( 60 * i));
        }
    }

    @Override
    public void onDisable() {
    }

    // handles the /givefish command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("givefish")) {

            Player p = null;

            if (sender instanceof Player)
                p = (Player) sender;

            if (args.length > 3 || args.length < 2)
                return false;

            int type;

            // getting the type from arguments
            switch (args[0].toLowerCase()) {
                case "cod":
                case "0":
                    type = 0;
                    break;
                case "salmon":
                case "1":
                    type = 1;
                    break;
                case "grouper":
                case "clownfish":
                case "tropical":
                case "2":
                    type = 2;
                    break;
                case "pufferfish":
                case "puffer":
                case "3":
                    type = 3;
                    break;
                default:
                    return false;
            }

            // getting the level from arguments
            int level;

            if (args[1] == null)
                return false;

            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                return false;
            }

            if (level < 1 || level > 10)
                return false;

            // handle the optional player argument (required if sent from console)
            Player t;
            if (sender instanceof ConsoleCommandSender && args.length != 3)
                return false;

            if (sender instanceof Player && args.length == 2) {
                t = p;
            }else if (Bukkit.getPlayerExact(args[2]) == null)
                return false;
            else
                t = Bukkit.getPlayerExact(args[2]);

            if (t == null)
                return false;

            // check for full inventory and give the fish
            if (t.getInventory().firstEmpty() == -1) {
                sender.sendMessage(t.getName() + "'s inventory is full.");
                return false;
            } else {
                t.getInventory().addItem(Fish.giveFish((short) type, level));
                return true;
            }
        }
    return false;
    }

}
