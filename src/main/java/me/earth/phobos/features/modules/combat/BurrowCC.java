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
    public Setting<Integer> ticks = this.register(new Setting<Integer>("Ticks", 10, 5, 60));
    public Setting<Integer> toggleDelays = this.register(new Setting<Integer>("Toggle Delay", 10, 5, 60));
    public Setting<Integer> oneDelays = this.register(new Setting<Integer>("One Delay", 10, 5, 60));
    public Setting<Integer> placeDelay = this.register(new Setting<Integer>("Second Delay", 10, 5, 60));
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

    @Override
    public void onEnable() {
        this.position = new BlockPos(BurrowCC.mc.player.getPositionVector());
    }

    @Override
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
            Phobos.TICK_TIMER = (float)ticks.getValue();
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
            Phobos.TICK_TIMER = (float)ticks.getValue();
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
        SECONDBYPASS
    }
}
