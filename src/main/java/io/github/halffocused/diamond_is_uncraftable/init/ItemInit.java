package io.github.halffocused.diamond_is_uncraftable.init;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable.JojoItemGroup;
import io.github.halffocused.diamond_is_uncraftable.item.StandArrowItem;
import io.github.halffocused.diamond_is_uncraftable.item.StandDiscItem;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class reminds me of how stupid {@link DeferredRegister} looks when used to register lots of things.
 */
@SuppressWarnings("unused")
public class ItemInit {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DiamondIsUncraftable.MOD_ID);

    static StandArrowTooltip theWorldTooltip = new StandArrowTooltip("The World's speed and power are bested only by it's ability to stop time. The ultimate close-quarters combatant.")
            .addAbility("Passive 1 - Momentum Barrage", "The World's barrage is really powerful, but using it drains momentum until empty. When out of momentum due to this effect, stop barraging.")
            .addAbility("Passive 2 - Survival Reflex", "If The World has been unsummoned for over a minute, when you take fatal ATTACK damage: survive it with 2 hearts, summon The World, and instantly use a full charge taunt. When summoned this way, the World starts with 50 energy. This passive goes on cooldown for ten minutes.")
            .addAbility("Held Input - Taunt", "When released, stop time to move all entities away from The World's master. The distance is determined by how long it is charged. Drains energy to charge. Cancels moves of any stand user hit. Has no effect while time is stopped.")
            .addAbility("Ability 1 - Time Stop", "The World begins to stop time. This can be cancelled by attacking it's master. If cancelled in this way, lose 30 energy. While time is stopped, input the ability again to end Time Stop early. Costs 11 energy per second.")
            .addAbility("Time Stop: Stopped Entities", "Entities under the influence of Time Stop take half damage from all sources. All damage is received after the stop is ended. Attacking entities during Time Stop does not grant momentum.")
            .addAbility("Ability 2 - Quick Stop", "Instantly teleport a short distance forward. Can not be used while time is stopped or if The World's master has been attacked in the last 2 seconds. Costs 40 energy.");

    static StandArrowTooltip silverChariotTooltip = new StandArrowTooltip("The fastest stand, shred your opponents with Silver Chariot's sword and then shed it's armor to reach blazing speeds!")
            .addAbility("Passive 1 - Armor", "Silver Chariot transfers less damage to it's master while armored, but more while unarmored. While unarmored, all basic attacks are replaced with more powerful attacks, and attacks grant a small amount of stand energy.")
            .addAbility("Passive 2 - Relentless", "Whenever a move first hits, gain increasing levels of Stand Strength. The level continues to increase until Silver Chariot does not hit anything for 5 seconds. Maxes out at Stand Strength VII.")
            .addAbility("Passive 3 - Overexertion", "For every level of Stand Strength Silver Chariot's master has, they lose 2.5 stand energy per second. If they run out of energy due to this effect, they lose all Stand Strength and gain 50 energy.")
            .addAbility("Toggled Ability - Switch Armor", "Switch Silver Chariot's armor state. Can only be performed while above 0 momentum. Automatically activates if hitting 0 momentum while unarmored. Passive momentum loss is faster while active. When entering unarmored, gain 100 energy.")
            .addAbility("Ability 1 - Shoot Sword", "Can only be used while armored. Silver Chariot shoots it's sword in front of itself. Deals 18 unblockable damage. For the next 7 seconds, Silver Chariot is not actionable.")
            .addAbility("Ability 1 - Counter", "Can only be used while unarmored. Silver Chariot enters a defensive position and, if it's master is attacked, does a strong area attack. Restores 20 momentum upon landing. Costs 40 energy.");

    static StandArrowTooltip kingCrimsonTooltip = new StandArrowTooltip("Devastatingly powerful, see into the future and skip to your desired outcome with King Crimson's time manipulating abilities.")
            .addAbility("Passive - Follow Up", "After a basic or charged attack hits an entity, input Time Erase to spend 15 energy for King Crimson to teleport behind the target and follow-up with an unblockable chop. If it hits, apply Stand Weakness II for 4 seconds.")
            .addAbility("Held Input - Charge Punch", "King Crimson charges a devastating punch. After charging for 10 seconds, the punch is replaced by the incredibly powerful Donut Punch command grab.")
            .addAbility("Ability 1 - Epitaph Predict and Time Erase", "Channeled. Can only be activated at full energy. While channeling, all affected entities are invincible. Glowing outlines appear to indicate future movements of affected entities. When channeling ends, all entities are teleported to their predicted positions.")
            .addAbility("Ability 2 - Epitaph Counter", "Enter countering state. If attacked during this time, damage is nullified and a 5 second time skip is began. For 2 seconds after the skip, King Crimson's basic attack is replaced by a devastating chop. Costs 90 energy.");

    static StandArrowTooltip killerQueenTooltip = new StandArrowTooltip("This terrifying stand posses the ability to transform anything it touches into a bomb, as well as summon the unstoppable Sheer Heart Attack. Is there more to this power?")
            .addAbility("Passive 1 - First Bomb", "Killer Queen can have one active First Bomb at a time. The bomb can take the form of an item, block, or entity. Setting the First Bomb onto anything will remove it's presence from whatever it was previously set to.")
            .addAbility("Passive 2 - Automatic Detonation", "If First Bomb is an item, it explodes automatically when picked up by any player.")
            .addAbility("Passive 3 - Bomb Sense", "Particles from this passive can only be seen by you. Your First Bomb emits black swirling particles. Anything that has your First Bomb emits white swirling particles. Any player with your First Bomb in their Ender Chest emit Ender particles.")
            .addAbility("Held Input - First Bomb (Entity)", "Charge up a chop that, although slow, turns any entity hit into Killer Queen's current bomb. If blocked, turn their shield into the First Bomb. If entity hit is below 15% HP or 3 hitpoints, a command grab occurs instead, instantly killing the mob and not placing detonate on cooldown.")
            .addAbility("Ability 1 - Detonate", "Killer Queen detonates it's First Bomb. If damaged during this time, the detonation is cancelled. Killer Queen's First Bomb is removed from it's current target. Detonated mobs die instantly but do not drop death loot. Detonated players take large damage. Costs 65 energy.")
            .addAbility("Ability 2 - Summon Sheer Heart Attack", "Killer Queen summons the heat-seeking Sheer Heart Attack to pursue nearby entities. Sheer Heart attack will home onto nearby entities. After a short fuse time, it explodes for incredible damage.")
            .addAbility("Ability 3 - First Bomb Set", "If sneaking, set the First Bomb on the block below Killer Queen's master. Otherwise, set the First Bomb on Killer Queen's master's held item, if present. Requires an open hotbar slot.");

    public static final RegistryObject<Item> STAND_ARROW = ITEMS.register("stand_arrow", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("arrow", TextFormatting.YELLOW)).group(JojoItemGroup.INSTANCE), 0, "On use, grants the user the power of a STAND."));
    public static final RegistryObject<Item> SUMMON_KING_CRIMSON = ITEMS.register("summon_king_crimson", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("king_crimson", TextFormatting.DARK_RED)).group(JojoItemGroup.INSTANCE), Util.StandID.KING_CRIMSON, kingCrimsonTooltip));
    public static final RegistryObject<Item> SUMMON_KILLER_QUEEN = ITEMS.register("summon_killer_queen", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("killer_queen", TextFormatting.GRAY)).group(JojoItemGroup.INSTANCE), Util.StandID.KILLER_QUEEN, killerQueenTooltip));
    public static final RegistryObject<Item> SUMMON_SILVER_CHARIOT = ITEMS.register("summon_silver_chariot", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("silver_chariot", TextFormatting.GRAY)).group(JojoItemGroup.INSTANCE), Util.StandID.SILVER_CHARIOT, silverChariotTooltip));
    public static final RegistryObject<Item> SUMMON_THE_WORLD = ITEMS.register("summon_the_world", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("the_world", TextFormatting.YELLOW)).group(JojoItemGroup.INSTANCE), Util.StandID.THE_WORLD, theWorldTooltip));

    /*
    public static final RegistryObject<Item> SUMMON_D4C = ITEMS.register("summon_d4c", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("d4c", TextFormatting.AQUA)).group(JojoItemGroup.INSTANCE), Util.StandID.D4C, "Allows the user to hop to parallel worlds when between two objects.\n\nControls (All abilities require user to be holding either a Shield or a Banner): \nABILITY1: Activates a short distance teleport, effectively making the user go in and out of D4C's pocket dimension rapidly.\nABILITY2: D4C throws a punch with all of it's power, this punch is double as fast and has double the range of a regular punch, upon hitting an entity, it sends it to a parallel world."));
    public static final RegistryObject<Item> SUMMON_GOLD_EXPERIENCE = ITEMS.register("summon_gold_experience", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("gold_experience", TextFormatting.GOLD)).group(JojoItemGroup.INSTANCE), Util.StandID.GOLD_EXPERIENCE, "Has the ability to turn objects into living creatures."));
    public static final RegistryObject<Item> SUMMON_MADE_IN_HEAVEN = ITEMS.register("summon_made_in_heaven", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("made_in_heaven", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.MADE_IN_HEAVEN, "Possess the ability to accelerate the passage of time. \n\nControls: \nABILITY1: Dashes forward, seemingly teleporting Made in Heaven and it's user 70 blocks forwards. \nABILITY2: Made in Heaven uses it's immense speed to dodge and counter all attacks for the next 10 seconds."));
    public static final RegistryObject<Item> SUMMON_AEROSMITH = ITEMS.register("summon_aerosmith", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("aerosmith", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.AEROSMITH, "Remote controlled Stand, allows the user to detect entities by their breathing.\n\nControls: \nABILITY1: Shoots a piece of TNT with a fuse of only 1 second."));
    public static final RegistryObject<Item> SUMMON_WEATHER_REPORT = ITEMS.register("summon_weather_report", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("weather_report", TextFormatting.WHITE)).group(JojoItemGroup.INSTANCE), Util.StandID.WEATHER_REPORT, "Possess the ability to control the weather, is there something more to this ability?\n\nControls: \nABILITY1: Changes the current Weather of the world."));
    public static final RegistryObject<Item> SUMMON_CRAZY_DIAMOND = ITEMS.register("summon_crazy_diamond", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("crazy_diamond", TextFormatting.BLUE)).group(JojoItemGroup.INSTANCE), Util.StandID.CRAZY_DIAMOND, "Has the ability to revert objects to a previous state.\n\nControls: \nABILITY1: Reverts punched blocks to their previous state, repairing them."));
    public static final RegistryObject<Item> SUMMON_PURPLE_HAZE = ITEMS.register("summon_purple_haze", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("purple_haze", TextFormatting.DARK_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.PURPLE_HAZE, "Releases a deadly virus into the atmosphere. \n\nControls: \nABILITY1: Makes Purple Haze stop following it's user, allowing it's user to use it without being in danger of getting infected by it's virus."));
    public static final RegistryObject<Item> SUMMON_THE_EMPEROR = ITEMS.register("summon_the_emperor", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("emperor", TextFormatting.DARK_GRAY)).group(JojoItemGroup.INSTANCE), Util.StandID.THE_EMPEROR, "The Emperor acts like a gun, dealing massive damage to entities it hits."));
    public static final RegistryObject<Item> SUMMON_WHITESNAKE = ITEMS.register("summon_whitesnake", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("whitesnake", TextFormatting.WHITE)).group(JojoItemGroup.INSTANCE), Util.StandID.WHITESNAKE, "Possesses the ability to take players' Stands and turn them into discs."));
    public static final RegistryObject<Item> SUMMON_CMOON = ITEMS.register("summon_cmoon", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("cmoon", TextFormatting.DARK_GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.CMOON, "Has almost perfect control over gravity, allows the user to levitate and invert entities."));
    public static final RegistryObject<Item> SUMMON_STAR_PLATINUM = ITEMS.register("summon_star_platinum", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("star_platinum", TextFormatting.DARK_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.STAR_PLATINUM, "Allows it's user to stop the flow of time for up to 5 seconds, akin to The World, only weaker.\n\nControls: \nABILITY1: Stops time briefly and walks forwards, effectively teleporting.\nABILITY2: Dodges all attacks by stopping time and moving behind the attacking entity."));
    public static final RegistryObject<Item> SUMMON_MAGICIANS_RED = ITEMS.register("summon_magicians_red", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("magicians_red", TextFormatting.RED)).group(JojoItemGroup.INSTANCE), Util.StandID.MAGICIANS_RED, "Can control and create flames, does not make it's user flame proof.\n\nControls: \nABILITY1: Shoots out the Crossfire Hurricane, an explosive flame blast."));
    public static final RegistryObject<Item> SUMMON_THE_HAND = ITEMS.register("summon_the_hand", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("the_hand", TextFormatting.DARK_BLUE)).group(JojoItemGroup.INSTANCE), Util.StandID.THE_HAND, "Erases anything it's right hand swipes at, very slow Stand.\n\nControls: \nABILITY1: Swipes away the space between itself and an entity, effectively pulling it.\nABILITY2: Swipes away the space in front of it's master, teleporting them forwards."));
    public static final RegistryObject<Item> SUMMON_HIEROPHANT_GREEN = ITEMS.register("summon_hierophant_green", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("hierophant_green", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.HIEROPHANT_GREEN, "Long range Stand, can use it' tail to whip and throw the Emerald splash.\n\nControls: \nBasic attack + Ability ON: Whips an entity with it's tail possessing it and allowing it's user to control it."));
    public static final RegistryObject<Item> SUMMON_GREEN_DAY = ITEMS.register("summon_green_day", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("green_day", TextFormatting.DARK_GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.GREEN_DAY, "Can spread a fungal infection to entities that descend from their current position."));
    public static final RegistryObject<Item> SUMMON_20TH_CENTURY_BOY = ITEMS.register("summon_20th_century_boy", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("20th_century_boy", TextFormatting.DARK_BLUE)).group(JojoItemGroup.INSTANCE), Util.StandID.TWENTIETH_CENTURY_BOY, "Makes it's user completely invincible, but prevents them from moving."));
    public static final RegistryObject<Item> SUMMON_THE_GRATEFUL_DEAD = ITEMS.register("summon_the_grateful_dead", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("the_grateful_dead", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.THE_GRATEFUL_DEAD, "Speeds up aging of crops and entities, making them either grow faster or gain negative effects from aging."));
    public static final RegistryObject<Item> SUMMON_STICKY_FINGERS = ITEMS.register("summon_sticky_fingers", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("sticky_fingers", TextFormatting.DARK_BLUE)).group(JojoItemGroup.INSTANCE), Util.StandID.STICKY_FINGERS, "Possesses the ability to create zippers on objects and entities.\n\nControls: \nABILITYTOGGLE: Rapidly places zippers on the ground, allowing it's user to swim through the floor.\nABILITY1: Puts a zipper on an entity and jumps inside of it, hiding itself and it's user.\nABILITY2: Puts a zipper on a wall and passes through it.\nABILITY3: Unzips it's arm and punches with it going 5 times as far as a regular punch."));
    public static final RegistryObject<Item> SUMMON_TUSK_ACT_1 = ITEMS.register("summon_tusk_act_1", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("tusk_act_1", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.TUSK_ACT_1, "Has the ability to charge and shoot bullets made from the user's nails."));
    public static final RegistryObject<Item> SUMMON_TUSK_ACT_2 = ITEMS.register("summon_tusk_act_2", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("tusk_act_2", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.TUSK_ACT_2, "Can shoot homing nail bullets, deals double the damage of Act 1 but has double the cooldown. Drinking honey bottles reduces the cooldown."));
    public static final RegistryObject<Item> SUMMON_TUSK_ACT_3 = ITEMS.register("summon_tusk_act_3", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("tusk_act_3", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.TUSK_ACT_3, "Same as Act 2, but can also shoot nails into the floor to go into the ground."));
    public static final RegistryObject<Item> SUMMON_TUSK_ACT_4 = ITEMS.register("summon_tusk_act_4", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("tusk_act_4", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.TUSK_ACT_4, "Can create an infinite rotational force that makes targets spin forever, deals a max of 59 damage. All infinite rotation benefits are only applied if it's user is riding a horse."));
    public static final RegistryObject<Item> SUMMON_ECHOES_ACT_1 = ITEMS.register("summon_echoes_act_1", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("echoes_act_1", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.ECHOES_ACT_1, "Can create sound effects and place them on entities damaging them over time."));
    public static final RegistryObject<Item> SUMMON_ECHOES_ACT_2 = ITEMS.register("summon_echoes_act_2", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("echoes_act_2", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.ECHOES_ACT_2, "Can put sound effects on blocks, effects include: Sizzle, Bounce, Damage or Explode.\n\nControls: \nABILITY1: Places a sound effect 2 blocks in front of Echoes' user.\nABILITY2: Removes all currently placed sound effects."));
    public static final RegistryObject<Item> SUMMON_ECHOES_ACT_3 = ITEMS.register("summon_echoes_act_3", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("echoes_act_3", TextFormatting.GREEN)).group(JojoItemGroup.INSTANCE), Util.StandID.ECHOES_ACT_3, "Gains access to the Three Freeze attack which makes entities heavy and unable to move.\n\nControls: \nABILITY1: Effects all entities within 7 blocks of Echoes with Three Freeze."));
    public static final RegistryObject<Item> SUMMON_BEACH_BOY = ITEMS.register("summon_beach_boy", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("beach_boy", TextFormatting.LIGHT_PURPLE)).group(JojoItemGroup.INSTANCE), Util.StandID.BEACH_BOY, "Acts as a fishing rod, but can also be used to attack.\n\nStates: \nFishing rod: Acts as a normal fishing rod, can also fish in lava and End portals.\nDamage: Instead of pulling entities towards itself, Beach Boy now damages entities upon pullling them.\nHoming: Beach Boy automatically homes in on entities and damages them upon pulling.\nControls: \nSWITCHACT: Switches Beach Boy's current state."));
    */

    public static final RegistryObject<Item> STAND_DISC = ITEMS.register("stand_disc", () -> new StandDiscItem(new Item.Properties().maxStackSize(1).group(JojoItemGroup.INSTANCE)));

    /*
    public static final RegistryObject<Item> CANZONI_PREFERITE = ITEMS.register("canzoni_preferite", () -> new MusicDiscItem(69, SoundInit.CANZONI_PREFERITE, new Item.Properties().maxStackSize(1).rarity(Rarity.RARE).group(JojoItemGroup.INSTANCE)));
    */

    public static class StandArrowTooltip{ //Unneeded? Yes!
        private final StringTextComponent moveset;
        private final StringTextComponent description;

        Style descriptionStyle = Style.EMPTY.setFormatting(TextFormatting.GOLD).setUnderlined(true);
        Style abilityNameStyle = Style.EMPTY.setFormatting(TextFormatting.GRAY);
        Style abilityDescriptionStyle = Style.EMPTY.setFormatting(TextFormatting.DARK_GRAY).setItalic(true).setUnderlined(false);

        public StandArrowTooltip(String descriptionIn){
            moveset = new StringTextComponent("");
            description = new StringTextComponent(descriptionIn);
            description.setStyle(descriptionStyle);
        }

        public StandArrowTooltip addAbility(String abilityName, String abilityDescription){
            if(!moveset.equals(new StringTextComponent(""))) {
                moveset.appendSibling(new StringTextComponent("\n").setStyle(abilityNameStyle));
            }
            moveset.appendSibling(new StringTextComponent(abilityName + ":").setStyle(abilityNameStyle));
            moveset.appendSibling(new StringTextComponent(" " + abilityDescription).setStyle(abilityDescriptionStyle));
            return this;
        }

        public StringTextComponent getDescription(){
            return description;
        }

        public StringTextComponent getMoveset(){
            return moveset;
        }
    }
}