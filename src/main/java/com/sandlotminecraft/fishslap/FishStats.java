package com.sandlotminecraft.fishslap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;


// this is all of the stats for the different types and levels of fish
// rows are by type and in order: cod, salmon, grouper, pufferfish
// entries in each row are in increasing order by level starting at zero
public class FishStats {

    // display name
    public static String[] displayName = {"&9&oA Crusty Cod", "&d&oA Slimy Salmon", "&6&oA Grody Grouper", "&e&oA Prickly Pufferfish"};

    //Damage
    public static int[][] damage = {
            {0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5}, //cod
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, //salmon
            {0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5}, //grouper
            {0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2} //pufferfish
    };

    // the enchantment on the fish if there is one
    public static Enchantment[][] enchant1 = {
            {null, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK, Enchantment.KNOCKBACK},
            {null, null, null, null, null, null, null, null, null, null, null},
            {null, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, Enchantment.MENDING, },
            {null, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, Enchantment.THORNS, }
    };

    // the level of the above enchantment
    public static int[][] enchant1level = {
            {0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4},
    };

    // the level/distance/damage modifier of the 'sweeping edge' enchant on the cod
    public static int[] aoeDistance = {0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3};

    // the effect that is given when the fish is equipped
    public static PotionEffectType[][] onEquipEffect = {
            {null, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE},
            {null, null, null, null, PotionEffectType.JUMP, PotionEffectType.JUMP, PotionEffectType.JUMP, PotionEffectType.JUMP, PotionEffectType.JUMP, PotionEffectType.JUMP, PotionEffectType.JUMP},
            {null, null, null, null, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION},
            {null, null, null, null, null, null, null, null, null, null, null}
    };

    // the level of the effect given when the fish is equipped
    public static int[][] equipEffectLevel = {
            {0, 0, 0, 1, 1,1,2,2,2,3,3},
            {0, 0, 0, 0, 0,0,0,1,1,1,1},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    // the level of the poison debuff given by the pufferfish
    public static int[] poisonEffectLevel = {0, 0,0,0,0,0,1,1,1,1,1};

    // the duration of the poison given by the pufferfish
    public static int[] poisonEffectDuration = {0, 4, 4, 6, 6, 6, 8, 8, 8, 10, 10};

    // the chance for each hit from the pufferfish to poison the target
    public static int[] poisonChance = {0, 20, 20, 20, 30, 30, 30, 35, 40, 45, 50};

    // the experience needed to reach the next level
    public static int[] xpNeeded = {0, 100, 150, 200, 300, 400, 500, 600, 800, 1000, 2000};

    // the effect given when the fish is used (right click effect) from the off-hand
    public static PotionEffectType[][] onUseEffect = {
            {null, null, null, null, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION, PotionEffectType.ABSORPTION},
            {null, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.INCREASE_DAMAGE},
            {null, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.REGENERATION, PotionEffectType.HEAL, PotionEffectType.HEAL, PotionEffectType.HEAL, PotionEffectType.HEAL, PotionEffectType.HEAL, PotionEffectType.HEAL, PotionEffectType.HEAL},
            {null, null, null, null, null, null, null, null, null, null, null}
    };

    // the duration of the use effect
    public static int[][] useEffectDuration = {
            {0, 0,0,0,8,10,12,12,12,12,12},
            {0, 5,6,7,8,9,10,10,12,12,12},
            {0, 12,16,20,0,0,0,0,0,0,0},
            {0, 0,0,0,0,0,0,0,0,0,0}
    };

    // the level of the use effect
    public static int[][] useEffectLevel = {
            {0, 0,0,0,0,0,0,1,1,1,2},
            {0, 0,0,0,0,0,0,1,1,1,1},
            {0, 0,0,0,0,4,6,6,6,6,6},
            {0, 0,0,0,0,0,0,0,0,0,0}
    };

    // the cooldown of the use effect (in seconds)
    public static int[][] cooldown = {
            {0, 0,0,0,60,60,60,60,55,50,45},
            {0, 45,45,45,45,45,45,45,45,40,35},
            {0, 30,30,30,60,60,60,45,45,30,30},
            {0, 60,55,50,45,40,35,30,25,20,10}
    };

}
