package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL46;

import java.util.function.Predicate;

/*
Made by OLEPOSSU
*/

public class FacingSettings extends Module {
    public FacingSettings() {
        super(BlackOut.SETTINGS, "Facing", "Global facing settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //  Place Ranges
    public final Setting<Boolean> strictDir = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Doesn't place on faces which aren't in your direction.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> unblocked = sgGeneral.add(new BoolSetting.Builder()
        .name("Unblocked")
        .description("Doesn't place on faces that have block on them.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Place")
        .description("Can place blocks in air.")
        .defaultValue(false)
        .build()
    );

    public Direction[] getDirection(BlockPos pos) {
        if (pos == null) {return new Direction[]{null, null};}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new Direction[]{null, Direction.UP};
            } else {
                double cDist = -1;
                for (Direction dir : Direction.values()) {
                    if (!getBlock(pos.offset(dir)).equals(Blocks.AIR)) {
                        double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                        if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                            best = dir;
                            cDist = dist;
                        }
                    }
                }
            }
        }
        return new Direction[]{best, null};
    }
    public Direction[] getDirection(BlockPos pos, Predicate<BlockState> predicate) {
        if (pos == null) {return new Direction[]{null, null};}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new Direction[]{null, Direction.UP};
            } else {
                double cDist = -1;
                for (Direction dir : Direction.values()) {

                    // Test if there is block in the side and if predicate is valid
                    if (getBlock(pos.offset(dir)).equals(Blocks.AIR) || !predicate.test(mc.world.getBlockState(pos.offset(dir)))) {continue;}

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite())) {continue;}

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return new Direction[]{best, null};
    }

    public Direction getPlaceOnDirection(BlockPos pos) {
        if (!strictDir.get()) {return Direction.UP;}
        if (pos == null) {return null;}
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            double cDist = -1;
            for (Direction dir : Direction.values()) {
                // Unblocked check (mostly for autocrystal placement facings)
                if (unblocked.get() && !getBlock(pos.offset(dir)).equals(Blocks.AIR)) {continue;}

                // Strict dir check (checks if face isnt on opposite side of the block to player)
                if (strictDir.get() && !OLEPOSSUtils.strictDir(pos, dir)) {continue;}

                // Only accepts if closer than last accepted direction
                double dist = dist(pos, dir);
                if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                    best = dir;
                    cDist = dist;
                }
            }
        }
        return best;
    }

    double dist(BlockPos pos, Direction dir) {
        if (mc.player == null) {return 0;}
        Vec3d vec = new Vec3d(pos.getX() + dir.getOffsetX() / 2f, pos.getY() + dir.getOffsetY() / 2f, pos.getZ() + dir.getOffsetZ() / 2f);
        Vec3d dist = mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);
        return Math.sqrt(dist.x * dist.x + dist.y * dist.y + dist.z * dist.z);
    }

    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}
