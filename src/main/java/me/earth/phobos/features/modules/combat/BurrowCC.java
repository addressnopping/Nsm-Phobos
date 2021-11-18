package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import net.minecraft.util.math.BlockPos;
import me.earth.phobos.Phobos;
import me.earth.phobos.util.Timer1;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.mixin.mixins.accessors.AccessorKeyBinding;

public class BurrowCC extends Module {
    public BurrowCC() {
        super("BurrowCC", "Burrow Bypass for CC(not 100%)", Category.COMBAT, true, false, false);
    }
    BlockPos position;
    int time;
    BlockPos pos;
    int stages;
    int delay, pdelay,stage,jumpdelay,toggledelay;
    boolean jump;
    Timer1 timer = new Timer1();

    @Override
    public void onEnable(){
        position = new BlockPos(mc.player.getPositionVector());
    }

    @Override
    public void onUpdate() {
        time = 0;
        pos = null;
        stages = 0;

        pdelay = 0;
        stage = 1;
        toggledelay = 0;
        jumpdelay = 0;
        timer.reset();
        jump = false;
        Phobos.TICK_TIMER = 1;
        position = null;
        delay = 0;
    }

    @Override
    public void onTick() {
        if (stage == 1) {
            delay++;
            ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(true);
            Phobos.TICK_TIMER = 30;
            if (delay >= 42) {
                stage = 2;
                delay = 0;
                Phobos.TICK_TIMER = 1;
                ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(false);
            }
        }
        if (stage == 2){
            Phobos.TICK_TIMER = 1;
            if (mc.player.onGround) ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(true);;
            BlockUtil.placeBlock1(position);
            pdelay++;
            if (pdelay >= 30){
                stage = 3;
                pdelay = 0;
                ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(false);
                Phobos.TICK_TIMER = 1;

            }
        }
        if (stage == 3){
            toggledelay++;
            Phobos.TICK_TIMER = 30;
            ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(true);
            if (toggledelay >= 25) {
                mc.player.motionY -= 0.4;
                Phobos.TICK_TIMER = 1;
                ((AccessorKeyBinding) mc.gameSettings.keyBindJump).setPressed(false);
                setEnabled(false);
            }
        }
    }
}