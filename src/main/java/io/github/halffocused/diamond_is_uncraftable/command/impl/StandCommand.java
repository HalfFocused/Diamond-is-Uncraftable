package io.github.halffocused.diamond_is_uncraftable.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("unused")
public class StandCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> literalCommandNode = dispatcher.register(Commands.literal("stand")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("king_crimson")
                                        .executes(context -> setPlayerStandID(context.getSource(), EntityArgument.getPlayer(context, "target"), Util.StandID.KING_CRIMSON)))
                                .then(Commands.literal("killer_queen")
                                        .executes(context -> setPlayerStandID(context.getSource(), EntityArgument.getPlayer(context, "target"), Util.StandID.KILLER_QUEEN)))
                                .then(Commands.literal("the_world")
                                        .executes(context -> setPlayerStandID(context.getSource(), EntityArgument.getPlayer(context, "target"), Util.StandID.THE_WORLD)))
                                .then(Commands.literal("silver_chariot")
                                        .executes(context -> setPlayerStandID(context.getSource(), EntityArgument.getPlayer(context, "target"), Util.StandID.SILVER_CHARIOT)))
                                .then(Commands.literal("sticky_fingers")
                                        .executes(context -> setPlayerStandID(context.getSource(), EntityArgument.getPlayer(context, "target"), Util.StandID.STICKY_FINGERS)))
                        ))
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> removePlayerStand(context.getSource(), EntityArgument.getPlayer(context, "target")))
                        ))

                .then(Commands.literal("evolve")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> evolvePlayerStand(context.getSource(), EntityArgument.getPlayer(context, "target")))
                        ))

        );
    }

    private static int setPlayerStandID(CommandSource source, PlayerEntity target, int standID) {
        Stand stand = Stand.getCapabilityFromPlayer(target);
        if (stand.getStandID() != standID) {
            stand.removeStand(false);
            stand.setStandID(standID);
            stand.setStandOn(false);
            source.sendFeedback(new StringTextComponent("Successfully set StandID for " + target.getDisplayName().getString() + "."), true);
        } else
            source.sendErrorMessage(new StringTextComponent(target.getDisplayName().getString() + " already has that Stand."));
        return stand.getStandID();
    }

    private static int removePlayerStand(CommandSource source, PlayerEntity target) {
        Stand props = Stand.getCapabilityFromPlayer(target);
        if (props.getStandID() != 0) {
            props.removeStand(false);
            source.sendFeedback(new StringTextComponent("Successfully removed Stand from " + target.getDisplayName().getUnformattedComponentText() + "."), true);
        } else
            source.sendErrorMessage(new StringTextComponent(target.getDisplayName() + " does not have a Stand."));
        return 1;
    }

    private static int evolvePlayerStand(CommandSource source, PlayerEntity target) {
        Stand stand = Stand.getCapabilityFromPlayer(target);
        int standID = 0;
        switch (stand.getStandID()) {
            default: {
                source.sendErrorMessage(new StringTextComponent(target.getDisplayName().getUnformattedComponentText() + "'s Stand cannot be evolved."));
                break;
            }
            case 0: {
                source.sendErrorMessage(new StringTextComponent(target.getDisplayName().getUnformattedComponentText() + " does not have a Stand."));
                break;
            }
            case Util.StandID.KILLER_QUEEN: {
                standID = Util.StandID.KILLER_QUEEN_BTD;
                break;
            }
        }
        if (standID != 0) {
            stand.removeStand(true);
            stand.setStandOn(false);
            stand.setStandID(standID);
            source.sendFeedback(new StringTextComponent("Successfully evolved " + target.getDisplayName().getUnformattedComponentText() + "'s Stand."), true);
        }
        return stand.getStandID();
    }
}
