package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

/**
 * @author PeterDev
 * I used some phobos things tho
 */

public class FastWebMove extends Module {

    public final Setting<Double> webHorizontalFactor = this.register(new Setting<Double>("WebHSpeed", 2.0, 0.0, 100.0));
    public final Setting<Double> webVerticalFactor = this.register(new Setting<Double>("WebVSpeed", 2.0, 0.0, 100.0));

    public FastWebMove() {
        super("FastWebMove", "Makes you move faster on webs", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (Phobos.moduleManager.getModuleByClass(Flight.class).isDisabled() && Phobos.moduleManager.getModuleByClass(Phase.class).isDisabled() && mc.player.isInWeb) {
            NoSlowDown.mc.player.motionX *= this.webHorizontalFactor.getValue().doubleValue();
            NoSlowDown.mc.player.motionZ *= this.webHorizontalFactor.getValue().doubleValue();
            NoSlowDown.mc.player.motionY *= this.webVerticalFactor.getValue().doubleValue();
        }
    }
}
