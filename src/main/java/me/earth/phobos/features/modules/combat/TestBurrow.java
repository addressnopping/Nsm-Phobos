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
    private static TestBurrow INSTANCE = new TestBurrow();

    public Setting<JumpMode> jumpMode = this.register(new Setting <> ("Jump Mode", JumpMode.JUMP));
    public Setting<BurrowMode> burrowMode = this.register(new Setting <> ("Burrow Mode", BurrowMode.CPACKET));
    public Setting<Integer> attempts = this.register(new Setting <> ("Attempts", 1, 1, 10));

    public TestBurrow() {
        super("TestBurrow", "custom", Category.COMBAT, true, false, false);
    }

    public static TestBurrow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestBurrow();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
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

        if (this.jumpMode.getValue() == JumpMode.JUMP) {
            BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockObsidian.class));
            mc.player.jump();
            BurrowUtil.placeBlock(originalPos, EnumHand.MAIN_HAND, false, true, false);

            this.doBurrow();
        } else if (this.jumpMode.getValue() == JumpMode.FAKEJUMP) {
            BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockObsidian.class));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
            BurrowUtil.placeBlock(originalPos, EnumHand.MAIN_HAND, false, true, false);

            this.doBurrow();
        }
    }

    public void doBurrow() {
        mc.player.jump();
        if (!isBurrowed(player)) {
            for (int thing = 0; thing < this.attempts.getValue(); ++thing) {
                if (!isBurrowed(player)) {
                    if (this.burrowMode.getValue() == BurrowMode.CPACKET) {
                        try { Thread.sleep(100); } catch (InterruptedException ex) {
                            if (this.burrowMode.getValue() == BurrowMode.CPACKET) {
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY -= 0.4, mc.player.posZ, false));
                            }
                        }
                    } else if (this.burrowMode.getValue() == BurrowMode.POSY) {
                        player.posY -= 0.4;
                    }
                }
            }
        }
        BurrowUtil.switchToSlot(oldSlot);

        this.toggle();
    }

    private
    boolean isBurrowed ( EntityPlayerSP player ) {
        BlockPos blockPos = new BlockPos ( Math.floor ( player.posX ) , Math.floor ( player.posY + 0.2 ) , Math.floor ( player.posZ ) );
        return BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ENDER_CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.OBSIDIAN || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ANVIL;
    }

    public enum JumpMode {
        JUMP,
        FAKEJUMP
    }
    public enum BurrowMode {
        CPACKET,
        POSY
    }
}