package com.sandlotminecraft.fishslap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

// this tracks and handles most things to do with scoring and tracking who is in the game currently

public class GameTracker {
    private static Map<UUID, BukkitTask> tasks = new HashMap<>(); // tracks players that are in the process of joining the game
    public static List<UUID> fsPlayers = new ArrayList<>(); // list of all of the players currently playing
    private static Map<UUID, Long> cooldowns = new HashMap<>(); // tracks players that are currently on an ability cooldown
    private static List<UUID> healCooldown = new ArrayList<>(); // tracks healing to prevent spam healing
    private static Map<UUID, LastDamager<String, EntityDamageEvent.DamageCause>> damageTracker = new HashMap<>(); // tracks a players last damage source (mostly from thorns or sweep attack)
    // set up the scoreboard
    private static ScoreboardManager manager = Bukkit.getScoreboardManager();
    private static Scoreboard sideboard = manager.getNewScoreboard();
    public static Objective topScores = sideboard.registerNewObjective("scores", "dummy");
    public static Objective health = sideboard.registerNewObjective("Health", "health");
    private static Map<UUID, LinkedList<String>> targetTracker = new HashMap<>(); // this holds each player's last 5 targets for purpose of calculating scores

    // starts a player's warmup to join the game when they equip a fish
    public static boolean addToGame(final Player p) {
        if (fsPlayers.contains(p.getUniqueId())) // player is already in game, not added
            return false;
        if (tasks.containsKey(p.getUniqueId())) // player already in process of joining. not added
            return false;

        // Player not in game or in process of joining, start warmup timer
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Fish equipped on off-hand. Joining game in 30 seconds."));
        tasks.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You have entered the game!"));
                if (p.isFlying() || p.getAllowFlight()) {
                    p.setFlying(false);
                    p.setAllowFlight(false);
                }
                fsPlayers.add(p.getUniqueId()); // add to active players
                tasks.remove(p.getUniqueId()); // remove warmup timer
                for (Player player : Bukkit.getOnlinePlayers()) { // notify players of new entry
                    if (fsPlayers.contains(player.getUniqueId()) && player != p)
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7" + p.getName() + ChatColor.GRAY + " has entered the game."));
                }
                // put them on the scoreboard
                addScore(p, null, 0);
                p.setScoreboard(sideboard);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 300L));
        return true;
    }


    // removes a player from the game or cancels their warmup if they haven't joined yet
    // used when a player removes a fish from the offhand (that isn't just swapped for another fish)
    public static boolean removeFromGame(Player p) {
        // see if the player is currently waiting to join, remove if so
        if (tasks.containsKey(p.getUniqueId()))
        {
            BukkitTask task = tasks.get(p.getUniqueId());
            if (task != null)
            {
                task.cancel();
                tasks.remove(p.getUniqueId());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Fish unequipped. Join cancelled."));
            }
            return true;
        }
        // see if player is active player, remove if so
        if (fsPlayers.contains(p.getUniqueId()))
        {
            fsPlayers.remove(p.getUniqueId());
            System.out.println(p.getName() + " has left the game of FishSlap.");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (fsPlayers.contains(player.getUniqueId()) && player != p)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7" + p.getName() + ChatColor.GRAY + " has left the game of FishSlap."));
            }
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Fish unequipped. You have left the game."));
            // removes the player's scoreboard
            Scoreboard blank = manager.getNewScoreboard();
            p.setScoreboard(blank);
            return true;
        }
        return false;
    }

    // checks if a player is currently in the game
    public static boolean isPlaying(Player p) {
        return fsPlayers.contains(p.getUniqueId());
    }

    // checks if a player is in the process of joining the game
    public static boolean isJoining(Player p) {
        return tasks.containsKey(p.getUniqueId());
    }

    // starts a cooldown when a fish ability is used
    // also handles the player xp bar showing the time remaining
    public static void startCooldown(final Player p, final int time) {
        final UUID id = p.getUniqueId();

        cooldowns.put(id, System.currentTimeMillis());
        p.setExp(1F);

        // this fills the player's xp bar and slowly drains it as the cooldown expires
        // also shows seconds remaining as their level above the bar
        BukkitTask doCooldown = new BukkitRunnable() {
            public void run() {
                int timeElapsed = (int) (System.currentTimeMillis() - cooldowns.get(id));

                if ((timeElapsed /1000) >= time || !p.isOnline()) {
                    this.cancel();
                    cooldowns.remove(id);
                    if (Fish.isFish(p.getInventory().getItemInMainHand())) {
                        ItemStack fish = p.getInventory().getItemInMainHand();
                        p.setLevel(Fish.getLevel(fish));
                        p.setExp(Fish.getXP(fish));
                    } else
                    p.setExp(0F);
                }
                else {
                    float percentLeft = 1F - ( (float) timeElapsed / (float) (time * 1000));
                    p.setExp(percentLeft);
                    p.setLevel(time - (timeElapsed / 1000));
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("FishSlap"), 0, 1);
    }

    // returns whether a player is on a healing cooldown
    public static boolean hasHealCooldown(Player p) {
        return healCooldown.contains(p.getUniqueId());
    }
    // starts the healing cooldown (10 ticks)
    public static void doHealCooldown(final Player p) {
        final UUID id = p.getUniqueId();

        if (!healCooldown.contains(id)) {
            healCooldown.add(id);
            new BukkitRunnable() {
                @Override
                public void run() {
                    healCooldown.remove(id);
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 10);
        }
    }

    // checks if a player is on an active ability cooldown
    public static boolean isOnCooldown(Player p) {
        return cooldowns.containsKey(p.getUniqueId());
    }

    // sets a players last damage source (both player and type of damage) - used for death messages
    public static void setDamager(Player p, Player d, EntityDamageEvent.DamageCause cause) {
        damageTracker.put(p.getUniqueId(), new LastDamager<>(d.getDisplayName(), cause));
    }

    // gets a players last damage source (player and damage type)
    public static LastDamager<String, EntityDamageEvent.DamageCause> getLastDamager(Player p) {
        return damageTracker.getOrDefault(p.getUniqueId(), null);
    }

    // class for holding a player name and damage source
    public static class LastDamager<PlayerName, DamageCause> {
        String name;
        EntityDamageEvent.DamageCause cause;

        LastDamager(String name, EntityDamageEvent.DamageCause cause) {
            this.name = name;
            this.cause = cause;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public EntityDamageEvent.DamageCause getCause() {
            return cause;
        }

        public void setCause(EntityDamageEvent.DamageCause cause) {
            this.cause = cause;
        }
    }

    // adds an amount to a player's score
    // calculates a modifier for the amount given
    // points are increased if:
    //    - a player kills a player with more points than them
    //    - a player kills a player and dealt most of the previous damage to them
    // points are decreased if: (these combined can reduce the gain to zero)
    //    - a player repeatedly hits the same target
    //    - kills a player with fewer points than them
    // also modifies xp given to the main-hand fish in the same way
    public static void addScore(Player p, String tName, int amount) {
        String pName = p.getDisplayName();
        UUID id = p.getUniqueId();
        Score score = topScores.getScore(pName);
        float multiplier;
        int totalPts;

        // accepts null target name and doesn't bother calculating a modifier if it is or if the amount is already zero
        if (tName == null || amount <= 0)
            multiplier = 1F;
        else {
            multiplier = 1F;
            LinkedList<String> targets = new LinkedList<>();

            // reduces multiplier for repeatedly hitting the same player
            // increases multiplier if the points are for a death and the killer has been attacking the deceased
            if (targetTracker.containsKey(id)) {
                targets = targetTracker.get(id);

                for (String name : targets) {
                    if (name.equals(tName))
                        if (Bukkit.getPlayer(tName).isDead())
                            multiplier += 0.1F;
                        else
                            multiplier -= 0.1F;
                }

                // adds the player's target to their 'recently hit' list and caps it at the last 5
                targets.add(tName);
                while (targets.size() >= 5)
                    targets.removeFirst();
                targetTracker.replace(id, targets);

                // adjusts the modifier based on the player's score and their target's score
                if (topScores.getScore(pName).getScore() != 0) {
                    float tScore = topScores.getScore(tName).getScore();
                    float pScore = topScores.getScore(pName).getScore();
                    float topScore = 0;

                    for (Player pTemp : Bukkit.getOnlinePlayers()) {
                        topScore = Math.max(topScores.getScore(pTemp.getDisplayName()).getScore(), topScore);
                    }
//                    topScore = 940; //Debug
                    float scoreMultiplier = (tScore - pScore) / topScore;

                    multiplier += scoreMultiplier;
                    multiplier = Math.max(multiplier, 0);
                }

            } else { // this only occurs the first time a player hits another player
                targets.add(tName);
                targetTracker.put(id, targets);
            }
        }
        Random rand = new Random();
        if (amount == 1)
            if (rand.nextInt(5) >= 3)
                multiplier = 1F;

        // updates the scoreboard and the fish's xp
        totalPts = Math.round(amount * multiplier);
        score.setScore(Math.max(0, score.getScore() + totalPts));
        if (Fish.isFish(p.getInventory().getItemInMainHand()) && amount > 0) {
            Fish.giveXP(p, p.getInventory().getItemInMainHand(), Math.min(totalPts, 10));
        }
    }

    // removes a player from the scoreboard (if they leave the server)
    public static void removeScore(String name) {
        sideboard.resetScores(name);
    }



}
