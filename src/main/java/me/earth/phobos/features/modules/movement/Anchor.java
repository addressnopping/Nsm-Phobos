package me.earth.phobos.features.modules.movement;

import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.HoleUtil;
import me.earth.phobos.features.modules.Module;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * @author Peterrr__ and gamesense devs
 * Skidded by Peterrr__
 */
public class Anchor
        extends Module {
    private final Setting<Boolean> guarantee = this.register(new Setting<Boolean>("Guarantee Hole", true));
    private final Setting<Integer> activateHeight = this.register(new Setting<Integer>("Activate Height", 2, 1, 5));
    private BlockPos playerPos;

    public Anchor() {
        super("Anchor", "Pulls you to a safe hole", Module.Category.MOVEMENT, true, false, false);
    }

    public void onUpdate() {
        if (mc.player == null) {
            return;
        }

        if (mc.player.posY < 0) {
            return;
        }

        double blockX = Math.floor(mc.player.posX);
        double blockZ = Math.floor(mc.player.posZ);

        double offsetX = Math.abs(mc.player.posX - blockX);
        double offsetZ = Math.abs(mc.player.posZ - blockZ);

        if (guarantee.getValue() && (offsetX < 0.3f || offsetX > 0.7f || offsetZ < 0.3f || offsetZ > 0.7f)) {
            return;
        }

        playerPos = new BlockPos(blockX, mc.player.posY, blockZ);

        if (mc.world.getBlockState(playerPos).getBlock() != Blocks.AIR) {
            return;
        }

        BlockPos currentBlock = playerPos.down();
        for (int i = 0; i < activateHeight.getValue(); i++) {
            currentBlock = currentBlock.down();
            if (mc.world.getBlockState(currentBlock).getBlock() != Blocks.AIR) {
                HashMap<HoleUtil.BlockOffset, HoleUtil.BlockSafety> sides = HoleUtil.getUnsafeSides(currentBlock.up());
                sides.entrySet().removeIf(entry -> entry.getValue() == HoleUtil.BlockSafety.RESISTANT);
                if (sides.size() == 0) {
                    mc.player.motionX = 0f;
                    mc.player.motionZ = 0f;
                }
            }
        }
    }
}