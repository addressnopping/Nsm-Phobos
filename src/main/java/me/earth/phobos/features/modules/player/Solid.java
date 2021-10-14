package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.SolidEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Solid
        extends Module {
    public static AxisAlignedBB offset = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9999, 1.0);
    private static Solid INSTANCE = new Solid();
    public Setting<Mode> mode = this.register(new Setting<Mode>("Mode", Mode.NORMAL));
    public Setting<Boolean> cancelVehicle = this.register(new Setting<Boolean>("NoVehicle", false));
    public Setting<EventMode> eventMode = this.register(new Setting<Object>("Jump", EventMode.PRE, v -> this.mode.getValue() == Mode.TRAMPOLINE));
    public Setting<Boolean> fall = this.register(new Setting<Object>("NoFall", Boolean.valueOf(false), v -> this.mode.getValue() == Mode.TRAMPOLINE));
    private boolean grounded = false;

    public Solid() {
        super("Solid", "Allows you to walk on water", Module.Category.PLAYER, true, false, false);
        INSTANCE = this;
    }

    public static Solid getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Solid();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void updateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (Solid.fullNullCheck() || Freecam.getInstance().isOn()) {
            return;
        }
        if (!(event.getStage() != 0 || this.mode.getValue() != Mode.BOUNCE && this.mode.getValue() != Mode.VANILLA && this.mode.getValue() != Mode.NORMAL || Solid.mc.player.isSneaking() || Solid.mc.player.noClip || Solid.mc.gameSettings.keyBindJump.isKeyDown() || !EntityUtil.isInLiquid())) {
            Solid.mc.player.motionY = 0.1f;
        }
        if (event.getStage() == 0 && this.mode.getValue() == Mode.TRAMPOLINE && (this.eventMode.getValue() == EventMode.ALL || this.eventMode.getValue() == EventMode.PRE)) {
            this.doTrampoline();
        } else if (event.getStage() == 1 && this.mode.getValue() == Mode.TRAMPOLINE && (this.eventMode.getValue() == EventMode.ALL || this.eventMode.getValue() == EventMode.POST)) {
            this.doTrampoline();
        }
    }

    @SubscribeEvent
    public void sendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && Freecam.getInstance().isOff() && (this.mode.getValue() == Mode.BOUNCE || this.mode.getValue() == Mode.NORMAL) && Solid.mc.player.getRidingEntity() == null && !Solid.mc.gameSettings.keyBindJump.isKeyDown()) {
            CPacketPlayer packet = event.getPacket();
            if (!EntityUtil.isInLiquid() && EntityUtil.isOnLiquid(0.05f) && EntityUtil.checkCollide() && Solid.mc.player.ticksExisted % 3 == 0) {
                packet.y -= 0.05f;
            }
        }
    }

    @SubscribeEvent
    public void onLiquidCollision(SolidEvent event) {
        if (Solid.fullNullCheck() || Freecam.getInstance().isOn()) {
            return;
        }
        if (event.getStage() == 0 && (this.mode.getValue() == Mode.BOUNCE || this.mode.getValue() == Mode.VANILLA || this.mode.getValue() == Mode.NORMAL) && Solid.mc.world != null && Solid.mc.player != null && EntityUtil.checkCollide() && !(Solid.mc.player.motionY >= (double) 0.1f) && (double) event.getPos().getY() < Solid.mc.player.posY - (double) 0.05f) {
            if (Solid.mc.player.getRidingEntity() != null) {
                event.setBoundingBox(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.95f, 1.0));
            } else {
                event.setBoundingBox(Block.FULL_BLOCK_AABB);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (this.cancelVehicle.getValue().booleanValue() && event.getPacket() instanceof SPacketMoveVehicle) {
            event.setCanceled(true);
        }
    }

    @Override
    public String getDisplayInfo() {
        if (this.mode.getValue() == Mode.NORMAL) {
            return null;
        }
        return this.mode.currentEnumName();
    }

    private void doTrampoline() {
        if (Solid.mc.player.isSneaking()) {
            return;
        }
        if (EntityUtil.isAboveLiquid(Solid.mc.player) && !Solid.mc.player.isSneaking() && !Solid.mc.gameSettings.keyBindJump.pressed) {
            Solid.mc.player.motionY = 0.1;
            return;
        }
        if (Solid.mc.player.onGround || Solid.mc.player.isOnLadder()) {
            this.grounded = false;
        }
        if (Solid.mc.player.motionY > 0.0) {
            if (Solid.mc.player.motionY < 0.03 && this.grounded) {
                Solid.mc.player.motionY += 0.06713;
            } else if (Solid.mc.player.motionY <= 0.05 && this.grounded) {
                Solid.mc.player.motionY *= 1.20000000999;
                Solid.mc.player.motionY += 0.06;
            } else if (Solid.mc.player.motionY <= 0.08 && this.grounded) {
                Solid.mc.player.motionY *= 1.20000003;
                Solid.mc.player.motionY += 0.055;
            } else if (Solid.mc.player.motionY <= 0.112 && this.grounded) {
                Solid.mc.player.motionY += 0.0535;
            } else if (this.grounded) {
                Solid.mc.player.motionY *= 1.000000000002;
                Solid.mc.player.motionY += 0.0517;
            }
        }
        if (this.grounded && Solid.mc.player.motionY < 0.0 && Solid.mc.player.motionY > -0.3) {
            Solid.mc.player.motionY += 0.045835;
        }
        if (!this.fall.getValue().booleanValue()) {
            Solid.mc.player.fallDistance = 0.0f;
        }
        if (!EntityUtil.checkForLiquid(Solid.mc.player, true)) {
            return;
        }
        if (EntityUtil.checkForLiquid(Solid.mc.player, true)) {
            Solid.mc.player.motionY = 0.5;
        }
        this.grounded = true;
    }

    public enum Mode {
        TRAMPOLINE,
        BOUNCE,
        VANILLA,
        NORMAL

    }

    public enum EventMode {
        PRE,
        POST,
        ALL

    }
}

