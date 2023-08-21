package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class StickyFingersEntity extends AbstractStandEntity {
    public LivingEntity disguiseEntity;
    int zipLineTicks = 0;
    boolean layingZipLine = false;
    Vector3d zipLineStartPoint;
    Vector3d zipLinePoint;
    ArrayList<ZipLinePoint> zipLinePoints = new ArrayList<>();
    ArrayList<ZippedBlock> zippedBlocks = new ArrayList<>();
    ArrayList<ZippedBlock> adjacentZippedBlocks = new ArrayList<>();
    Vector3d initialLookVec;
    Vector3d increment;
    static final double ZIP_INCREMENT = 0.3;


    enum ZipLineDirection {
        FORWARD,
        BACKWARD,
        UP,
        DOWN
    }
    ZipLineDirection zipLineDirection;

    public StickyFingersEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    public void toggleZippedBlock() {
        if (getMaster() == null || world.isRemote()) return;
            //Raytracing but awesome
            //Just kidding, it's never awesome
            Vector3d lookVector = master.getLookVec().normalize();
            boolean hasTriggered = false;
            for(int i = 0; i < 40 && !hasTriggered; i++){
                double distance = 0.2 * i;
                double posX = master.getPosX() + (lookVector.x * distance);
                double posY = master.getPosY() + master.getEyeHeight() + (lookVector.y * distance);
                double posZ = master.getPosZ() + (lookVector.z * distance);
                if(i % 3 == 0) {
                    Util.spawnParticle(this, 10, posX, posY, posZ, 0.6, 0.6, 0.6, 1);
                }

                for(ZippedBlock zipped : zippedBlocks){
                    boolean xBound = posX >= zipped.position.getX() && posX <= zipped.position.getX() + 1;
                    boolean yBound = posY >= zipped.position.getY() && posY <= zipped.position.getY() + 1;
                    boolean zBound = posZ >= zipped.position.getZ() && posZ <= zipped.position.getZ() + 1;
                    if (xBound && yBound && zBound) {
                        hasTriggered = true;
                        adjacentZippedBlocks.clear();
                        adjacentZippedBlocks.add(zipped);
                        addAdjacentZippedBlocks(zipped.position);

                        if (master.isSneaking()) {
                            for(ZippedBlock block: adjacentZippedBlocks){
                                if(!block.toggled){
                                    block.switchState();
                                }
                            }
                            zippedBlocks.removeAll(adjacentZippedBlocks);
                        }else{
                            for (ZippedBlock zippedBlock : adjacentZippedBlocks) {
                                zippedBlock.switchState();
                            }
                        }
                    }
                }
            }
        }

    public void addZippedBlock() {
        if (getMaster() == null || world.isRemote || disguiseEntity != null) return;
        BlockPos pos = lookingAt(master, false);
        ZippedBlock block = null;
        if(pos != null) {
            for (ZippedBlock zippedBlock : zippedBlocks) {
                if (zippedBlock.position.getX() == pos.getX() && zippedBlock.position.getY() == pos.getY() && zippedBlock.position.getZ() == pos.getZ()) {
                    block = zippedBlock;
                }
            }
        }

        if(pos != null && block == null && zippedBlocks.size() < DiamondIsUncraftableConfig.COMMON.maxStickyFingersBlocks.get()) {
            if(world.getBlockState(pos).getBlockHardness(world, pos) != -1f) {
                zippedBlocks.add(new ZippedBlock(this, 1, pos, true, world.getBlockState(pos), world));
                double min = 0.1;
                Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() - 0.1, pos.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() - 0.1, min, min, min, 1);
                Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1.1, min, min, min, 1);
                Util.spawnParticle(this, 11, pos.getX() - 0.1, pos.getY() + 0.5, pos.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(this, 11, pos.getX() + 1.1, pos.getY() + 0.5, pos.getZ() + 0.5, min, min, min, 1);
            }
        }
    }

    public void zipLine() {
        if (getMaster() == null || world.isRemote || disguiseEntity != null) return;

        if(!layingZipLine && master.isOnGround()) {
            zipLineStartPoint = master.getPositionVec();
            zipLinePoint = master.getPositionVec();
            initialLookVec = Util.rotationVectorIgnoreY(master).normalize();
            increment = initialLookVec.scale(ZIP_INCREMENT);
            zipLineDirection = ZipLineDirection.FORWARD;
            layingZipLine = true;

        }else{
            layingZipLine = false;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (layingZipLine) {

                if(master.getPositionVec().distanceTo(zipLineStartPoint) > 1){
                    zipLinePoints.clear();
                    layingZipLine = false;
                }else{

                    Vector3d nextPoint;
                    boolean gotNextPoint = false;
                    int passes = 0;
                    zipLineTicks++;
                    while(!gotNextPoint && passes < 4) {
                        passes++;
                        if (zipLineDirection == ZipLineDirection.FORWARD) {
                            increment = initialLookVec.scale(ZIP_INCREMENT);
                            nextPoint = zipLinePoint.add(increment);

                            if (Util.isPointAtVecSolid(world, nextPoint)) {
                                zipLineDirection = ZipLineDirection.UP;
                                nextPoint = zipLinePoint;
                            } else if (!Util.isPointAtVecSolid(world, new Vector3d(nextPoint.getX(), nextPoint.getY() - ZIP_INCREMENT, nextPoint.getZ()))) {
                                zipLineDirection = ZipLineDirection.DOWN;
                            } else {
                                nextPoint = zipLinePoint.add(increment);
                                gotNextPoint = true;
                            }
                        } else if (zipLineDirection == ZipLineDirection.UP) {
                            increment = new Vector3d(0, 0.2, 0);
                            nextPoint = zipLinePoint.add(increment);
                            if (Util.isPointAtVecSolid(world, nextPoint)) {
                                zipLineDirection = ZipLineDirection.BACKWARD;
                                nextPoint = zipLinePoint;
                            } else if (!Util.isPointAtVecSolid(world, nextPoint.add(initialLookVec.scale(ZIP_INCREMENT)))) {
                                zipLineDirection = ZipLineDirection.FORWARD;
                                nextPoint = zipLinePoint.add(increment);
                            } else {
                                nextPoint = zipLinePoint.add(increment);
                                gotNextPoint = true;
                            }
                        } else if (zipLineDirection == ZipLineDirection.BACKWARD) {
                            increment = initialLookVec.scale(ZIP_INCREMENT).inverse();
                            nextPoint = zipLinePoint.add(increment);
                            if (Util.isPointAtVecSolid(world, nextPoint)) {
                                zipLineDirection = ZipLineDirection.DOWN;
                                nextPoint = zipLinePoint;
                            } else if (!Util.isPointAtVecSolid(world, nextPoint.add(new Vector3d(0, ZIP_INCREMENT, 0)))) {
                                zipLineDirection = ZipLineDirection.UP;
                                nextPoint = zipLinePoint.add(increment);
                            } else {
                                nextPoint = zipLinePoint.add(increment);
                                gotNextPoint = true;
                            }
                        } else {
                            increment = new Vector3d(0, -ZIP_INCREMENT, 0);
                            nextPoint = zipLinePoint.add(increment);
                            if (Util.isPointAtVecSolid(world, nextPoint)) {
                                zipLineDirection = ZipLineDirection.FORWARD;
                                nextPoint = zipLinePoint;
                            } else if (!Util.isPointAtVecSolid(world, nextPoint.add(initialLookVec.scale(ZIP_INCREMENT).inverse()))) {
                                zipLineDirection = ZipLineDirection.BACKWARD;
                                nextPoint = zipLinePoint.add(increment);
                            } else {
                                nextPoint = zipLinePoint.add(increment);
                                gotNextPoint = true;
                            }
                        }
                        zipLinePoint = nextPoint;
                        zipLinePoints.add(new ZipLinePoint(nextPoint.x, nextPoint.y, nextPoint.z, zipLineDirection == ZipLineDirection.BACKWARD));
                    }
                }
                if(zipLineTicks >= 80){
                    layingZipLine = false;
                }

            } else {
                zipLineTicks = 0;
                if (zipLinePoints.size() > 0) {
                    double yShift = zipLinePoints.get(0).isCeilingLine ? -2 : 0;
                    master.setPositionAndUpdate(zipLinePoints.get(0).x, zipLinePoints.get(0).y + yShift, zipLinePoints.get(0).z);
                    for (int i = 0; i < 4; i++) {
                        if (zipLinePoints.size() > 0) {
                            zipLinePoints.remove(0);
                        }
                    }
                }
            }

            for (ZipLinePoint point : zipLinePoints) {
                Util.spawnParticle(this, 10, point.x, point.y, point.z, 0.1, 0.1, 0.1, 1);
            }

            for (ZippedBlock block : zippedBlocks) {
                BlockPos pos = block.position;
                //Util.spawnParticle(this,11, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5, 1.5, 1.5, 2);
                double min = 0.1;
                if (block.toggled) {
                    if (this.ticksExisted % 10 == 0) {
                        Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() - 0.1, pos.getZ() + 0.5, min, min, min, 1);
                        Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, min, min, min, 1);
                        Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() - 0.1, min, min, min, 1);
                        Util.spawnParticle(this, 11, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1.1, min, min, min, 1);
                        Util.spawnParticle(this, 11, pos.getX() - 0.1, pos.getY() + 0.5, pos.getZ() + 0.5, min, min, min, 1);
                        Util.spawnParticle(this, 11, pos.getX() + 1.1, pos.getY() + 0.5, pos.getZ() + 0.5, min, min, min, 1);
                    }
                } else {
                    Util.spawnParticle(this, 12, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.75, 0.75, 0.75, 1);
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(props -> props.setNoClip(false));
        master.setInvulnerable(false);

        for(ZippedBlock zipped : zippedBlocks){
            if(!zipped.toggled){
                zipped.switchState();
            }
        }
        zippedBlocks.clear();
    }

    static class ZipLinePoint{
        double x;
        double y;
        double z;
        boolean isCeilingLine = false;

        public ZipLinePoint(double xIn, double yIn, double zIn){
            x = xIn;
            y = yIn;
            z = zIn;
        }

        public ZipLinePoint(double xIn, double yIn, double zIn, boolean isCeilingLineIn){
            x = xIn;
            y = yIn;
            z = zIn;
            isCeilingLine = isCeilingLineIn;
        }
    }

    private BlockPos lookingAt(PlayerEntity player, boolean isFluid){
        ArrayList<String> list = new ArrayList<String>();

        RayTraceResult block =  player.pick(5.0D, 0.0F, isFluid);

        if(block.getType() == RayTraceResult.Type.BLOCK)
        {
            return ((BlockRayTraceResult)block).getPos();
        }else{
            return null;
        }
    }

    private void addAdjacentZippedBlocks(BlockPos pos){
        //The relative x y and z coordinates of the 6 blocks that make contact with the original block.
        //The answer to "Is this the best way to do this?" and "Do I want to spend more time on recursive processes?" is the same.
        int[][] displacementArray= {
                {0,1,0},
                {0,-1,0},
                {1,0,0},
                {-1,0,0},
                {0,0,1},
                {0,0,-1}
        };
        for(int[] array : displacementArray) {
            BlockPos checkPos = new BlockPos(pos.getX() + array[0], pos.getY() + array[1], pos.getZ() + array[2]);
            for(ZippedBlock zippedBlock : zippedBlocks){
                BlockPos block = zippedBlock.position;

                boolean adjacentZippedBlockContainsBlock = false;

                for (ZippedBlock zippedBlock1 : adjacentZippedBlocks) {
                    if (zippedBlock1.position.getX() == checkPos.getX() && zippedBlock1.position.getY() == checkPos.getY() && zippedBlock1.position.getZ() == checkPos.getZ()) {
                        adjacentZippedBlockContainsBlock = true;
                    }
                }

                if(block.getX() == checkPos.getX() && block.getY() == checkPos.getY() && block.getZ() == checkPos.getZ() && !adjacentZippedBlockContainsBlock){
                    adjacentZippedBlocks.add(zippedBlock);
                    addAdjacentZippedBlocks(block);
                }
            }
        }
    }


    static class ZippedBlock{
        World world;
        public BlockPos position;
        public boolean toggled;
        public BlockState blockState;
        public StickyFingersEntity stand;
        int index;

        public ZippedBlock(StickyFingersEntity standIn, int indexIn, BlockPos positionIn, boolean toggledIn, BlockState blockTypeIn, World worldIn){
            position = positionIn;
            toggled = toggledIn;
            blockState = blockTypeIn;
            world = worldIn;
            stand = standIn;
            index = indexIn;
        }

        public void switchState(){
            if(toggled){
                blockState = world.getBlockState(position);
                world.removeBlock(position, false);
            }else{
                world.setBlockState(position, blockState);
                double min = 0.1;
                Util.spawnParticle(stand, 11, position.getX() + 0.5, position.getY() - 0.1, position.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(stand, 11, position.getX() + 0.5, position.getY() + 1.1, position.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(stand, 11, position.getX() + 0.5, position.getY() + 0.5, position.getZ() - 0.1, min, min, min, 1);
                Util.spawnParticle(stand, 11, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 1.1, min, min, min, 1);
                Util.spawnParticle(stand, 11, position.getX() - 0.1, position.getY() + 0.5, position.getZ() + 0.5, min, min, min, 1);
                Util.spawnParticle(stand, 11, position.getX() + 1.1, position.getY() + 0.5, position.getZ() + 0.5, min, min, min, 1);
            }
            toggled = !toggled;
        }

    }

    private static boolean zippedBlocksOccupySameSpace(ZippedBlock zipped1, ZippedBlock zipped2){
        return zipped1.position.getX() == zipped2.position.getX() && zipped1.position.getY() == zipped2.position.getY() && zipped1.position.getZ() == zipped2.position.getZ();
    }

}
