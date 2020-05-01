package com.sandlotminecraft.fishslap;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

// this handles catching fish
public class Fishing implements Listener {

    @EventHandler
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            if (PlayerFishEvent.State.CAUGHT_FISH.equals(event.getState())) {
                Player p = event.getPlayer();
                ItemStack offhand = p.getInventory().getItemInOffHand();
                short found = 0;

                //Get rid of the actual caught entity
                event.getCaught().remove();
                event.setExpToDrop(0);

                // If the player isn't currently "playing", they can only catch Cod or small amounts of currency
                if (!GameTracker.isPlaying(p)) {
                    ItemStack[] inventory = p.getInventory().getContents();

                    // Check for cod
                    for (int i = 1; i < inventory.length; i++) {
                        if (inventory[i] == null)
                            continue;

                        if (inventory[i].getType().equals(Material.RAW_FISH) &&
                                inventory[i].getDurability() == (short) 0 &&
                                inventory[i].hasItemMeta())
                            found++;
                    }

                    // If the player doesn't yet have 2 cod, they have a chance to get one, otherwise they get 1-3 iron nuggets
                    Random rand = new Random();
                    if (found < 2) {

                        // 75% chance to get a cod if they have none, 50% chance if they do
                        if (rand.nextInt(100) > (1 + found) * 25) {
                            p.getInventory().addItem(Fish.giveFish((short) 0, 1));
                            p.sendMessage("You caught " + Fish.giveFish((short) 0, 1).getItemMeta().getDisplayName());
                        } else // they didn't catch one, give nuggets instead
                        {
                            int amount = rand.nextInt(3) + 1;
                            ItemStack nuggets = new ItemStack(Material.IRON_NUGGET, amount);

                            p.getInventory().addItem(nuggets);
                            p.sendMessage(amount == 1 ? "You caught an iron nugget!" : "You caught " + amount + " iron nuggets!");
                        }
                    } else // Player already has 2 cod, give 1-3 iron nuggets
                    {
                        int amount = rand.nextInt(3) + 1;
                        ItemStack nuggets = new ItemStack(Material.IRON_NUGGET, amount);

                        p.getInventory().addItem(nuggets);
                        p.sendMessage(amount == 1 ? "You caught an iron nugget!" : "You caught " + amount + " iron nuggets!");
                    }
                }

                // Rewards for players playing the game
                else if (GameTracker.isPlaying(p)) {
                    Random rand = new Random();
                    found = 0;
                    // get the fishing rod's luck enchantment
                    // used to slightly increase chance of better loot
                    // or gives one extra iron nugget per level if nothing else was caught
                    ItemStack rod = p.getInventory().getItemInMainHand();
                    ItemMeta rodMeta = rod.getItemMeta();
                    int modifier = 0;
                    if (rodMeta.hasEnchant(Enchantment.LUCK))
                        modifier = rodMeta.getEnchantLevel(Enchantment.LUCK);

                    /*
                    Players have a chance to catch a special fish:
                    5% Chance to catch a Pufferfish
                    ~10% Chance to catch a Grouper
                    ~25% Chance to catch a Salmon
                    ~50% Chance to catch a Cod
                    */

                    GameTracker.addScore(p, null, rand.nextInt(5));
                    if (Fish.isFish(p.getInventory().getItemInOffHand()))
                        Fish.giveXP(p,p.getInventory().getItemInOffHand(),rand.nextInt(2));


                    ItemStack[] inventory = p.getInventory().getContents();
                    short fishType = 4;

                    // Roll for the catch
                    if (rand.nextInt(100 + modifier) >= 94)
                        fishType = 3; // pufferfish
                    else if (rand.nextInt(100 + modifier) >= 89)
                        fishType = 2; // grouper
                    else if (rand.nextInt(100 + modifier) >= 74)
                        fishType = 1; // salmon
                    else if (rand.nextInt(100 + modifier) >= 49)
                        fishType = 0; // cod

                    // Check for existing fish of the same type
                    if (fishType < 4 && p.getInventory().contains(Material.RAW_FISH)) {
                        for (int i = 1; i < inventory.length; i++) // Loop through the inventory and look for a special fish of same type
                        {
                            if (inventory[i] == null)
                                continue;

                            if (Fish.isFish(inventory[i]) && inventory[i].getDurability() == fishType) {
                                // found a special fish of same type as rolled
                                found++;
                            }
                        }
                    }

                    // Player rolled for a fish and doesn't have two already, so give one
                    if (fishType < 4 && found < 2) {
                        p.getInventory().addItem(Fish.giveFish(fishType, 1));
                        p.sendMessage("You caught a " + Fish.giveFish(fishType, 1).getItemMeta().getDisplayName());
                    } else // already has two or didn't roll for a fish, give some currency or a potion
                    {


                        /* Chances to earn:
                        Diamond: .01%
                        Gold Block: ~.1%
                        Potion: ~5%
                        Gold Bar: ~1%
                        Iron Block: ~2%
                        Gold Nuggets (1-3): ~3%
                        Iron Bar: ~5%
                        Iron Nuggets (1-3): fallback
                        */

                        if (rand.nextInt(10000 + modifier) >= 9999) // Give a diamond
                        {
                            p.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                            p.sendMessage("You caught a diamond!");
                        } else if (rand.nextInt(1000 + modifier) >= 999) // Give a gold block
                        {
                            p.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK, 1));
                            p.sendMessage("You caught a block of gold!");
                        } else if (rand.nextInt(100 + modifier) >= 99) // Give a gold ingot
                        {
                            p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
                            p.sendMessage("You caught a gold bar!");
                        } else if (rand.nextInt(100 + modifier) >= 94) // Give a potion
                        {
                            ItemStack potion = new ItemStack(Material.POTION, 1);
                            PotionMeta pm = (PotionMeta) potion.getItemMeta();

                            // 10% chance for an offensive lingering potion
                            if (rand.nextInt(10 + modifier) == 10) {
                                potion.setType(Material.LINGERING_POTION);
                                PotionEffectType type = PotionEffectType.POISON;
                                int duration = 20 * (rand.nextInt(5) + 1);
                                int amplifier = 0;

                                switch (rand.nextInt(6)) {
                                    case 0:
                                        type = PotionEffectType.BLINDNESS;
                                        pm.setDisplayName("Lingering Potion of Blindness");
                                        pm.setColor(Color.GRAY);
                                        duration *= 2;
                                        break;
                                    case 1:
                                        type = PotionEffectType.HARM;
                                        duration = 1;
                                        amplifier = rand.nextInt(2);
                                        pm.setDisplayName(amplifier == 0 ? "Lingering Potion of Harming" : "Lingering Potion of Harming II");
                                        pm.setColor(Color.RED);
                                        break;
                                    case 2:
                                        type = PotionEffectType.SLOW;
                                        amplifier = rand.nextInt(2);
                                        duration *= 2;
                                        pm.setDisplayName(amplifier == 0 ? "Lingering Potion of Slowness" : "Lingering Potion of Slowness II");
                                        pm.setColor(Color.PURPLE);
                                        break;
                                    case 3:
                                        type = PotionEffectType.WEAKNESS;
                                        amplifier = rand.nextInt(2);
                                        duration *= 2;
                                        pm.setDisplayName(amplifier == 0 ? "Lingering Potion of Weakness" : "Lingering Potion of Weakness II");
                                        pm.setColor(Color.OLIVE);
                                        break;
                                    case 4:
                                        type = PotionEffectType.WITHER;
                                        amplifier = rand.nextInt(2);
                                        pm.setDisplayName("Lingering Potion of Withering");
                                        pm.setColor(Color.BLACK);
                                        break;
                                    default:
                                        amplifier = rand.nextInt(2);
                                        pm.setDisplayName(amplifier == 0 ? "Lingering Potion of Poison" : "Lingering Potion of Poison II");
                                }

                                pm.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                                potion.setItemMeta(pm);
                            }

                            // 40% chance for an offensive splash potion
                            else if (rand.nextInt(10 + modifier) >= 6) {
                                potion.setType(Material.SPLASH_POTION);
                                PotionEffectType type = PotionEffectType.POISON;
                                int duration = 20 * (rand.nextInt(16) + 5);
                                int amplifier = rand.nextInt(2);

                                switch (rand.nextInt(6)) {
                                    case 0:
                                        type = PotionEffectType.BLINDNESS;
                                        amplifier = 0;
                                        pm.setDisplayName("Splash Potion of Blindness");
                                        duration *= 2;
                                        pm.setColor(Color.GRAY);
                                        break;
                                    case 1:
                                        type = PotionEffectType.HARM;
                                        pm.setDisplayName(amplifier == 0 ? "Splash Potion of Harming" : "Splash Potion of Harming II");
                                        duration = 1;
                                        pm.setColor(Color.RED);
                                        break;
                                    case 2:
                                        type = PotionEffectType.SLOW;
                                        pm.setDisplayName(amplifier == 0 ? "Splash Potion of Slowness" : "Splash Potion of Slowness II");
                                        duration *= 2;
                                        pm.setColor(Color.PURPLE);
                                        break;
                                    case 3:
                                        type = PotionEffectType.WEAKNESS;
                                        pm.setDisplayName(amplifier == 0 ? "Splash Potion of Weakness" : "Splash Potion of Weakness II");
                                        duration *= 2;
                                        pm.setColor(Color.OLIVE);
                                        break;
                                    case 4:
                                        type = PotionEffectType.WITHER;
                                        amplifier = 0;
                                        pm.setDisplayName("Splash Potion of Withering");
                                        pm.setColor(Color.BLACK);
                                        break;
                                    default:
                                        pm.setDisplayName(amplifier == 0 ? "Splash Potion of Poison" : "Splash Potion of Poison II");
                                        pm.setColor(Color.GREEN);
                                }

                                pm.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                                potion.setItemMeta(pm);

                            }

                            // Otherwise it's a beneficial drinkable potion
                            else {
                                potion.setType(Material.POTION);
                                PotionEffectType type = PotionEffectType.HEAL;
                                int duration = 20 * (rand.nextInt(16) + 5); // set initial duration to 5 - 10 seconds
                                int amplifier = rand.nextInt(2);

                                switch (rand.nextInt(6)) {
                                    case 0:
                                        type = PotionEffectType.REGENERATION;
                                        pm.setDisplayName(amplifier == 0 ? "Potion of Regeneration" : "Potion of Regeneration II");
                                        pm.setColor(Color.FUCHSIA);
                                        break;
                                    case 1:
                                        type = PotionEffectType.DAMAGE_RESISTANCE;
                                        duration *= 10;
                                        pm.setDisplayName(amplifier == 0 ? "Potion of Resistance" : "Potion of Resistance II");
                                        pm.setColor(Color.SILVER);
                                        break;
                                    case 2:
                                        type = PotionEffectType.INCREASE_DAMAGE;
                                        pm.setDisplayName(amplifier == 0 ? "Potion of Strength" : "Potion of Strength II");
                                        duration *= 2;
                                        pm.setColor(Color.MAROON);
                                        break;
                                    case 3:
                                        type = PotionEffectType.INVISIBILITY;
                                        amplifier = 0;
                                        pm.setDisplayName("Potion of Invisibility");
                                        duration *= 3;
                                        pm.setColor(Color.NAVY);
                                        break;
                                    case 4:
                                        type = PotionEffectType.SPEED;
                                        pm.setDisplayName(amplifier == 0 ? "Potion of Speed" : "Potion of Speed II");
                                        duration *= 2;
                                        pm.setColor(Color.WHITE);
                                        break;
                                    default:
                                        pm.setDisplayName(amplifier == 0 ? "Potion of Healing" : "Potion of Healing II");
                                        pm.setColor(Color.LIME);

                                }

                                pm.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                                potion.setItemMeta(pm);

                            }

                            p.getInventory().addItem(potion);
                            p.sendMessage("You caught a " + potion.getItemMeta().getDisplayName());
                        } else if (rand.nextInt(50 + modifier) >= 49) // Give an iron block
                        {
                            p.getInventory().addItem(new ItemStack(Material.IRON_BLOCK, 1));
                            p.sendMessage("You caught an iron block!");
                        } else if (rand.nextInt(100 + modifier) >= 97) // Give 1-3 gold nuggets
                        {
                            int amount = rand.nextInt(3) + 1;

                            p.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, amount));
                            p.sendMessage(amount == 1 ? "You caught a gold nugget!" : "You caught " + amount + " gold nuggets!");
                        } else if (rand.nextInt(20 + modifier) >= 19) // Give an iron ingot
                        {
                            p.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
                            p.sendMessage("You caught an iron bar!");
                        } else // Give 1-3 iron nuggets
                        {
                            int amount = rand.nextInt(3 + modifier) + 1;

                            p.getInventory().addItem(new ItemStack(Material.IRON_NUGGET, amount));
                            p.sendMessage(amount == 1 ? "You caught an iron nugget!" : "You caught " + amount + " iron nuggets!");
                        }
                    }
                }
            }
        }
    }
}
