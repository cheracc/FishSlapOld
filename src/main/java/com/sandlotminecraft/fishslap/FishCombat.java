package com.sandlotminecraft.fishslap;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;

// this handles players hitting other players with fish
public class FishCombat implements Listener {
    @EventHandler
    public void onFishSlap(EntityDamageByEntityEvent event) {
        // Deal with arrows just in case
        if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getDamager();
            ProjectileSource shooter = arrow.getShooter();

            if (shooter instanceof Player) {
                Player p = (Player) shooter;

                if (!GameTracker.isPlaying(p)) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getEntity() instanceof Player) {
                    Player t = (Player) event.getEntity();

                    if (!GameTracker.isPlaying(t)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (event.getDamager().getType() == EntityType.SPECTRAL_ARROW) {
            SpectralArrow arrow = (SpectralArrow) event.getDamager();
            ProjectileSource shooter = arrow.getShooter();

            if (shooter instanceof Player) {
                Player p = (Player) shooter;

                if (!GameTracker.isPlaying(p)) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getEntity() instanceof Player) {
                    Player t = (Player) event.getEntity();

                    if (!GameTracker.isPlaying(t)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (event.getDamager().getType() == EntityType.TIPPED_ARROW) {
            TippedArrow arrow = (TippedArrow) event.getDamager();
            ProjectileSource shooter = arrow.getShooter();

            if (shooter instanceof Player) {
                Player p = (Player) shooter;

                if (!GameTracker.isPlaying(p)) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getEntity() instanceof Player) {
                    Player t = (Player) event.getEntity();

                    if (!GameTracker.isPlaying(t)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Make sure this is a pvp action
        if (!event.getDamager().getType().equals(EntityType.PLAYER) || !event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }

        Player p = (Player) event.getDamager();
        Player t = (Player) event.getEntity();

        // See if attacker is in the game
        if (!GameTracker.isPlaying(p)) {
            event.setCancelled(true);
            if (GameTracker.isJoining(p))
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You haven't joined the game yet."));
            else
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You must have a fish in your offhand to play FishSlap."));
            return;
        }

        // This event handler is calculating thorns damage on its own so cancel the actual effect
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) {
            event.setCancelled(true);
            return;
        }

        // See if target is in the game
        if (!GameTracker.isPlaying(t)) {
            event.setCancelled(true);
            if (GameTracker.isJoining(t))
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7" + t.getName() + " is currently waiting to join."));
            else
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7" + t.getName() + " is not playing."));
            return;
        }

        // Make sure attack is happening with a fish
        if (!Fish.isFish(p.getInventory().getItemInMainHand())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You can only play FishSlap with a fish!"));
            event.setCancelled(true);
            return;
        }

        // Both players are playing and a hit is occurring. Bind the fish of both players and set the damage to zero to change it later
        Fish.bindFish(p);
        Fish.bindFish(t);
        event.setDamage(0);

        if (Fish.isFish(p.getInventory().getItemInMainHand())) {
            ItemStack fish = p.getInventory().getItemInMainHand();
            int damage = FishStats.damage[fish.getDurability()][Fish.getLevel(fish)];

            //set the raw damage for anything other than the grouper (this can be reduced by resistance/armor)
            if (fish.getDurability() != 2) {
                event.setDamage(damage);
                GameTracker.addScore(p, t.getDisplayName(), damage);
                p.getWorld().playSound(t.getEyeLocation(), Sound.ENTITY_SLIME_SQUISH, 1, 1);
                Random rand = new Random();
                for (int i = 0; i < 3; i++) {
                    double randx = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    double randy = (0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1)) ;
                    double randz = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    p.getWorld().spawnParticle(Particle.SLIME, t.getEyeLocation().add(-1 * randx, -1 * randy, -1 * randz), 3);
                    p.getWorld().spawnParticle(Particle.SLIME, t.getEyeLocation().add(randx, randy, randz), 3);
                }
            }
            else  //heal the target
            {
                Random rand = new Random();
                // don't award xp if the target is already at full health
                if (t.getHealth() >= t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    event.setCancelled(true);
                    for (int i = 0; i < 2; i++) {
                        double randx = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        double randy = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        double randz = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        p.getWorld().spawnParticle(Particle.FALLING_DUST, t.getEyeLocation().add(-1 * randx, -1 * randy, -1 * randz), 3);
                        p.getWorld().spawnParticle(Particle.FALLING_DUST, t.getEyeLocation().add(randx, randy, randz), 3);
                    }
                }
                else if (!(event.getCause() == EntityDamageEvent.DamageCause.THORNS)) {
                    // cancel the heal if they are on heal cooldown
                    if (GameTracker.hasHealCooldown(p)) {
                        event.setCancelled(true);
                        return;
                    }
                    // set their health
                    t.setHealth(Math.min(20, t.getHealth() + damage));
                    // award xp
                    GameTracker.addScore(p, t.getDisplayName(), damage);
                    GameTracker.doHealCooldown(p);
                    event.setCancelled(true);
                    for (int i = 0; i < 6; i++) {
                        double randx = 0.8 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        double randy = 0.8 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        double randz = 0.8 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                        p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, t.getEyeLocation().add(-1 * randx, -1 * randy, -1 * randz), 3);
                        p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, t.getEyeLocation().add(randx, randy, randz), 3);
                        p.getWorld().playSound(t.getEyeLocation(), Sound.ENTITY_SLIME_ATTACK, 1F, 1.5F);
                    }
                }
                return;
            }

            //deal with thorns
            if (t.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.THORNS)) {
                ItemStack tFish = t.getInventory().getItemInOffHand();
                int thornsDamage = FishStats.enchant1level[tFish.getDurability()][Fish.getLevel(tFish)] + 1;
                Random rand = new Random();

                if (rand.nextInt(7) < thornsDamage) {
                    GameTracker.setDamager(p, t, EntityDamageEvent.DamageCause.THORNS);
                    p.damage(thornsDamage);
                    p.getWorld().playSound(t.getEyeLocation(), Sound.ENCHANT_THORNS_HIT, 1F, 1F);
                }
                return;
            }

            //add any effects (poison) to the target
            if (fish.getDurability() == 3) {
                Random rand = new Random();

                if (rand.nextInt(100) <= FishStats.poisonChance[Fish.getLevel(fish)]) {
                    PotionEffect pe = new PotionEffect(PotionEffectType.POISON, 20 * FishStats.poisonEffectDuration[Fish.getLevel(fish)], FishStats.poisonEffectLevel[Fish.getLevel(fish)]);
                    t.addPotionEffect(pe);
                    GameTracker.addScore(p, t.getDisplayName(), (FishStats.poisonEffectLevel[Fish.getLevel(fish)]+2) * 2);
                }
            }

            // deal aoe damage
            if (fish.getItemMeta().hasEnchant(Enchantment.SWEEPING_EDGE)) {
                int level = FishStats.aoeDistance[Fish.getLevel(fish)];
                Random rand = new Random();

                // see if there is a successful sweep attack
                if (rand.nextInt(7) > level) {
                    List<Player> nearbyPlayers = newArrayList();
                    double distance = 0.5 + ((double) level / 2);

                    // get all nearby players that are in the game
                    for (Entity entity : t.getNearbyEntities(distance, distance, distance)) {
                        if (entity instanceof Player) {
                            Player sweeptarget = (Player) entity;

                            if (sweeptarget.isOnline() && GameTracker.isPlaying(sweeptarget) && sweeptarget != p && sweeptarget != t)
                                nearbyPlayers.add((Player) entity);
                        }
                    }

                    // damage the nearby players and send them flying based on knockback level
                    if (!nearbyPlayers.isEmpty()) {
                        for (Player aoeTarget : nearbyPlayers) {
                            GameTracker.setDamager(aoeTarget, p, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);
                            aoeTarget.damage((float) (damage / 2));
                            Vector vector = new Vector(aoeTarget.getLocation().getX() - p.getLocation().getX(), 1, aoeTarget.getLocation().getZ() - p.getLocation().getZ());
                            aoeTarget.setVelocity(vector.normalize().multiply(0.3 * FishStats.enchant1level[fish.getDurability()][Fish.getLevel(fish)]));
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1F, 0.8F);
                            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, t.getEyeLocation(), 1);
                            GameTracker.addScore(p, t.getDisplayName(),1);
                        }
                    }
                }
            }
        }
    }
}
