package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.render.BurrowESP;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BurrowUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * @author PeterDev
 * @since 11/18/2021 at 4:57 p.m.
 */

public class TestBurrow extends Module {
    public Setting<Mode> mode = this.register(new Setting <Mode> ("Mode", Mode.JUMP));
    public Setting<Integer> attempts = this.register ( new Setting <> ( "Attempts", 1, 5, 10 ) );

    public TestBurrow() {
        super("TestBurrow", "custom", Category.COMBAT, true, false, false);
    }

    private BlockPos originalPos;
    int oldSlot;
    EntityPlayerSP player;

    @Override
    public void onEnable() {
        super.onEnable();

        originalPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        oldSlot = mc.player.inventory.currentItem;
        player = BurrowCC.mc.player;


        if (this.mode.getValue() == Mode.JUMP) {
            mc.player.jump();

            this.doBurrow();
        } else if (this.mode.getValue() == Mode.FAKEJUMP) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));

            this.doBurrow();
        }



    }

    public void doBurrow() {
        BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockObsidian.class));
        BurrowUtil.placeBlock(originalPos, EnumHand.MAIN_HAND, false, true, false);

        if (!isBurrowed(player)) {
            for (int thing = 0; thing < this.attempts.getValue(); ++thing) {
                if (!isBurrowed(player)) {
                    try { Thread.sleep(1000); } catch (InterruptedException ex) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.4, mc.player.posZ, false));
                    }
                }
            }
        }

        BurrowUtil.switchToSlot(oldSlot);
    }

    private
    boolean isBurrowed ( EntityPlayerSP player ) {
        BlockPos blockPos = new BlockPos ( Math.floor ( player.posX ) , Math.floor ( player.posY + 0.2 ) , Math.floor ( player.posZ ) );
        return BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ENDER_CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.OBSIDIAN || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ANVIL;
    }

    public enum Mode {
        JUMP,
        FAKEJUMP
    }
}