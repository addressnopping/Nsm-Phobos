package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.Phobos;
import me.earth.phobos.util.Timer1;
import net.minecraft.util.math.BlockPos;

public class BurrowCC extends Module
{
    Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Mode> mode = this.register(new Setting<Mode>("Bypass Mode", Mode.PIGBYPASS));
    private final Setting<Integer> ticks = this.register(new Setting<Integer>("Ticks", 10, 10, 60));
    private final Setting<Integer> toggleDelays = this.register(new Setting<Integer>("Toggle Delay", 10, 10, 60));
    private final Setting<Integer> oneDelays = this.register(new Setting<Integer>("One Delay", 10, 10, 60));
    private final Setting<Integer> placeDelay = this.register(new Setting<Integer>("Second Delay", 10, 10, 60));

    BlockPos position;
    int delay;
    int pdelay;
    int stage;
    int jumpdelay;
    int toggledelay;
    boolean jump;
    Timer1 timer;

    public BurrowCC() {
        super("CCBurrow", "burrow bypass for cc", Category.COMBAT, true, false, false);
        this.mode = Mode.PIGBYPASS;
        this.ticks = 50;
        this.toggleDelays = 20;
        this.oneDelays = 42;
        this.placeDelay = 30;
        this.timer = new Timer1();
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
        if (this.position != null && this.mode == Mode.PIGBYPASS) {
            this.firstmethod();
        }
    }

    public void firstmethod() {
        if (this.stage == 1) {
            ++this.delay;
            if (BurrowCC.mc.player.onGround) {
                BurrowCC.mc.player.jump();
            }
            Phobos.TICK_TIMER = (float)this.ticks;
            if (this.delay >= this.oneDelays) {
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
            Phobos.TICK_TIMER = (float)this.ticks.getValue();
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
