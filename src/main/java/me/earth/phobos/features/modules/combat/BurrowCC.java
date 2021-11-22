package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.movement.Phase;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.Phobos;
import me.earth.phobos.util.Timer1;
import net.minecraft.util.math.BlockPos;

public class BurrowCC extends Module
{
    static Minecraft mc = Minecraft.getMinecraft();

    public BurrowCC() {
        super("CCBurrow", "burrow bypass for cc", Category.COMBAT, true, false, false);
    }

    public void onEnable() {
        BlockPos position = new BlockPos(mc.player.getPositionVector());
        final EntityPlayerSP player = BurrowCC.mc.player;

        if (BurrowCC.mc.player.onGround) {
            BurrowCC.mc.player.jump();
        }
        if (BurrowCC.mc.player.onGround) {
            BurrowCC.mc.player.jump();
        }

        BlockUtil.placeBlock1(position);

        if (BurrowCC.mc.player.onGround) {
            BurrowCC.mc.player.jump();
        }

        player.motionY -= 0.4;
        this.toggle();
    }
}
