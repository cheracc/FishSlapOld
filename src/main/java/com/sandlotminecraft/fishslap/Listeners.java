package com.sandlotminecraft.fishslap;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

public class Listeners implements org.bukkit.event.Listener {

    // cancels eathing fish
    @EventHandler
    public void onEatFish(PlayerItemConsumeEvent event) {
        if (Fish.isFish(event.getItem()))
            event.setCancelled(true);
    }

    // grants the on-use effects when a player right clicks with a fish in their offhand (while in the game)
    @SuppressWarnings("DefaultAnnotationParam")
    @EventHandler(ignoreCancelled = false)
    public void onRightClickFish(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) || !event.getHand().equals(EquipmentSlot.OFF_HAND))
            return;

        Player p = event.getPlayer();

        if (Fish.isFish(p.getInventory().getItemInOffHand())) {

            // checks if the player is on cooldown and plays an error sound if they are
            if (GameTracker.isOnCooldown(p)) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5F, 0F);
                return;
            }
            // checks if they are in the game and sends a message if they are not
            if (!GameTracker.isPlaying(p)) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You must join the game before using that"));
                return;
            }

            int level = Fish.getLevel(p.getInventory().getItemInOffHand());
            int type = p.getInventory().getItemInOffHand().getDurability();

            // see if the fish has an on-use effect
            if (FishStats.onUseEffect[type][level] != null) {
                // handles the 'instant heal' effect of the grouper
                if (FishStats.onUseEffect[type][level] == PotionEffectType.HEAL) {
                    if (p.getHealth() + FishStats.useEffectLevel[type][level] > 20) {
                        p.setHealth(20);
                    } else {
                        p.setHealth(p.getHealth() + FishStats.useEffectLevel[type][level]);
                    }
                }
                // add the effect, play a sound, and start the player's cooldown
                PotionEffect potion = new PotionEffect(FishStats.onUseEffect[type][level], 20 * FishStats.useEffectDuration[type][level], FishStats.useEffectLevel[type][level]);
                p.addPotionEffect(potion);
                p.playSound(p.getLocation(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 2F, 1F);

                Random rand = new Random();

                // play a particle effect
                for (int i = 0; i < 8; i++) {
                    double randx = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    double randy = (0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1));
                    double randz = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    p.getWorld().spawnParticle(Particle.SPELL_INSTANT, p.getEyeLocation().add(-1 * randx, -1 * randy, -1 * randz), 3);
                    p.getWorld().spawnParticle(Particle.SPELL_WITCH, p.getEyeLocation().add(randx, randy, randz), 3);
                }
                GameTracker.startCooldown(p, FishStats.cooldown[type][level]);

            }
            // handle the 'remove poison' ability
            if (type == 3) {
                p.removePotionEffect(PotionEffectType.POISON);
                p.playSound(p.getLocation(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 2F, 1F);
                Random rand = new Random();
                for (int i = 0; i < 8; i++) {
                    double randx = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    double randy = (0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1)) ;
                    double randz = 0.5 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1);
                    p.getWorld().spawnParticle(Particle.SPELL_INSTANT, p.getEyeLocation().add(-1 * randx, -1 * randy, -1 * randz), 3);
                    p.getWorld().spawnParticle(Particle.SPELL_WITCH, p.getEyeLocation().add(randx, randy, randz), 3);
                }
                GameTracker.startCooldown(p, FishStats.cooldown[type][level]);
            }
        }
    }

    // prevents players from dropping their fish
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (Fish.isFish(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    // handles the rare incident of someone 'dragging' their fish in the offhand to equip it
    @EventHandler
    public void onEquipFishByDrag(InventoryDragEvent event) {
        if (!event.getInventorySlots().contains(40))
            return;
        if (Fish.isFish(event.getOldCursor())) {
            GameTracker.addToGame((Player) event.getWhoClicked());
            Fish.addEquipEffect((Player) event.getWhoClicked(), event.getOldCursor());
        }
    }

    @EventHandler
    public void onClickOffhand(InventoryClickEvent event) {
        // Check if the slot clicked was the offhand, don't care otherwise
        if (event.getSlot() != 40)
            return;
        if (!event.getWhoClicked().getType().equals(EntityType.PLAYER))
            return;

        Player p = (Player) event.getWhoClicked();

        if (Fish.isFish(event.getCurrentItem()) && event.getCurrentItem().getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Your fish is bound. You must wait before removing it."));
            event.setCancelled(true);
            return;
        }
        if (Fish.isFish(event.getCurrentItem()) && GameTracker.isOnCooldown(p)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You cannot remove that while it is on cooldown"));
            event.setCancelled(true);
            return;
        }

        // Look for swapping a fish into or out of offhand with another item
        if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            if (Fish.isFish(event.getCurrentItem()) && Fish.isFish(event.getCursor())) {// fish being replaced by another fish
                Fish.removeEquipEffect(p, event.getCurrentItem());
                Fish.addEquipEffect(p, event.getCursor());
                final Player pdelay = p;
                new BukkitRunnable() {
                    public void run() {
                        pdelay.updateInventory();
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 1);

                return;
            }
            if (Fish.isFish(event.getCurrentItem())) {// fish being removed from offhand slot
                GameTracker.removeFromGame(p);
                Fish.removeEquipEffect(p, event.getCurrentItem());
            }

            if (Fish.isFish(event.getCursor())) {// fish being placed into offhand slot
                GameTracker.addToGame(p);
                Fish.addEquipEffect(p, event.getCursor());
                final Player pdelay = p;
                new BukkitRunnable() {
                    public void run() {
                        pdelay.updateInventory();
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 1);

            }

        }

        // Look for dropping a fish into empty offhand (to join the game)
        if (event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.PLACE_ALL)
            if (Fish.isFish(event.getCursor())) {
                GameTracker.addToGame(p);
                Fish.addEquipEffect(p, event.getCursor());
                final Player pdelay = p;
                new BukkitRunnable() {
                    public void run() {
                        pdelay.updateInventory();
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 1);
            }

        // Look for picking up a fish from offhand (to leave the game)
        if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            if (Fish.isFish(event.getCurrentItem())) {
                GameTracker.removeFromGame(p);
                Fish.removeEquipEffect(p, event.getCurrentItem());
            }
    }

    @EventHandler
    public void onSwitchMainHand(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        ItemStack fish = p.getInventory().getItem(event.getNewSlot());

        if (!GameTracker.isOnCooldown(p) && Fish.isFish(fish)) {
            p.setLevel(Fish.getLevel(fish));
            p.setExp(Fish.getXP(fish));
        } else if (!GameTracker.isOnCooldown(p)) {
            p.setLevel(0);
            p.setExp(0F);
        }

    }

    @EventHandler
    public void onChangedOffhand(PlayerSwapHandItemsEvent event) {
        if (!Fish.isFish(event.getOffHandItem()) && !Fish.isFish(event.getMainHandItem())) // Neither item is a fish
            return;

        Player p = event.getPlayer();
        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();

        if (Fish.isFish(main) && main.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Your fish is bound. You must wait before removing it."));
            event.setCancelled(true);
            return;
        }

        if (Fish.isFish(main) && GameTracker.isOnCooldown(p)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7You cannot remove that while it is on cooldown"));
            event.setCancelled(true);
            return;
        }


        if (Fish.isFish(off) && Fish.isFish(main)) {// Check for a fish swapped with another fish
            Fish.removeEquipEffect(p, main);
            Fish.addEquipEffect(p, off);
            if (!GameTracker.isOnCooldown(p)) {
                p.setExp(Fish.getXP(main));
                p.setLevel(Fish.getLevel(main));
            }
            return;
        }

        if (Fish.isFish(off)) {// player swapped a fish into offhand to join game
            GameTracker.addToGame(event.getPlayer());
            Fish.addEquipEffect(p, off);
            if (!GameTracker.isOnCooldown(p)) {
                p.setExp(0);
                p.setLevel(0);
            }
        }

        if (Fish.isFish(main)) {// player swapped fish out of offhand to leave game
            GameTracker.removeFromGame(event.getPlayer());
            Fish.removeEquipEffect(p, main);
            if (!GameTracker.isOnCooldown(p)) {
                p.setExp(Fish.getXP(main));
                p.setLevel(Fish.getLevel(main));
            }
        }
    }

    @EventHandler // prevent lingering potions affecting non-players
    public void onAoeCloud(AreaEffectCloudApplyEvent event) {
        ProjectileSource source = event.getEntity().getSource();

        if (!(source instanceof Player))
            return;

        Player p = (Player) source;

        if (!GameTracker.isPlaying(p)) {
            List<LivingEntity> toRemove = newArrayList();
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (entity instanceof Player)
                    toRemove.add(entity);
            }
            event.getAffectedEntities().removeAll(toRemove);
        } else {
            List<LivingEntity> toRemove = newArrayList();
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (entity instanceof Player && !GameTracker.isPlaying((Player) entity)) {
                    toRemove.add(entity);
                }
            }
            event.getAffectedEntities().removeAll(toRemove);
        }
        for (LivingEntity entity : event.getAffectedEntities()) {
            GameTracker.setDamager((Player) entity, p, EntityDamageEvent.DamageCause.CUSTOM);
        }
    }

    @EventHandler //try to prevent splash potions affecting non-players
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ProjectileSource source = potion.getShooter();

        if (!(source instanceof Player)) {
            return;
        }
        Player p = (Player) source;

        if (!GameTracker.isPlaying(p)) {
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (entity instanceof Player) {
                    event.setIntensity(entity, 0);
                    event.getAffectedEntities().remove(entity);
                }
            }
        } else {
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (entity instanceof Player && !GameTracker.isPlaying((Player) entity)) {
                    event.setIntensity(entity, 0);
                    event.getAffectedEntities().remove(entity);
                }
            }
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        ItemStack offhand = p.getInventory().getItemInOffHand();

        // remove fish if there is one in offhand
        if (Fish.isFish(offhand)) {
            Fish.unbindFish(p);
            p.getInventory().setItem(40, new ItemStack(Material.AIR));
            p.getInventory().addItem(offhand);
        }
        p.getActivePotionEffects().clear();
        GameTracker.removeFromGame(p);
        GameTracker.removeScore(p.getDisplayName());

        // cancel any runnables
        UUID id = p.getUniqueId();
        if (Fish.bindTracker.containsKey(id)){
            BukkitTask task = Fish.bindTracker.get(id);
            if (task != null)
                task.cancel();
            Fish.bindTracker.remove(id);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        // remove fish if there is one in offhand
        if (Fish.isFish(event.getPlayer().getInventory().getItemInOffHand())) {
            ItemStack offhand = p.getInventory().getItemInOffHand();
            p.getInventory().setItem(40, new ItemStack(Material.AIR));
            p.getInventory().addItem(offhand);
        }
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());

        GameTracker.removeFromGame(p);
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        if (event.getEntity().getType().equals(EntityType.PLAYER)) {
            Player p = (Player) event.getEntity();
            p.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER))
            if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.REGEN) || event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED))
                event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.setDeathMessage(null);
        event.setDroppedExp(0);

        Player p = event.getEntity();
        Player k = p.getKiller();
        String killer;
        String message = p.getDisplayName();
        GameTracker.addScore(p, null, -50);

        if (k != null) {
            killer = k.getDisplayName();
            GameTracker.addScore(k, p.getDisplayName(), 20);
            if (Fish.isFish(k.getInventory().getItemInMainHand())) {
                Random rand = new Random();
                String fish = ChatColor.stripColor(k.getInventory().getItemInMainHand().getItemMeta().getDisplayName()).substring(2);
                switch (rand.nextInt(5)) {
                    case 0:
                        message = killer + "'s " + fish + " now sits where " + p.getDisplayName() + "'s head should be";
                        break;
                    case 1:
                        message = message + " got slapped around a bit too much by " + killer;
                        break;
                    case 2:
                        message = message + " now knows what " + killer + "'s " + fish + " tastes like";
                        break;
                    case 3:
                        message = message + "'s face stings a bit from the impact of " + killer + "'s " + fish;
                        break;
                    case 4:
                        message = message + "'s head is now a " + fish + " shaped hole";
                        break;
                    default:
                        message = message + " was killed by " + killer + " with a " + fish;
                }
            }
            else if (p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.MAGIC)
                message = message + " got splattered by " + killer + "'s magic potion";
            else if (Objects.requireNonNull(GameTracker.getLastDamager(p)).getCause() == EntityDamageEvent.DamageCause.CUSTOM)
                message = message + " wandered into " + killer + "'s stinky lingering cloud";
            else
                message = message + " was slain by " + killer;
        } else {
            if (p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                killer = p.getLastDamageCause().getEntity().getName();
                message = message + " got splattered by " + killer + "'s magic potion";
            }
            else if (GameTracker.getLastDamager(p) != null) {
                killer = Objects.requireNonNull(GameTracker.getLastDamager(p)).getName();

                switch (Objects.requireNonNull(GameTracker.getLastDamager(p)).getCause()) {
                    case THORNS:
                        message = message + " was pricked to death by " + killer + "'s thorns";
                        break;
                    case ENTITY_SWEEP_ATTACK:
                        message = message + " got caught in " + killer + "'s sweep attack";
                        break;
                    default:
                        message = message + " was killed by " + killer;
                }
            }
            else
                message = message + " died";
        }

            for (UUID id : GameTracker.fsPlayers) {
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline())
                    player.sendMessage(message);
            }
    }

    @EventHandler
    public void cancelFallDamage(EntityDamageEvent event) {
        if (event.getEntity().getType().equals(EntityType.PLAYER) && event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
            event.setCancelled(true);
    }

    @EventHandler
    public void updateEffectOnRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();

        if (Fish.isFish(p.getInventory().getItemInOffHand())) {
            final Player pdelay = p;
            new BukkitRunnable() {
                public void run() {
                    Fish.addEquipEffect(pdelay, pdelay.getInventory().getItemInOffHand());
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 3);


            new BukkitRunnable() {
                public void run() {
                    pdelay.updateInventory();
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 5);

        }

    }

    @EventHandler
    public void stopFlying(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (p.isFlying() && GameTracker.isPlaying(p) && p.getGameMode() != GameMode.CREATIVE) {
            p.setFlying(false);
            p.setAllowFlight(false);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3<&fFishSlap&3> &7Flying is disabled while playing FishSlap"));

        }
    }

}