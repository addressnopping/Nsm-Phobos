package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author PeterDev
 * @since 10/13/2021 at 6:46 p.m.
 * kool
 */
public class NewPhase extends Module {
    private final Setting<Boolean> noClip = this.register(new Setting<Boolean>("NoClip", false));

    public NewPhase() {
        super("CustomPhase", "Under development", Category.MOVEMENT, true, true, false);
    }

    @Override
    public void onEnable() {
        if (this.noClip.getValue()) {
            mc.player.noClip = true;
        }
    }
    @Override
    public void onDisable() {
        if (this.noClip.getValue()){
            mc.player.noClip = false;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        event.setX(Phase.mc.player.motionX);
        event.setY(Phase.mc.player.motionY);
        event.setZ(Phase.mc.player.motionZ);
        if (this.noClip.getValue()) {
            Phase.mc.player.noClip = true;
        }
    }
}
