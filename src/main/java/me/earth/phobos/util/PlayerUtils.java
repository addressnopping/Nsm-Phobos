package me.earth.phobos.util;

import java.text.DecimalFormat;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static double[] lastLookAt = null;

    public static boolean isPlayerMoving() {
        return PlayerUtils.mc.gameSettings.keyBindForward.isKeyDown() || PlayerUtils.mc.gameSettings.keyBindBack.isKeyDown() || PlayerUtils.mc.gameSettings.keyBindRight.isKeyDown() || PlayerUtils.mc.gameSettings.keyBindLeft.isKeyDown();
    }

    public static boolean isPlayerMovingLegit() {
        return PlayerUtils.mc.gameSettings.keyBindForward.isKeyDown() && !PlayerUtils.mc.player.isSneaking();
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = PlayerUtils.getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        float[] rotations = new float[]{PlayerUtils.mc.player.rotationYaw + MathHelper.wrapDegrees((float)(yaw - PlayerUtils.mc.player.rotationYaw)), PlayerUtils.mc.player.rotationPitch + MathHelper.wrapDegrees((float)(pitch - PlayerUtils.mc.player.rotationPitch))};
        return rotations;
    }

    private static Vec3d getEyesPos() {
        return new Vec3d(PlayerUtils.mc.player.posX, PlayerUtils.mc.player.posY + (double)PlayerUtils.mc.player.getEyeHeight(), PlayerUtils.mc.player.posZ);
    }

    public static Vec3d roundPos(Vec3d vec3d) {
        DecimalFormat f = new DecimalFormat(".##");
        double x = Double.parseDouble(f.format(vec3d.x));
        double y = Double.parseDouble(f.format(vec3d.y));
        double z = Double.parseDouble(f.format(vec3d.z));
        return new Vec3d(x, y, z);
    }

    public static double degreeToRadian(float degree) {
        return (double)degree * (Math.PI / 180);
    }

    public static double[] getDirectionFromYaw(float yaw) {
        double radian = PlayerUtils.degreeToRadian(yaw);
        double x = Math.sin(radian);
        double z = Math.cos(radian);
        return new double[]{x, z};
    }

    public static double[] directionSpeed(double speed) {
        float forward = PlayerUtils.mc.player.movementInput.moveForward;
        float side = PlayerUtils.mc.player.movementInput.moveStrafe;
        float yaw = PlayerUtils.mc.player.prevRotationYaw + (PlayerUtils.mc.player.rotationYaw - PlayerUtils.mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double[] directionSpeed(double speed, EntityPlayerSP player) {
        Minecraft mc = Minecraft.getMinecraft();
        float forward = player.movementInput.moveForward;
        float side = player.movementInput.moveStrafe;
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static boolean isPlayerAboveVoid() {
        boolean aboveVoid = false;
        if (PlayerUtils.mc.player.posY <= 0.0) {
            return true;
        }
        int i = 1;
        while ((double)i < PlayerUtils.mc.player.posY) {
            BlockPos pos = new BlockPos(PlayerUtils.mc.player.posX, (double)i, PlayerUtils.mc.player.posZ);
            if (!(PlayerUtils.mc.world.getBlockState(pos).getBlock() instanceof BlockAir)) {
                aboveVoid = false;
                break;
            }
            aboveVoid = true;
            ++i;
        }
        return aboveVoid;
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((double)(difX * difX + difZ * difZ));
        return new float[]{(float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)), (float)MathHelper.wrapDegrees((double)Math.toDegrees(Math.atan2(difY, dist)))};
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        double pitch = Math.asin(diry /= len);
        double yaw = Math.atan2(dirz /= len, dirx /= len);
        pitch = pitch * 180.0 / Math.PI;
        yaw = yaw * 180.0 / Math.PI;
        return new double[]{yaw += 90.0, pitch};
    }

    public static void moveToBlockCenter() {
        double centerX = Math.floor(PlayerUtils.mc.player.posX) + 0.5;
        double centerZ = Math.floor(PlayerUtils.mc.player.posZ) + 0.5;
        PlayerUtils.mc.player.setPosition(centerX, PlayerUtils.mc.player.posY, centerZ);
        PlayerUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(centerX, PlayerUtils.mc.player.posY, centerZ, PlayerUtils.mc.player.onGround));
    }

    public static boolean isKeyDown(int i) {
        if (i != 0 && i < 256) {
            return i < 0 ? Mouse.isButtonDown((int)(i + 100)) : Keyboard.isKeyDown((int)i);
        }
        return false;
    }
}