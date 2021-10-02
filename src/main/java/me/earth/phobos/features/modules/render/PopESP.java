package me.earth.phobos.features.modules.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.event.events.RenderEntityEvent;
import me.earth.phobos.event.events.TotemPopEvent1;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

//thx notorious client lmao

public class PopESP
        extends Module {

    public PopESP() {
        super("PopESP", "Renders pops", Module.Category.RENDER, true, false, false);
    }

    private final Setting<Integer> fadeTime = this.register(new Setting<Integer>("FadeTime", 3000, 1, 5000));
    public Setting<Float> fadeSpeed = this.register(new Setting<Float>("FadeSpeed", Float.valueOf(0.05f), Float.valueOf(0.01f), Float.valueOf(1.0f)));
    public Setting<FadeMode> fadeMode = this.register(new Setting("FadeMode", FadeMode.Elevator));
    public Setting<ElevatorMode> elevatorMode = this.register(new Setting("ElevatorMode", ElevatorMode.Heaven));
    public Setting<RenderMode> renderMode = this.register(new Setting("RenderMode", RenderMode.Both));
    public Setting<Float> lineWidth = this.register(new Setting<Float>("FadeSpeed", Float.valueOf(1f), Float.valueOf(0.1f), Float.valueOf(3f)));
    private final Setting<Integer> r = this.register(new Setting<Integer>("Red", 255, 0, 255));
    private final Setting<Integer> g = this.register(new Setting<Integer>("Green", 255, 0, 255));
    private final Setting<Integer> b = this.register(new Setting<Integer>("Blue", 255, 0, 255));
    private final Setting<Integer> a = this.register(new Setting<Integer>("Alpha", 255, 0, 255));

    private static final HashMap<EntityOtherPlayerMP, Long> popFakePlayerMap = new HashMap<>();

    float fade = 1.0f;

    public String getMetaData() {
        if(fadeMode.getValue().equals(FadeMode.Elevator)) {
            return " [" + ChatFormatting.GRAY + elevatorMode.getValue() + ChatFormatting.RESET + "]";
        }else {
            return " [" + ChatFormatting.GRAY + fadeMode.getValue() + ChatFormatting.RESET + "]";
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        for (Map.Entry<EntityOtherPlayerMP, Long> entry : new HashMap<>(popFakePlayerMap).entrySet()) {
            boolean wireFrame;
            boolean textured;
            if(renderMode.getValue().equals(RenderMode.Both)) {
                wireFrame = true;
                textured = true;
            }else if(renderMode.getValue().equals(RenderMode.Wireframe)) {
                wireFrame = true;
                textured = false;
            }else {
                wireFrame = false;
                textured = true;
            }
            if(System.currentTimeMillis() - entry.getValue() < (long) fadeTime.getValue() && fadeMode.getValue().equals(FadeMode.Elevator)) {
                if(elevatorMode.getValue().equals(ElevatorMode.Heaven)) {
                    entry.getKey().posY += fadeSpeed.getValue() * event.getPartialTicks();
                }else {
                    entry.getKey().posY -= fadeSpeed.getValue() * event.getPartialTicks();
                }
            }else if(System.currentTimeMillis() - entry.getValue() < (long) fadeTime.getValue() && fadeMode.getValue().equals(FadeMode.Fade)) {
                fade -= fadeSpeed.getValue();
            }
            if(System.currentTimeMillis() - entry.getValue() > (long) fadeTime.getValue() || fade == 0.0f) {
                popFakePlayerMap.remove(entry.getKey());
                continue;
            }
            GL11.glPushMatrix();
            GL11.glDepthRange(0.01, 1.0f);
            if(wireFrame) {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glLineWidth(lineWidth.getValue());
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glColor4f(r.getValue() / 255f, g.getValue() / 255f, b.getValue() / 255f, fadeMode.getValue().equals(FadeMode.Fade) ? fade : 1f);
                renderEntityStatic(entry.getKey(), event.getPartialTicks(), true);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glColor4f(1f, 1f, 1f, 1f);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }
            if(textured) {
                GL11.glPushAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GL11.glLineWidth(1.5f);
                GL11.glColor4f(r.getValue() / 255f, g.getValue() / 255f, b.getValue() / 255f, fadeMode.getValue().equals(FadeMode.Fade) ? fade : a.getValue() / 255f);
                renderEntityStatic(entry.getKey(), event.getPartialTicks(), true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glColor4f(1f, 1f, 1f, 1f);
                GL11.glPopAttrib();
            }
            GL11.glDepthRange(0.0, 1.0f);
            GL11.glPopMatrix();
            fade = 1.0f;
        }
    }

    @SubscribeEvent
    public void onPop(TotemPopEvent1 event) {
        if (mc.world.getEntityByID(event.getEntityId()) != null) {
            final Entity entity = mc.world.getEntityByID(event.getEntityId());
            if (entity instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer) entity;
                final EntityOtherPlayerMP fakeEntity = new EntityOtherPlayerMP(mc.world, player.getGameProfile());
                fakeEntity.copyLocationAndAnglesFrom(player);
                fakeEntity.rotationYawHead = player.rotationYawHead;
                fakeEntity.prevRotationYawHead = player.rotationYawHead;
                fakeEntity.rotationYaw = player.rotationYaw;
                fakeEntity.prevRotationYaw = player.rotationYaw;
                fakeEntity.rotationPitch = player.rotationPitch;
                fakeEntity.prevRotationPitch = player.rotationPitch;
                fakeEntity.cameraYaw = fakeEntity.rotationYaw;
                fakeEntity.cameraPitch = fakeEntity.rotationPitch;
                popFakePlayerMap.put(fakeEntity, System.currentTimeMillis());
            }
        }
    }

    public void renderEntityStatic(Entity entityIn, float partialTicks, boolean p_188388_3_) {
        if (entityIn.ticksExisted == 0)
        {
            entityIn.lastTickPosX = entityIn.posX;
            entityIn.lastTickPosY = entityIn.posY;
            entityIn.lastTickPosZ = entityIn.posZ;
        }

        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        float f = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks;
        int i = entityIn.getBrightnessForRender();

        if (entityIn.isBurning())
        {
            i = 15728880;
        }

        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
        mc.getRenderManager().renderEntity(entityIn, d0 - mc.getRenderManager().viewerPosX, d1 - mc.getRenderManager().viewerPosY, d2 - mc.getRenderManager().viewerPosZ, f, partialTicks, p_188388_3_);
    }

    private void glColor(boolean textured, boolean wireframe) {
        final Color clr = new Color(r.getValue(), g.getValue(), b.getValue(), a.getValue());
        if(textured)
            GL11.glColor4f(clr.getRed() / 255f, clr.getGreen() / 255f, clr.getBlue() / 255f, clr.getAlpha() / 255f);
        if(wireframe)
            GL11.glColor4f(clr.getRed() / 255f, clr.getGreen() / 255f, clr.getBlue() / 255f, 1f);
    }

    public enum FadeMode {
        Elevator,
        Fade,
        None
    }

    public enum ElevatorMode {
        Heaven,
        Hell
    }

    public enum RenderMode {
        Both,
        Textured,
        Wireframe
    }
}