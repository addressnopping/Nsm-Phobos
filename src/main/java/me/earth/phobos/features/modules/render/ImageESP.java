package me.earth.phobos.features.modules.render;

import me.earth.phobos.util.EntityUtil1;
import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.VectorUtils;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.InputStream;
import javax.imageio.ImageIO;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class ImageESP extends Module {

    private Setting<Boolean> noRenderPlayers = this.register(new Setting<Boolean>("No Render Players", false));
    private Setting<CachedImage> imageUrl = this.register(new Setting<> ("Image", CachedImage.NSM));

    private ResourceLocation waifu;

    public ImageESP() {
        super("ImageESP", "hhaha", Category.RENDER, true, false, false);
        onLoad();
    }


    @Override
    public void onEnable() {
        EVENT_BUS.register(this);
    }

    @Override
    public void onUpdate() {
        Phobos.configManager.saveCurrentConfig();
        Phobos.configManager.loadCurrentConfig();
    }

    @Override
    public void onDisable() {
        EVENT_BUS.unregister(this);
    }

    private <T> BufferedImage getImage(final T source, final ThrowingFunction<T, BufferedImage> readFunction) {
        try {
            return readFunction.apply(source);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean shouldDraw(final EntityLivingBase entity) {

        return !entity.equals(mc.player) && entity.getHealth() > 0f && EntityUtil1.isPlayer(entity);
    }

    private ICamera camera = new Frustum();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
        if (this.waifu == null) {
            return;
        }
        double d3 = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double)event.getPartialTicks();
        double d4 = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double)event.getPartialTicks();
        double d5 = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double)event.getPartialTicks();

        camera.setPosition(d3,  d4,  d5);
        final List<EntityPlayer> players = new ArrayList<>(mc.world.playerEntities);
        players.sort(Comparator.comparing(entityPlayer -> mc.player.getDistance((EntityPlayer)entityPlayer)).reversed());
        for (final EntityPlayer player : players) {
            if (player != mc.getRenderViewEntity() && player.isEntityAlive() && camera.isBoundingBoxInFrustum(player.getEntityBoundingBox())) {
                final EntityLivingBase living = (EntityLivingBase)player;
                final Vec3d bottomVec = EntityUtil1.getInterpolatedPos((Entity)living, event.getPartialTicks());
                final Vec3d topVec = bottomVec.add(new Vec3d(0.0, player.getRenderBoundingBox().maxY - player.posY, 0.0));
                final VectorUtils.ScreenPos top = VectorUtils._toScreen(topVec.x, topVec.y, topVec.z);
                final VectorUtils.ScreenPos bot = VectorUtils._toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
                if (!top.isVisible && !bot.isVisible) {
                    continue;
                }
                final int width;
                final int height = width = bot.y - top.y;
                final int x = (int)(top.x - width / 1.8);
                final int y = top.y;
                mc.renderEngine.bindTexture(this.waifu);
                GlStateManager.color(255.0f, 255.0f, 255.0f);
                Gui.drawScaledCustomSizeModalRect(x, y, 0.0f, 0.0f, width, height, width, height, (float)width, (float)height);
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(final RenderPlayerEvent.Pre event) {
        if (this.noRenderPlayers.getValue() && !event.getEntity().equals(mc.player)) {
            event.setCanceled(true);
        }
    }

    public void onLoad() {
        BufferedImage image = null;
        DynamicTexture dynamicTexture;
        try {
            if (getFile(imageUrl.getValue().getName()) != null) {
                image = this.getImage(getFile(imageUrl.getValue().getName()), ImageIO::read);
            }
            /*
            else {
                image = this.getImage(new URL(url.getUrl()), ImageIO::read);
                if (image != null) {
                    try {
                        ImageIO.write(image, "png", getCache(url));
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            */
            if (image == null) {
                Phobos.LOGGER.warn("Failed to load image");
            }
            else {
                dynamicTexture = new DynamicTexture(image);
                dynamicTexture.loadTexture(mc.getResourceManager());
                this.waifu = mc.getTextureManager().getDynamicTextureLocation("NSMPHOBOS_" + imageUrl.getValue(), dynamicTexture);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ThrowingFunction<T, R>
    {
        R apply(final T p0) throws IOException;
    }

    private InputStream getFile(String string) {
        return ImageESP.class.getResourceAsStream(string);
    }

    private enum CachedImage {
        NSM("/images/nsm.png"),
        FITBEARD("/images/fit.png"),
        BRINQUEDO("/images/brinquedo.png");

        String name;

        CachedImage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}