package io.github.halffocused.diamond_is_uncraftable.item;


import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandPerWorldCapability;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.StandArrowEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.init.ItemInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class StandArrowItem extends ArrowItem {
    final int standID;
    final StringTextComponent tooltip;
    final StringTextComponent description;
    final String tooltip2;
    boolean newSystem;

    public StandArrowItem(Properties properties, int standID, ItemInit.StandArrowTooltip tooltip) {
        super(properties);
        this.standID = standID;
        this.description = tooltip.getDescription();
        this.tooltip = tooltip.getMoveset();
        this.tooltip2 = null;
        newSystem = true;
    }

    public StandArrowItem(Properties properties, int standID, String tooltip) {
        super(properties);
        this.standID = standID;
        this.tooltip = null;
        this.description = null;
        this.tooltip2 = tooltip;
        newSystem = false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override //TODO add an animation when obtaining GER
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof PlayerEntity)) return;
        if(!world.isRemote()) {
            PlayerEntity player = (PlayerEntity) entity;
            Stand.getLazyOptional(player).ifPresent(stand -> {
                int random;
                StandPerWorldCapability standPerWorld = StandPerWorldCapability.getCapabilityFromWorld(world.getServer().getWorld(entity.world.getDimensionKey()));
                int newStandID;
                if(DiamondIsUncraftableConfig.COMMON.uniqueStandMode.get()) {


                    if (standPerWorld.getTakenStandIDs().size() == Util.StandID.STANDS.length) {
                        player.sendStatusMessage(new StringTextComponent("There are no more available stands!"), true);
                        return;
                    }

                    do {
                        random = world.rand.nextInt(Util.StandID.STANDS.length);
                    } while (standPerWorld.getTakenStandIDs().contains(random));
                }else{
                    random = world.rand.nextInt(Util.StandID.STANDS.length);
                }

                if (standID == 0)
                    newStandID = Util.StandID.STANDS[random];
                else
                    newStandID = standID;

                if (stand.getStandID() == 0) {
                    if (!player.isCreative())
                        stack.shrink(1);
                    stand.setStandID(newStandID);
                    if(DiamondIsUncraftableConfig.COMMON.uniqueStandMode.get()) {
                        standPerWorld.addTakenStandId(random);
                    }
                    stand.setStandOn(true);
                    final AbstractStandEntity standEntity = Util.StandID.getStandByID(newStandID, world);
                    if (standEntity != null) { //Can be null if Stand is The Emperor or Beach Boy
                        standEntity.setLocationAndAngles(player.getPosX() + 0.1, player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
                        standEntity.setMasterUUID(player.getUniqueID());
                        standEntity.setMaster(player);
                        world.addEntity(standEntity);
                    }
                }
            });
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        Stand stand = Stand.getCapabilityFromPlayer(playerIn);
        if (!Stand.getLazyOptional(playerIn).isPresent()) return ActionResult.resultFail(stack);
        if (stand.getStandID() == 0) {
            playerIn.setActiveHand(handIn);
            return ActionResult.resultSuccess(stack);
        } else if (stand.getStandID() != 0) {
            playerIn.sendStatusMessage(new StringTextComponent("You already have a Stand!"), true);
            return ActionResult.resultFail(stack);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (standID != 0) {
            if (!newSystem && (Util.isClientHoldingShift() && !this.tooltip2.equals("")) || newSystem && (Util.isClientHoldingShift() && !tooltip.isEmpty()))
                tooltip.add(newSystem ? this.tooltip : new StringTextComponent(this.tooltip2));
            else if (!Util.isClientHoldingShift())
                tooltip.add(description);
                tooltip.add(new StringTextComponent("Hold\u00A7e Shift\u00A7f for moveset!"));
        } else
            tooltip.add(newSystem ? this.tooltip : new StringTextComponent(this.tooltip2));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.isAllowedOnBooks();
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public UseAction getUseAction(final ItemStack itemStack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(final ItemStack itemStack) {
        return 7200;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, PlayerEntity shooter) {
        return false;
    }

    @Override
    public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity entity) {
        return new StandArrowEntity(world, entity);
    }

}