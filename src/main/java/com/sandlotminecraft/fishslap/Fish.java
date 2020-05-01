package com.sandlotminecraft.fishslap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

// this handles most things that have to do with the fish themselves
// xp, levels, creating, adding/removing effects

public class Fish {
    public static Map<UUID, BukkitTask> bindTracker = new HashMap<>(); // this stores players with a bound off-hand fish

    // creates a fish of the specified type and level
    public static ItemStack giveFish(short type, int level) {
       ItemStack fish = new ItemStack(Material.RAW_FISH, 1);
       ItemMeta im = fish.getItemMeta();
       fish.setDurability(type);
       im.setDisplayName(ChatColor.translateAlternateColorCodes('&', FishStats.displayName[type]));
       List<String> lore = new ArrayList<>();
       UUID uuid = UUID.randomUUID();

       // put the enchant on
       if (FishStats.enchant1[type][level] != null && type != 3)
           im.addEnchant(FishStats.enchant1[type][level], FishStats.enchant1level[type][level], true);
       if (type == 0 && FishStats.aoeDistance[level] > 0)
           im.addEnchant(Enchantment.SWEEPING_EDGE, FishStats.aoeDistance[level], true);

       // set the lore based on the level and type of fish
       lore.add(HiddenStringUtils.encodeString(uuid.toString()));
       if (type == 2)
           lore.add(ChatColor.DARK_GREEN + "Heals for: " + FishStats.damage[type][level]);
       else
           lore.add(ChatColor.DARK_RED + "Damage: " + FishStats.damage[type][level]);
       lore.add(type == 3 ? ChatColor.DARK_GREEN + "  + Poison " + toRomanNum(FishStats.poisonEffectLevel[level]) + " (" + FishStats.poisonEffectDuration[level] + " sec/"+ FishStats.poisonChance[level] + "% chance)" : "");
       lore.add(ChatColor.DARK_AQUA + "Level: " + level);
       lore.add(ChatColor.DARK_AQUA + "0/" + FishStats.xpNeeded[level] + " xp");

       if (type == 3) {
           lore.add(ChatColor.GRAY + "When held in off-hand:");
           lore.add(ChatColor.BLUE + "  Adds Thorns " + toRomanNum(FishStats.enchant1level[type][level]-1));
           if (FishStats.armorValue[type][level] != 0)
               lore.add(ChatColor.BLUE + "  +" + FishStats.armorValue[type][level] + " Armor");

           lore.add(ChatColor.GRAY + "When used (in off-hand)");
           lore.add(ChatColor.BLUE + "  Removes Poison");
           lore.add(ChatColor.BLUE + "  (" + FishStats.cooldown[type][level] + " sec cooldown)");
       } else {
           if (FishStats.onEquipEffect[type][level] != null || FishStats.armorValue[type][level] != 0) {
               lore.add(ChatColor.GRAY + "When held in off-hand:");
               if (FishStats.onEquipEffect[type][level] != null)
                   lore.add(ChatColor.BLUE + "  Adds " + getEffectName(FishStats.onEquipEffect[type][level]) + " " + toRomanNum(FishStats.equipEffectLevel[type][level]));
               if (FishStats.armorValue[type][level] != 0)
                   lore.add(ChatColor.BLUE + "  +" + FishStats.armorValue[type][level] + " Armor");
           }
           if (FishStats.onUseEffect[type][level] != null) {
               lore.add(ChatColor.GRAY + "When used (in off-hand):");
               if (FishStats.onUseEffect[type][level] == PotionEffectType.HEAL)
                   lore.add(ChatColor.BLUE + "  " + getEffectName(FishStats.onUseEffect[type][level]) + " " + toRomanNum(FishStats.useEffectLevel[type][level]));
               else
                   lore.add(ChatColor.BLUE + "  " + getEffectName(FishStats.onUseEffect[type][level]) + " " + toRomanNum(FishStats.useEffectLevel[type][level]) + "  for " + FishStats.useEffectDuration[type][level] + " sec");
               lore.add(ChatColor.BLUE + "  (" + FishStats.cooldown[type][level] + " sec cooldown)");
           }
       }
       im.setLore(lore);
       fish.setItemMeta(im);

       return fish;
   }

   // puts the equip effect on the player (or on the fish in case of pufferfish/thorns)
   public static void addEquipEffect(Player p, ItemStack fish) {
       int type = fish.getDurability();
       int level = getLevel(fish);

       if (FishStats.armorValue[type][level] != 0) {
           p.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(FishStats.armorValue[type][level]);
       }

       if (FishStats.onEquipEffect[type][level] == null && type != 3)
           return;

       if (type == 3) {
           ItemMeta im = fish.getItemMeta();
           im.addEnchant(Enchantment.THORNS, FishStats.enchant1level[type][level], true);
           fish.setItemMeta(im);
       }
       else {
           PotionEffect effect = new PotionEffect(FishStats.onEquipEffect[type][level], 20*1800, FishStats.equipEffectLevel[type][level]);
           p.addPotionEffect(effect, true);
       }
   }

   // removes the equip effect from the player or fish
   public static void removeEquipEffect(Player p, ItemStack fish) {
        int type = fish.getDurability();
        int level = getLevel(fish);

           p.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(0);

       if (FishStats.onEquipEffect[type][level] == null && type != 3)
           return;

       if (type == 3 ) {
           ItemMeta im = fish.getItemMeta();
           im.removeEnchant(Enchantment.THORNS);
           fish.setItemMeta(im);
       }
       else {
           p.removePotionEffect(FishStats.onEquipEffect[type][level]);
       }
   }

   // gives friendly effect name strings for the fish lore text
   private static String getEffectName(PotionEffectType type) {
       switch (type.getName()) {
           case "DAMAGE_RESISTANCE":
               return "Resistance";
           case "JUMP":
               return "Jump Boost";
           case "ABSORPTION":
               return "Absorption";
           case "INCREASE_DAMAGE":
               return "Strength";
           case "REGENERATION":
               return "Regeneration";
           case "HEAL":
               return "Instant Heal";
           case "SPEED":
               return "Speed";
           default:
               return "";
       }
   }

   // gives friendly roman numerals for the fish lore text
   private static String toRomanNum(int num) {
       switch (num) {
           case 0:
               return "I";
           case 1:
               return "II";
           case 2:
               return "III";
           case 3:
               return "IV";
           case 4:
               return "V";
           default:
               return "";
       }
   }

    // checks if the item is a special fish - looks for fish type and that it has lore
    public static boolean isFish(ItemStack item)
    {
        if (item != null)
            return item.getType().equals(Material.RAW_FISH) && item.hasItemMeta() && item.getItemMeta().hasLore();
        else
            return false;
    }

    // binds the fish to the player and puts a curse of binding on the fish
    public static void bindFish(final Player p) {
        UUID id = p.getUniqueId();
        // Restart timer if currently bound
        if (bindTracker.containsKey(id) && p.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.BINDING_CURSE))
        {
            BukkitTask task = bindTracker.get(id);
            if (task != null)
                task.cancel();
            bindTracker.remove(id);
        }
        else // fish not bound, so bind it
        {
            ItemMeta im = p.getInventory().getItemInOffHand().getItemMeta();

            if (!im.hasEnchant(Enchantment.BINDING_CURSE)) {
                im.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                p.getInventory().getItemInOffHand().setItemMeta(im);
            }
            if (bindTracker.containsKey(id))
            {
                BukkitTask task = bindTracker.get(id);
                if (task != null)
                    task.cancel();
                bindTracker.remove(id);
            }
        }
        // start the timer
        bindTracker.put(id, new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta im = p.getInventory().getItemInOffHand().getItemMeta();
                im.removeEnchant(Enchantment.BINDING_CURSE);
                p.getInventory().getItemInOffHand().setItemMeta(im);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("FishSlap"), 300L));
    }

    // removes the bind - this is used when a player leaves or joins the server with a bind on their fish
    public static void unbindFish(final Player p) {
        UUID id = p.getUniqueId();
        ItemMeta im = p.getInventory().getItemInOffHand().getItemMeta();

        if (bindTracker.containsKey(id)) {
            BukkitTask task = bindTracker.get(id);
            if (task != null)
                task.cancel();
            bindTracker.remove(id);
        }
        if (im.hasEnchant(Enchantment.BINDING_CURSE)) {
            im.removeEnchant(Enchantment.BINDING_CURSE);
            p.getInventory().getItemInOffHand().setItemMeta(im);
        }
    }

    // returns the level of the given fish
    public static int getLevel(ItemStack fish) {
        List<String> lore = fish.getItemMeta().getLore();
        int level = 0;

        for (String s : lore) {
            if (s.contains("Level")) {
                String[] levelstr = s.split("\\s+");
                level = Integer.parseInt(levelstr[1]);
            }
        }
        return level;
    }

    // calculates the percent completion towards the next level of the given fish and returns it as a decimal
    public static float getXP(ItemStack fish) {
        List <String> lore = fish.getItemMeta().getLore();
        String xpLine = "";
        int haveXP;
        int needXP;

        for (String s : lore) {
            if (s.contains("xp"))
                xpLine = s;
        }
        if (xpLine.equals(""))
            return 0F;

        String[] xpHaveNeed = ChatColor.stripColor(xpLine).split("/");
        haveXP = Integer.parseInt(xpHaveNeed[0]);
        xpHaveNeed = xpHaveNeed[1].split("\\s+");
        needXP = Integer.parseInt(xpHaveNeed[0]);

        if (needXP != 0) {
            return (float) haveXP / (float) needXP;
        }
        else
            return 0F;
    }

    // adds xp to the fish and updates the player's xp bar
    public static void giveXP(Player p, ItemStack fish, int amount) {
        List <String> lore = fish.getItemMeta().getLore();
        String xpLine = "";
        int index;

        for (String s : lore) {
            if (s.contains("xp"))
                xpLine = s;
        }
        index = lore.indexOf(xpLine);

        String[] xpHaveNeed = ChatColor.stripColor(xpLine).split("/");
        int haveXP = Integer.parseInt(xpHaveNeed[0]);
        xpHaveNeed = xpHaveNeed[1].split("\\s+");
        int needXP = Integer.parseInt(xpHaveNeed[0]);

        haveXP += amount;

        if (haveXP >= needXP && getLevel(fish) < 10) {
            levelUp(p, fish);
        }
        else
        {
            if (getLevel(fish) >= 10 && haveXP > needXP)
                haveXP = needXP;
            xpLine = ChatColor.DARK_AQUA + "" + haveXP + "/" + needXP + " xp";

            lore.set(index,xpLine);
            ItemMeta im = fish.getItemMeta();
            im.setLore(lore);
            fish.setItemMeta(im);

            if (!GameTracker.isOnCooldown(p)) {
                p.setExp(getXP(fish));
            }
        }
    }

    // levels up a fish - simply replaces it with another that is one level higher
    public static void levelUp(Player p, ItemStack fish) {
        int level = getLevel(fish) + 1;
        int type = fish.getDurability();

        p.getInventory().setItemInMainHand(giveFish((short) type, level));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1F,0F);

        if (!GameTracker.isOnCooldown(p)) {
            p.setExp(0);
            p.setLevel(level);
        }
    }
}
