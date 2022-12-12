package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PlayerUpdateEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.mixin.mixins.accessors.ISPacketPlayerPosLook;
import me.earth.phobos.util.PlayerUtils;
import me.earth.phobos.util.TimeVec3d;
import me.earth.phobos.util.Timer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketFly
        extends Module {
    private Setting<Type> type = this.register(new Setting<Type>("Type", Type.FAST));
    private Setting<Mode> packetMode = this.register(new Setting<Mode>("PacketMode", Mode.UP));
    private Setting<Phase> phase = this.register(new Setting<Phase>("Phase", Phase.NCP));
    private Setting<AntiKick> antiKickMode = this.register(new Setting<AntiKick>("AntiKick", AntiKick.NORMAL));
    private Setting<Limit> limit = this.register(new Setting<Limit>("Limit", Limit.NONE));
    public Setting<Float> motion = this.register(new Setting<Object>("Distance", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(20.0f), v -> this.type.getValue() == Type.FACTOR));
    public Setting<Float> speed = this.register(new Setting<Float>("Speed", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(2.0f)));
    public Setting<Float> factor = this.register(new Setting<Object>("Factor", Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), v -> this.type.getValue() == Type.FACTOR || this.type.getValue() == Type.DESYNC));
    public Setting<Boolean> boost = this.register(new Setting<Boolean>("Boost", false));
    public Setting<Boolean> jitter = this.register(new Setting<Boolean>("Jitter", false));
    public Setting<Boolean> constrict = this.register(new Setting<Boolean>("Constrict", false));
    public Setting<Boolean> noPhaseSlow = this.register(new Setting<Boolean>("NoPhaseSlow", false));
    public Setting<Boolean> multiAxis = this.register(new Setting<Boolean>("MultiAxis", false));
    public Setting<Boolean> bounds = this.register(new Setting<Boolean>("Bounds", false));
    public Setting<Boolean> strict = this.register(new Setting<Boolean>("Strict", true));
    private int teleportId;
    private CPacketPlayer.Position startingOutOfBoundsPos;
    private ArrayList<CPacketPlayer> packets = new ArrayList();
    private Map<Integer, TimeVec3d> posLooks = new ConcurrentHashMap<Integer, TimeVec3d>();
    private int antiKickTicks = 0;
    private int vDelay = 0;
    private int hDelay = 0;
    private boolean limitStrict = false;
    private int limitTicks = 0;
    private int jitterTicks = 0;
    private boolean oddJitter = false;
    double speedX = 0.0;
    double speedY = 0.0;
    double speedZ = 0.0;
    private float postYaw = -400.0f;
    private float postPitch = -400.0f;
    private int factorCounter = 0;
    private Timer intervalTimer = new Timer();
    private static final Random random = new Random();

    public PacketFly() {
        super("PacketFly", "\u043b\u0435\u0442\u0430\u0442\u044c \u043d\u0430 \u043f\u0430\u043a\u0435\u0442\u0430\u0445-\u0438\u0437 \u043f\u044f\u0442\u0435\u0440\u043e\u0447\u043a\u0438", Module.Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (PacketFly.mc.currentScreen instanceof GuiDisconnected || PacketFly.mc.currentScreen instanceof GuiMainMenu || PacketFly.mc.currentScreen instanceof GuiMultiplayer || PacketFly.mc.currentScreen instanceof GuiDownloadTerrain) {
            this.toggle();
        }
        Phobos.TICK_TIMER = this.boost.getValue() != false ? 1.088f : 1.0f;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (PacketFly.mc.player == null || PacketFly.mc.world == null) {
            this.toggle();
            return;
        }
        if (PacketFly.mc.player.ticksExisted % 20 == 0) {
            this.cleanPosLooks();
        }
        PacketFly.mc.player.setVelocity(0.0, 0.0, 0.0);
        if (this.teleportId <= 0 && this.type.getValue() != Type.SETBACK) {
            this.startingOutOfBoundsPos = new CPacketPlayer.Position(this.randomHorizontal(), 1.0, this.randomHorizontal(), PacketFly.mc.player.onGround);
            this.packets.add((CPacketPlayer)this.startingOutOfBoundsPos);
            PacketFly.mc.player.connection.sendPacket((Packet)this.startingOutOfBoundsPos);
            return;
        }
        boolean phasing = this.checkCollisionBox();
        this.speedX = 0.0;
        this.speedY = 0.0;
        this.speedZ = 0.0;
        if (PacketFly.mc.gameSettings.keyBindJump.isKeyDown() && (this.hDelay < 1 || this.multiAxis.getValue().booleanValue() && phasing)) {
            this.speedY = PacketFly.mc.player.ticksExisted % (this.type.getValue() == Type.SETBACK || this.type.getValue() == Type.SLOW || this.limit.getValue() == Limit.STRICT ? 10 : 20) == 0 ? (this.antiKickMode.getValue() != AntiKick.NONE ? -0.032 : 0.062) : 0.062;
            this.antiKickTicks = 0;
            this.vDelay = 5;
        } else if (PacketFly.mc.gameSettings.keyBindSneak.isKeyDown() && (this.hDelay < 1 || this.multiAxis.getValue().booleanValue() && phasing)) {
            this.speedY = -0.062;
            this.antiKickTicks = 0;
            this.vDelay = 5;
        }
        if (this.multiAxis.getValue().booleanValue() && phasing || !PacketFly.mc.gameSettings.keyBindSneak.isKeyDown() || !PacketFly.mc.gameSettings.keyBindJump.isKeyDown()) {
            if (PlayerUtils.isPlayerMoving()) {
                double[] dir = PlayerUtils.directionSpeed((phasing && this.phase.getValue() == Phase.NCP ? (this.noPhaseSlow.getValue().booleanValue() ? (this.multiAxis.getValue().booleanValue() ? 0.0465 : 0.062) : 0.031) : 0.26) * (double)this.speed.getValue().floatValue());
                if ((dir[0] != 0.0 || dir[1] != 0.0) && (this.vDelay < 1 || this.multiAxis.getValue().booleanValue() && phasing)) {
                    this.speedX = dir[0];
                    this.speedZ = dir[1];
                    this.hDelay = 5;
                }
            }
            if (this.antiKickMode.getValue() != AntiKick.NONE && (this.limit.getValue() == Limit.NONE || this.limitTicks != 0)) {
                if (this.antiKickTicks < (this.packetMode.getValue() == Mode.BYPASS && this.bounds.getValue() == false ? 1 : 3)) {
                    ++this.antiKickTicks;
                } else {
                    this.antiKickTicks = 0;
                    if (this.antiKickMode.getValue() != AntiKick.LIMITED || !phasing) {
                        double d = this.speedY = this.antiKickMode.getValue() == AntiKick.STRICT ? -0.08 : -0.04;
                    }
                }
            }
        }
        if (phasing && (this.phase.getValue() == Phase.NCP && (double)PacketFly.mc.player.moveForward != 0.0 || (double)PacketFly.mc.player.moveStrafing != 0.0 && this.speedY != 0.0)) {
            this.speedY /= 2.5;
        }
        if (this.limit.getValue() != Limit.NONE) {
            if (this.limitTicks == 0) {
                this.speedX = 0.0;
                this.speedY = 0.0;
                this.speedZ = 0.0;
            } else if (this.limitTicks == 2 && this.jitter.getValue().booleanValue()) {
                if (this.oddJitter) {
                    this.speedX = 0.0;
                    this.speedY = 0.0;
                    this.speedZ = 0.0;
                }
                this.oddJitter = !this.oddJitter;
            }
        } else if (this.jitter.getValue().booleanValue() && this.jitterTicks == 7) {
            this.speedX = 0.0;
            this.speedY = 0.0;
            this.speedZ = 0.0;
        }
        switch (this.type.getValue()) {
            case FAST: {
                PacketFly.mc.player.setVelocity(this.speedX, this.speedY, this.speedZ);
                this.sendPackets(this.speedX, this.speedY, this.speedZ, this.packetMode.getValue(), true, false);
                break;
            }
            case SLOW: {
                this.sendPackets(this.speedX, this.speedY, this.speedZ, this.packetMode.getValue(), true, false);
                break;
            }
            case SETBACK: {
                PacketFly.mc.player.setVelocity(this.speedX, this.speedY, this.speedZ);
                this.sendPackets(this.speedX, this.speedY, this.speedZ, this.packetMode.getValue(), false, false);
                break;
            }
            case FACTOR:
            case DESYNC: {
                float rawFactor = this.factor.getValue().floatValue();
                int factorInt = (int)Math.floor(rawFactor);
                ++this.factorCounter;
                if (this.factorCounter > (int)(20.0 / (((double)rawFactor - (double)factorInt) * 20.0))) {
                    ++factorInt;
                    this.factorCounter = 0;
                }
                for (int i = 1; i <= factorInt; ++i) {
                    PacketFly.mc.player.setVelocity(this.speedX * (double)i, this.speedY * (double)i, this.speedZ * (double)i);
                    this.sendPackets(this.speedX * (double)i, this.speedY * (double)i, this.speedZ * (double)i, this.packetMode.getValue(), true, false);
                }
                this.speedX = PacketFly.mc.player.motionX;
                this.speedY = PacketFly.mc.player.motionY;
                this.speedZ = PacketFly.mc.player.motionZ;
            }
        }
        --this.vDelay;
        --this.hDelay;
        if (this.constrict.getValue().booleanValue() && (this.limit.getValue() == Limit.NONE || this.limitTicks > 1)) {
            PacketFly.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(PacketFly.mc.player.posX, PacketFly.mc.player.posY, PacketFly.mc.player.posZ, false));
        }
        ++this.limitTicks;
        ++this.jitterTicks;
        if (this.limitTicks > (this.limit.getValue() == Limit.STRICT ? (this.limitStrict ? 1 : 2) : 3)) {
            this.limitTicks = 0;
            boolean bl = this.limitStrict = !this.limitStrict;
        }
        if (this.jitterTicks > 7) {
            this.jitterTicks = 0;
        }
    }

    private void sendPackets(double x, double y, double z, Mode mode4, boolean sendConfirmTeleport, boolean sendExtraCT) {
        Vec3d nextPos = new Vec3d(PacketFly.mc.player.posX + x, PacketFly.mc.player.posY + y, PacketFly.mc.player.posZ + z);
        Vec3d bounds = this.getBoundsVec(x, y, z, mode4);
        CPacketPlayer.Position nextPosPacket = new CPacketPlayer.Position(nextPos.x, nextPos.y, nextPos.z, PacketFly.mc.player.onGround);
        this.packets.add((CPacketPlayer)nextPosPacket);
        PacketFly.mc.player.connection.sendPacket((Packet)nextPosPacket);
        if (this.limit.getValue() != Limit.NONE && this.limitTicks == 0) {
            return;
        }
        CPacketPlayer.Position boundsPacket = new CPacketPlayer.Position(bounds.x, bounds.y, bounds.z, PacketFly.mc.player.onGround);
        this.packets.add((CPacketPlayer)boundsPacket);
        PacketFly.mc.player.connection.sendPacket((Packet)boundsPacket);
        if (sendConfirmTeleport) {
            ++this.teleportId;
            if (sendExtraCT) {
                PacketFly.mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(this.teleportId - 1));
            }
            PacketFly.mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(this.teleportId));
            this.posLooks.put(this.teleportId, new TimeVec3d(nextPos.x, nextPos.y, nextPos.z, System.currentTimeMillis()));
            if (sendExtraCT) {
                PacketFly.mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(this.teleportId + 1));
            }
        }
    }

    private Vec3d getBoundsVec(double x, double y, double z, Mode mode4) {
        switch (mode4) {
            case UP: {
                return new Vec3d(PacketFly.mc.player.posX + x, this.bounds.getValue() != false ? (double)(this.strict.getValue() != false ? 255 : 256) : PacketFly.mc.player.posY + 420.0, PacketFly.mc.player.posZ + z);
            }
            case PRESERVE: {
                return new Vec3d(this.bounds.getValue() != false ? PacketFly.mc.player.posX + this.randomHorizontal() : this.randomHorizontal(), this.strict.getValue() != false ? Math.max(PacketFly.mc.player.posY, 2.0) : PacketFly.mc.player.posY, this.bounds.getValue() != false ? PacketFly.mc.player.posZ + this.randomHorizontal() : this.randomHorizontal());
            }
            case LIMITJITTER: {
                return new Vec3d(PacketFly.mc.player.posX + (this.strict.getValue() != false ? x : PacketFly.randomLimitedHorizontal()), PacketFly.mc.player.posY + PacketFly.randomLimitedVertical(), PacketFly.mc.player.posZ + (this.strict.getValue() != false ? z : PacketFly.randomLimitedHorizontal()));
            }
            case BYPASS: {
                if (this.bounds.getValue().booleanValue()) {
                    double rawY = y * 510.0;
                    return new Vec3d(PacketFly.mc.player.posX + x, PacketFly.mc.player.posY + (rawY > (double)(PacketFly.mc.player.dimension == -1 ? 127 : 255) ? -rawY : (rawY < 1.0 ? -rawY : rawY)), PacketFly.mc.player.posZ + z);
                }
                return new Vec3d(PacketFly.mc.player.posX + (x == 0.0 ? (double)(random.nextBoolean() ? -10 : 10) : x * 38.0), PacketFly.mc.player.posY + y, PacketFly.mc.player.posX + (z == 0.0 ? (double)(random.nextBoolean() ? -10 : 10) : z * 38.0));
            }
            case OBSCURE: {
                return new Vec3d(PacketFly.mc.player.posX + this.randomHorizontal(), Math.max(1.5, Math.min(PacketFly.mc.player.posY + y, 253.5)), PacketFly.mc.player.posZ + this.randomHorizontal());
            }
        }
        return new Vec3d(PacketFly.mc.player.posX + x, this.bounds.getValue() != false ? (double)(this.strict.getValue() != false ? 1 : 0) : PacketFly.mc.player.posY - 1337.0, PacketFly.mc.player.posZ + z);
    }

    public double randomHorizontal() {
        int randomValue = random.nextInt(this.bounds.getValue().booleanValue() ? 80 : (this.packetMode.getValue() == Mode.OBSCURE ? (PacketFly.mc.player.ticksExisted % 2 == 0 ? 480 : 100) : 29000000)) + (this.bounds.getValue() != false ? 5 : 500);
        if (random.nextBoolean()) {
            return randomValue;
        }
        return -randomValue;
    }

    public static double randomLimitedVertical() {
        int randomValue = random.nextInt(22);
        randomValue += 70;
        if (random.nextBoolean()) {
            return randomValue;
        }
        return -randomValue;
    }

    public static double randomLimitedHorizontal() {
        int randomValue = random.nextInt(10);
        if (random.nextBoolean()) {
            return randomValue;
        }
        return -randomValue;
    }

    private void cleanPosLooks() {
        this.posLooks.forEach((tp, timeVec3d) -> {
            if (System.currentTimeMillis() - timeVec3d.getTime() > TimeUnit.SECONDS.toMillis(30L)) {
                this.posLooks.remove(tp);
            }
        });
    }

    @Override
    public void onEnable() {
        if (PacketFly.mc.player == null || PacketFly.mc.world == null) {
            this.toggle();
            return;
        }
        this.packets.clear();
        this.posLooks.clear();
        this.teleportId = 0;
        this.vDelay = 0;
        this.hDelay = 0;
        this.postYaw = -400.0f;
        this.postPitch = -400.0f;
        this.antiKickTicks = 0;
        this.limitTicks = 0;
        this.jitterTicks = 0;
        this.speedX = 0.0;
        this.speedY = 0.0;
        this.speedZ = 0.0;
        this.oddJitter = false;
        this.startingOutOfBoundsPos = null;
        this.startingOutOfBoundsPos = new CPacketPlayer.Position(this.randomHorizontal(), 1.0, this.randomHorizontal(), PacketFly.mc.player.onGround);
        this.packets.add((CPacketPlayer)this.startingOutOfBoundsPos);
        PacketFly.mc.player.connection.sendPacket((Packet)this.startingOutOfBoundsPos);
    }

    @Override
    public void onDisable() {
        if (PacketFly.mc.player != null) {
            PacketFly.mc.player.setVelocity(0.0, 0.0, 0.0);
        }
        Phobos.TICK_TIMER = 1.0f;
    }

    @SubscribeEvent
    public void onReceive(PacketEvent.Receive event) {
        if (PacketFly.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (!(PacketFly.mc.currentScreen instanceof GuiDownloadTerrain)) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.getPacket();
                if (PacketFly.mc.player.isEntityAlive()) {
                    if (this.teleportId <= 0) {
                        this.teleportId = ((SPacketPlayerPosLook)event.getPacket()).getTeleportId();
                    } else if (PacketFly.mc.world.isBlockLoaded(new BlockPos(PacketFly.mc.player.posX, PacketFly.mc.player.posY, PacketFly.mc.player.posZ), false) && this.type.getValue() != Type.SETBACK) {
                        if (this.type.getValue() == Type.DESYNC) {
                            this.posLooks.remove(packet.getTeleportId());
                            event.setCanceled(true);
                            if (this.type.getValue() == Type.SLOW) {
                                PacketFly.mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
                            }
                            return;
                        }
                        if (this.posLooks.containsKey(packet.getTeleportId())) {
                            TimeVec3d vec = this.posLooks.get(packet.getTeleportId());
                            if (vec.x == packet.getX() && vec.y == packet.getY() && vec.z == packet.getZ()) {
                                this.posLooks.remove(packet.getTeleportId());
                                event.setCanceled(true);
                                if (this.type.getValue() == Type.SLOW) {
                                    PacketFly.mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
                                }
                                return;
                            }
                        }
                    }
                }
                ((ISPacketPlayerPosLook)packet).setYaw(PacketFly.mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)packet).setPitch(PacketFly.mc.player.rotationPitch);
                packet.getFlags().remove((Object)SPacketPlayerPosLook.EnumFlags.X_ROT);
                packet.getFlags().remove((Object)SPacketPlayerPosLook.EnumFlags.Y_ROT);
                this.teleportId = packet.getTeleportId();
            } else {
                this.teleportId = 0;
            }
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (this.type.getValue() != Type.SETBACK && this.teleportId <= 0) {
            return;
        }
        if (this.type.getValue() != Type.SLOW) {
            event.setX(this.speedX);
            event.setY(this.speedY);
            event.setZ(this.speedZ);
        }
        if (this.phase.getValue() != Phase.NONE && this.phase.getValue() == Phase.VANILLA || this.checkCollisionBox()) {
            PacketFly.mc.player.noClip = true;
        }
    }

    private boolean checkCollisionBox() {
        if (!PacketFly.mc.world.getCollisionBoxes((Entity)PacketFly.mc.player, PacketFly.mc.player.getEntityBoundingBox().expand(0.0, 0.0, 0.0)).isEmpty()) {
            return true;
        }
        return !PacketFly.mc.world.getCollisionBoxes((Entity)PacketFly.mc.player, PacketFly.mc.player.getEntityBoundingBox().offset(0.0, 2.0, 0.0).contract(0.0, 1.99, 0.0)).isEmpty();
    }

    @SubscribeEvent
    public void onSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            if (this.packets.contains((Object)packet)) {
                this.packets.remove((Object)packet);
                return;
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        event.setCanceled(true);
    }

    private static enum AntiKick {
        NONE,
        NORMAL,
        LIMITED,
        STRICT;

    }

    public static enum Phase {
        NONE,
        VANILLA,
        NCP;

    }

    public static enum Type {
        FACTOR,
        SETBACK,
        FAST,
        SLOW,
        DESYNC;

    }

    public static enum Mode {
        UP,
        PRESERVE,
        DOWN,
        LIMITJITTER,
        BYPASS,
        OBSCURE;

    }

    public static enum Limit {
        NONE,
        STRONG,
        STRICT;

    }
}
