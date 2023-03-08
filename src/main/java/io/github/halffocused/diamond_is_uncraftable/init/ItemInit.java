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
            .addAbility("Ability 1 - Counter", "Can only be used while unarmored. Silver Chariot enters a defensive position and, if it's master is attacked, negates the damage and does a strong area attack. Restores 20 momentum upon landing. Costs 40 energy.");

    static StandArrowTooltip kingCrimsonTooltip = new StandArrowTooltip("Devastatingly powerful, see into the future and skip to your desired outcome with King Crimson's time manipulating abilities.")
            .addAbility("Passive - Follow Up", "After a basic or charged attack hits an entity, input Time Erase to spend 15 energy for King Crimson to teleport behind the target and follow-up with an unblockable chop. If it hits, apply Stand Weakness II for 4 seconds.")
            .addAbility("Held Input - Charge Punch", "King Crimson charges a devastating punch. After charging for 10 seconds, the punch is replaced by the incredibly powerful Donut Punch command grab.")
            .addAbility("Ability 1 - Epitaph Predict and Time Erase", "Channeled. Can only be activated at full energy. While channeling, all affected entities are invincible. Glowing outlines appear to indicate future movements of affected entities. When channeling ends, all entities are teleported to their predicted positions.")
            .addAbility("Ability 2 - Epitaph Counter", "Enter countering state. If attacked during this time, damage is nullified and a 5 second time skip is began. For 2 seconds after the skip, King Crimson's basic attack is replaced by a devastating chop. Costs 90 energy.");

    static StandArrowTooltip killerQueenTooltip = new StandArrowTooltip("This terrifying stand posses the ability to transform anything it touches into a bomb, as well as summon the unstoppable Sheer Heart Attack. Is there more to this power?")
            .addAbility("Passive 1 - First Bomb", "Killer Queen can have one active First Bomb at a time. The bomb can take the form of an item, block, or entity. Setting the First Bomb onto anything will remove it's presence from whatever it was previously set to.")
            .addAbility("Passive 2 - Automatic Detonation", "If First Bomb is an item, it explodes automatically when picked up by any player.")
            //.addAbility("Passive 3 - Bomb Sense", "Particles from this passive can only be seen by you. Your First Bomb emits black swirling particles. Anything that has your First Bomb emits white swirling particles. Any player with your First Bomb in their Ender Chest emit Ender particles.")
            .addAbility("Held Input - First Bomb (Entity)", "Charge up a chop that, although slow, turns any entity hit into Killer Queen's current bomb. If blocked, turn their shield into the First Bomb. If entity hit is below 15% HP or 3 HP, a command grab occurs instead, instantly killing the mob and not placing detonate on cooldown.")
            .addAbility("Ability 1 - Detonate", "Killer Queen detonates it's First Bomb. If damaged during this time, the detonation is cancelled. Killer Queen's First Bomb is removed from it's current target. Detonated mobs die instantly but do not drop death loot. Detonated players take large damage. Costs 65 energy.")
            .addAbility("Ability 2 - Summon Sheer Heart Attack", "Killer Queen summons the heat-seeking Sheer Heart Attack to pursue nearby entities. Sheer Heart attack will home onto nearby entities. After a short fuse time, it explodes for incredible damage.")
            .addAbility("Ability 3 - First Bomb Set", "If sneaking, set the First Bomb on the block below Killer Queen's master. Otherwise, set the First Bomb on Killer Queen's master's held item, if present. Requires an open hotbar slot.");

    public static final RegistryObject<Item> STAND_ARROW = ITEMS.register("stand_arrow", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("arrow", TextFormatting.YELLOW)).group(JojoItemGroup.INSTANCE), 0, "On use, grants the user the power of a STAND."));
    public static final RegistryObject<Item> SUMMON_KING_CRIMSON = ITEMS.register("summon_king_crimson", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("king_crimson", TextFormatting.DARK_RED)).group(JojoItemGroup.INSTANCE), Util.StandID.KING_CRIMSON, kingCrimsonTooltip));
    public static final RegistryObject<Item> SUMMON_KILLER_QUEEN = ITEMS.register("summon_killer_queen", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("killer_queen", TextFormatting.GRAY)).group(JojoItemGroup.INSTANCE), Util.StandID.KILLER_QUEEN, killerQueenTooltip));
    public static final RegistryObject<Item> SUMMON_SILVER_CHARIOT = ITEMS.register("summon_silver_chariot", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("silver_chariot", TextFormatting.GRAY)).group(JojoItemGroup.INSTANCE), Util.StandID.SILVER_CHARIOT, silverChariotTooltip));
    public static final RegistryObject<Item> SUMMON_THE_WORLD = ITEMS.register("summon_the_world", () -> new StandArrowItem(new Item.Properties().maxStackSize(1).rarity(Rarity.create("the_world", TextFormatting.YELLOW)).group(JojoItemGroup.INSTANCE), Util.StandID.THE_WORLD, theWorldTooltip));

    public static final RegistryObject<Item> STAND_DISC = ITEMS.register("stand_disc", () -> new StandDiscItem(new Item.Properties().maxStackSize(1).group(JojoItemGroup.INSTANCE)));

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