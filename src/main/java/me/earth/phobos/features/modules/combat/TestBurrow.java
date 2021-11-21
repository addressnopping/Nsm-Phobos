package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.render.BurrowESP;
import me.earth.phobos.util.BurrowUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * @author PeterDev
 * @since 11/18/2021 at 4:57 p.m.
 */

public class TestBurrow extends Module {
    public TestBurrow() {
        super("TestBurrow", "custom", Category.COMBAT, true, true, false);
    }

    private BlockPos originalPos;
    private EntityPlayer entityPlayer;
    private int oldSlot = -1;
    private int thing = 10;

    @Override
    public void onEnable() {
        super.onEnable();

        oldSlot = mc.player.inventory.currentItem;
        originalPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockObsidian.class));

        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));

        BurrowUtil.placeBlock(originalPos, EnumHand.MAIN_HAND, false, true, false);

        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.16610926093821D, mc.player.posZ, false));

        if (!isBurrowed(entityPlayer)) {
            if (thing<10) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.16610926093821D, mc.player.posZ, false));
            }
        }
        if (thing>=10) {
            BurrowUtil.switchToSlot(oldSlot);
            toggle();
        }
    }

    private
    boolean isBurrowed ( EntityPlayer entityPlayer ) {
        BlockPos blockPos = new BlockPos ( Math.floor ( entityPlayer.posX ) , Math.floor ( entityPlayer.posY + 0.2 ) , Math.floor ( entityPlayer.posZ ) );
        return BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ENDER_CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.OBSIDIAN || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.CHEST || BurrowESP.mc.world.getBlockState ( blockPos ).getBlock ( ) == Blocks.ANVIL;
    }
}
