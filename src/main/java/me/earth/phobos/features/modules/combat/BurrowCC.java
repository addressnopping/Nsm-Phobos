package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.Phobos;
import me.earth.phobos.util.Timer1;
import net.minecraft.util.math.BlockPos;

public class BurrowCC extends Module
{
    private final Setting <Mode> mode = this.register (new Setting <> ("Bypass Method" , Mode.PIGBYPASS));
    public Setting<Float> ticks = this.register(new Setting<Float>("Ticks", Float.valueOf(10.0f), Float.valueOf(10.0f), Float.valueOf(60.0f)));
    public Setting<Float> toggleDelays = this.register(new Setting<Float>("Toggle Delay", Float.valueOf(10.0f), Float.valueOf(10.0f), Float.valueOf(60.0f)));
    public Setting<Float> oneDelays = this.register(new Setting<Float>("One Delay", Float.valueOf(10.0f), Float.valueOf(10.0f), Float.valueOf(60.0f)));
    public Setting<Float> placeDelay = this.register(new Setting<Float>("Second Delay", Float.valueOf(10.0f), Float.valueOf(10.0f), Float.valueOf(60.0f)));
    BlockPos position;
    int delay;
    int pdelay;
    int stage;
    int jumpdelay;
    int toggledelay;
    boolean jump;
    Timer1 timer;

    public BurrowCC() {
        super("BurrowCC", "burrow for cc", Category.COMBAT, true, false, false);
    }


    public void onEnable() {
        this.position = new BlockPos(BurrowCC.mc.player.getPositionVector());
    }

    public void onToggle() {
        this.pdelay = 0;
        this.stage = 1;
        this.toggledelay = 0;
        this.jumpdelay = 0;
        this.timer.reset();
        this.jump = false;
        Phobos.TICK_TIMER = 1.0f;
        this.position = null;
        this.delay = 0;
    }

    @Override
    public void onTick() {
        if (this.position != null && mode.getValue() == Mode.PIGBYPASS) {
            this.firstmethod();
        }
    }

    public void firstmethod() {
        if (this.stage == 1) {
            ++this.delay;
            if (BurrowCC.mc.player.onGround) {
                BurrowCC.mc.player.jump();
            }
            Phobos.TICK_TIMER = this.ticks.getValue();
            if (this.delay >= this.oneDelays.getValue()) {
                this.stage = 2;
                this.delay = 0;
                Phobos.TICK_TIMER = 1.0f;
                this.jump = true;
            }
        }
        if (this.stage == 2) {
            Phobos.TICK_TIMER = 1.0f;
            if (BurrowCC.mc.player.onGround) {
                BurrowCC.mc.player.jump();
            }
            BlockUtil.placeBlock1(this.position);
            ++this.pdelay;
            if (this.pdelay >= this.placeDelay.getValue()) {
                this.stage = 3;
                this.pdelay = 0;
                Phobos.TICK_TIMER = 1.0f;
            }
        }
        if (this.stage == 3) {
            ++this.toggledelay;
            Phobos.TICK_TIMER = this.ticks.getValue();
            if (BurrowCC.mc.player.onGround) {
                BurrowCC.mc.player.jump();
            }
            if (this.toggledelay >= this.toggleDelays.getValue()) {
                final EntityPlayerSP player = BurrowCC.mc.player;
                player.motionY -= 0.4;
                Phobos.TICK_TIMER = 1.0f;
                this.toggle();
            }
        }
    }

    public enum Mode
    {
        PIGBYPASS,
        SECONDBYPASS;
    }
}
