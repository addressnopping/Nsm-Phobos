package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author PeterDev
 * Using somethings from Phobos Solid module btw
 */

public class WebSolid extends Module {

    public WebSolid() {
        super("WebSolid", "Solid module but for webs", Category.MOVEMENT, true, false, false);
    }

    @SubscribeEvent
    public void sendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && Freecam.getInstance().isOff() && (mc.player.getRidingEntity() == null && !mc.gameSettings.keyBindJump.isKeyDown())) {
            CPacketPlayer packet = event.getPacket();
            if (!EntityUtil.isInWeb() && EntityUtil.isOnWeb(0.05f) && EntityUtil.checkCollide() && mc.player.ticksExisted % 3 == 0) {
                packet.y -= 0.05f;
            }
        }
    }
}
