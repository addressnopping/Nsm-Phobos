package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.Timer;
import io.netty.util.internal.ConcurrentSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NewPhase extends Module {
    public NewPhase() {
        super("NewPhase", "Under development", Category.MOVEMENT, true, false, false);
    }

    public Setting<Mode> mode = this.register(new Setting<Mode>("Mode", Mode.SEQUENTIAL));
    public Setting<Double> speedH = this.register(new Setting<Double>("HorizontalVelocity", 0.2, 0.0, 1.0));
    public Setting<Double> speedV = this.register(new Setting<Double>("VerticalVelocity", 1.0, 0.0, 5.0));
    public Setting<Integer> motionFactor = this.register(new Setting<Integer>("MotionFactor", 2, 1, 5));
    public Setting<Double> timertime = this.register(new Setting<Double>("Timer", 200.0, 0.0, 1000.0));
    public Setting<Double> loops = this.register(new Setting<Double>("Loops", 0.5, 0.0, 1.0));
    public Setting<Boolean> extraMotion = this.register(new Setting<Boolean>("ExtraMotion", false));
    public Setting<Integer> motionCount = this.register(new Setting<Integer>("ExtraMotionCountTicks", Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(10), v -> this.extraMotion.getValue()));
    public Setting<Boolean> bounds = this.register(new Setting<Boolean>("Bounds", true));
    public Setting<Boolean> instant = this.register(new Setting<Boolean>("Instant", true));
    public Setting<Boolean> bypass = this.register(new Setting<Boolean>("Bypass", false));
    public Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", false));
    public Setting<Boolean> antiKick = this.register(new Setting<Boolean>("AntiKick", true));
    public Setting<Integer> increaseTicks = this.register(new Setting<Integer>("IncreaseTicks", 10, 0, 15));
    public Setting<Double> teleportBackBypass = this.register(new Setting<Double>("TeleportBackBypass", 0.5, 0.0, 3.0));
    public Setting<Boolean> constrict = this.register(new Setting<Boolean>("Constrict", true));
    public Setting<Boolean> limit = this.register(new Setting<Boolean>("Limit", true));
    public Setting<Boolean> jitter = this.register(new Setting<Boolean>("Jitter", true));
    public Setting<Double> limitJitter = this.register(new Setting<Double>("JitterLimit", Double.valueOf(1.5), Double.valueOf(0.0), Double.valueOf(20.0), v -> this.jitter.getValue()));
    public Setting<Directions> directions = this.register(new Setting<Directions>("Directions", Directions.PRESERVE));
    public Setting<Bind> bind = this.register(new Setting<Bind>("LoopsBind:", new Bind(-1)));
    public Setting<Type> rotationType = this.register(new Setting<Type>("RotationType", Type.PACKET));
    public Setting<RotateMode> rotateMode = this.register(new Setting<RotateMode>("RotateMode", RotateMode.FULL));
    public Setting<Boolean> rotationSpoofer = this.register(new Setting<Boolean>("RotationSpoofer", Boolean.valueOf(false), v -> this.rotate.getValue()));
    public Setting<Boolean> extraPacket = this.register(new Setting<Boolean>("ExtraPacket", false));
    public Setting<Integer> extraPacketPackets = this.register(new Setting<Integer>("Packets", 5, 0, 20));
    private final Set<CPacketPlayer> packets = new ConcurrentSet();
    private final double[] positionSpoofer = new double[]{0.42, 0.75};
    private final double[] twoblockpositionSpoofer = new double[]{0.4, 0.75, 0.5, 0.41, 0.83, 1.16, 1.41, 1.57, 1.58, 1.42};
    private final double[] predictpositionSpoofer = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
    final double[] positionSpooferOffset = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
    private final double[] fourBlockpositionSpoofer = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43, 1.78, 1.63, 1.51, 1.9, 2.21, 2.45, 2.43, 2.78, 2.63, 2.51, 2.9, 3.21, 3.45, 3.43};
    private final double[] selectedSpoofedPositions = new double[0];
    private final Map<Integer, IDtime> teleportmap = new ConcurrentHashMap<Integer, IDtime>();
    private final Timer timer = new Timer();
    private int flightCounter = 0;
    private int teleportID = 0;
    private static NewPhase instance;

    public static NewPhase getInstance() {
        if (instance == null) {
            instance = new NewPhase();
        }
        return instance;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        double speed = mc.player.capabilities.getWalkSpeed();
        if (event.getStage() == 1) {
            return;
        }
        NewPhase.mc.player.setVelocity(0.0, 0.0, 0.0);
        boolean checkCollisionBoxes = this.checkHitBoxes();
        double d = NewPhase.mc.player.movementInput.jump && (checkCollisionBoxes || !EntityUtil.isMoving()) ? (!checkCollisionBoxes ? (this.resetCounter(10) ? -0.032 : 0.062) : 0.062) : (NewPhase.mc.player.movementInput.sneak ? -0.062 : (!checkCollisionBoxes ? (this.resetCounter(4) ? -0.04 : 0.0) : (speed = 0.0)));
        if (checkCollisionBoxes && EntityUtil.isMoving() && speed != 0.0) {
            double antiFactor = 2.5;
            speed /= antiFactor;
        }
        double[] strafing = this.getMotion(checkCollisionBoxes ? 0.031 : 0.26);
        double loops = this.bypass.getValue() != false ? this.loops.getValue() : 0.0;
        int i = 1;
        while ((double)i < loops + 1.0) {
            double extraFactor = 1.0;
            NewPhase.mc.player.motionX = strafing[0] * (double)i * extraFactor;
            NewPhase.mc.player.motionY = speed * (double)i;
            NewPhase.mc.player.motionZ = strafing[1] * (double)i * extraFactor;
            this.sendPackets(NewPhase.mc.player.motionX, NewPhase.mc.player.motionY, NewPhase.mc.player.motionZ);
            if (this.rotationSpoofer.getValue().booleanValue()) {
                mc.shutdown();
            }
            ++i;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && !this.packets.remove(event.getPacket())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PushEvent event) {
        if (event.getStage() == 1) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && !NewPhase.fullNullCheck()) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.getPacket();
            if (NewPhase.mc.player.isEntityAlive() && NewPhase.mc.world.isBlockLoaded(new BlockPos(NewPhase.mc.player.posX, NewPhase.mc.player.posY, NewPhase.mc.player.posZ), false) && !(NewPhase.mc.currentScreen instanceof GuiDownloadTerrain)) {
                this.teleportmap.remove(packet.getTeleportId());
            }
            this.teleportID = packet.getTeleportId();
        }
    }

    private boolean checkHitBoxes() {
        return !NewPhase.mc.world.getCollisionBoxes((Entity)NewPhase.mc.player, NewPhase.mc.player.getEntityBoundingBox().expand(-0.0, -0.1, -0.0)).isEmpty();
    }

    private boolean resetCounter(int counter) {
        if (++this.flightCounter >= counter) {
            this.flightCounter = 0;
            return true;
        }
        return false;
    }

    private double[] getMotion(double speed) {
        float moveForward = NewPhase.mc.player.movementInput.moveForward;
        float moveStrafe = NewPhase.mc.player.movementInput.moveStrafe;
        float rotationYaw = NewPhase.mc.player.prevRotationYaw + (NewPhase.mc.player.rotationYaw - NewPhase.mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += (float)(moveForward > 0.0f ? -45 : 45);
            } else if (moveStrafe < 0.0f) {
                rotationYaw += (float)(moveForward > 0.0f ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            } else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }
        double posX = (double)moveForward * speed * -Math.sin(Math.toRadians(rotationYaw)) + (double)moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        double posZ = (double)moveForward * speed * Math.cos(Math.toRadians(rotationYaw)) - (double)moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
        return new double[]{posX, posZ};
    }

    private void sendPackets(double x, double y, double z) {
        Vec3d vec = new Vec3d(x, y, z);
        Vec3d position = NewPhase.mc.player.getPositionVector().add(vec);
        Vec3d outOfBoundsVec = this.outOfBoundsVec(position);
        this.packetSender((CPacketPlayer)new CPacketPlayer.Position(position.x, position.y, position.z, NewPhase.mc.player.onGround));
        this.packetSender((CPacketPlayer)new CPacketPlayer.Position(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, NewPhase.mc.player.onGround));
        this.teleportPacket(position);
    }

    private void teleportPacket(Vec3d pos) {
        NewPhase.mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(++this.teleportID));
        this.teleportmap.put(this.teleportID, new IDtime(pos, new Timer()));
    }

    private Vec3d outOfBoundsVec(Vec3d position) {
        return position.add(0.0, 1337.0, 0.0);
    }

    private void packetSender(CPacketPlayer packet) {
        this.packets.add(packet);
        NewPhase.mc.player.connection.sendPacket((Packet)packet);
    }

    public static class IDtime {
        private final Vec3d pos;
        private final Timer timer;

        public IDtime(Vec3d pos, Timer timer) {
            this.pos = pos;
            this.timer = timer;
            this.timer.reset();
        }

        public Vec3d getPos() {
            return this.pos;
        }

        public Timer getTimer() {
            return this.timer;
        }
    }

    public static enum RotateMode {
        FULL,
        FULLSTRICT,
        SEMI,
        SEMISTRICT;

    }

    public static enum Type {
        PACKET,
        BYPASS,
        VANILLA,
        STRICT;

    }

    public static enum Directions {
        PRESERVE,
        UP,
        DOWN,
        SEMI,
        FULL,
        MULTIAXIS,
        DOUBLEAXIS,
        SINGLEAXIS;

    }

    public static enum Mode {
        SEQUENTIAL,
        DYNAMIC,
        VANILLA,
        NONPARALLEL,
        SERIAL,
        CONSECUTIVE,
        INCIDENTAL,
        STRICT,
        TWOBSTRICT,
        NCP;

    }
}
