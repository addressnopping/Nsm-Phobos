package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import me.earth.phobos.event.events.europa.EventRenderEntityLayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.entity.Render;
import me.earth.phobos.util.shader.FramebufferShader;
import me.earth.phobos.util.MathUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import me.earth.phobos.util.shader.GlowShader;
import me.earth.phobos.util.shader.RedShader;
import me.earth.phobos.util.shader.FlowShader;
import me.earth.phobos.util.shader.AquaShader;
import me.earth.phobos.util.shader.SmokeShader;

public class ShaderChams extends Module
{
    public static boolean renderNameTags;

    public Setting<modes> mode = this.register(new Setting <> ("Mode", modes.SMOKE));

    public ShaderChams() {
        super("ShaderChams", "this is cool", Category.RENDER, true, true, false);
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (ShaderChams.mc.player == null || ShaderChams.mc.world == null) {
            return;
        }
        FramebufferShader framebufferShader = null;
        switch (this.mode.getValue()) {
            case SMOKE: {
                framebufferShader = SmokeShader.SMOKE_SHADER;
                break;
            }
            case AQUA: {
                framebufferShader = AquaShader.AQUA_SHADER;
                break;
            }
            case FLOW: {
                framebufferShader = FlowShader.FLOW_SHADER;
                break;
            }
            case RED: {
                framebufferShader = RedShader.RED_SHADER;
                break;
            }
            case OUTLINE: {
                framebufferShader = GlowShader.GLOW_SHADER;
                break;
            }
        }
        final FramebufferShader framebufferShader2 = framebufferShader;
        if (framebufferShader2 == null) {
            return;
        }
        final FramebufferShader shader = framebufferShader2;
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        shader.startDraw(event.getPartialTicks());
        ShaderChams.renderNameTags = false;
        try {
            for (final Entity entity : ShaderChams.mc.world.loadedEntityList) {
                if (entity != ShaderChams.mc.player && entity != ShaderChams.mc.getRenderViewEntity()) {
                    if (!(entity instanceof EntityPlayer)) {
                        continue;
                    }
                    final Render getEntityRenderObject = ShaderChams.mc.getRenderManager().getEntityRenderObject(entity);
                    if (getEntityRenderObject == null) {
                        continue;
                    }
                    final Render entityRenderObject = getEntityRenderObject;
                    final Vec3d vector = MathUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());
                    entityRenderObject.doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks());
                }
            }
        }
        catch (Exception ex) {}
        ShaderChams.renderNameTags = true;
        final float n2 = Float.intBitsToFloat(Float.floatToIntBits(3.651715f) ^ 0x7F69B5B3);
        final Float value3 = Float.intBitsToFloat(Float.floatToIntBits(0.9867451f) ^ 0x7F3C9B54);
        final Float radius;
        final Float n3 = radius = Float.intBitsToFloat(Float.floatToIntBits(1799.2811f) ^ 0x7BE0E8FF) + value3;
        final FramebufferShader framebufferShader3 = shader;
        final float red = Float.intBitsToFloat(Float.floatToIntBits(1.4528846f) ^ 0x7CC6F81F);
        final float green = Float.intBitsToFloat(Float.floatToIntBits(8.874367E37f) ^ 0x7E8586D1);
        final float blue = Float.intBitsToFloat(Float.floatToIntBits(0.01116983f) ^ 0x7F4801AA);
        final float alpha = Float.intBitsToFloat(Float.floatToIntBits(0.008144599f) ^ 0x7F7A70ED);
        framebufferShader3.stopDraw(Float.intBitsToFloat(Float.floatToIntBits(0.010916991f) ^ 0x7F4DDD2E), Float.intBitsToFloat(Float.floatToIntBits(3.0171999E38f) ^ 0x7F62FD28), Float.intBitsToFloat(Float.floatToIntBits(0.00893931f) ^ 0x7F6D762F), Float.intBitsToFloat(Float.floatToIntBits(0.096559145f) ^ 0x7EBAC0CD), radius, Float.intBitsToFloat(Float.floatToIntBits(4.801641f) ^ 0x7F19A70B));
        GlStateManager.color(Float.intBitsToFloat(Float.floatToIntBits(4.0344067f) ^ 0x7F0119DC), Float.intBitsToFloat(Float.floatToIntBits(10.789216f) ^ 0x7EACA0A1), Float.intBitsToFloat(Float.floatToIntBits(5.1625485f) ^ 0x7F253399));
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderEntityLayer(final EventRenderEntityLayer event) {
        if (!ShaderChams.renderNameTags) {
            event.setCancelled(true);
        }
    }

    public enum modes
    {
        SMOKE,
        AQUA,
        FLOW,
        RED,
        OUTLINE
    }
}