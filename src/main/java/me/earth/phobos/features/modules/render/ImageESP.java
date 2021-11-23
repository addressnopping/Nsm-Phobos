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
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class ImageESP extends Module {

    private Setting<Boolean> noRenderPlayers = this.register(new Setting<Boolean>("No Render Players", false));
    private Setting<CachedImage> imageUrl = this.register(new Setting<CachedImage> ("Image", CachedImage.NSM, CachedImage.values()).onChanged(imagesOnChangedValue -> {
        this.waifu = null;
        this.onLoad();
        return;
    }));
    private ResourceLocation waifu;
    private ICamera camera;

    public ImageESP() {
        super("ImageESP", "things on players", Category.RENDER, true, false, false);
        this.camera = (ICamera)new Frustum();
        this.onLoad();
    }


    @Override
    public void onEnable() {
        EVENT_BUS.register((Object)this);
    }

    @Override
    public void onDisable() {
        EVENT_BUS.unregister((Object)this);
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
        return !entity.equals((Object)ImageESP.mc.player) && entity.getHealth() > 0.0f && EntityUtil1.isPlayer((Entity)entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
        if (this.waifu == null) {
            return;
        }
        final double d3 = ImageESP.mc.player.lastTickPosX + (ImageESP.mc.player.posX - ImageESP.mc.player.lastTickPosX) * event.getPartialTicks();
        final double d4 = ImageESP.mc.player.lastTickPosY + (ImageESP.mc.player.posY - ImageESP.mc.player.lastTickPosY) * event.getPartialTicks();
        final double d5 = ImageESP.mc.player.lastTickPosZ + (ImageESP.mc.player.posZ - ImageESP.mc.player.lastTickPosZ) * event.getPartialTicks();
        this.camera.setPosition(d3, d4, d5);
        final List<EntityPlayer> players = new ArrayList<EntityPlayer>(ImageESP.mc.world.playerEntities);
        players.sort(Comparator.comparing(entityPlayer -> ImageESP.mc.player.getDistance((Entity)entityPlayer)).reversed());
        for (final EntityPlayer player : players) {
            if (player != ImageESP.mc.getRenderViewEntity() && player.isEntityAlive() && this.camera.isBoundingBoxInFrustum(player.getEntityBoundingBox())) {
                final EntityLivingBase living = (EntityLivingBase)player;
                final Vec3d bottomVec = EntityUtil1.getInterpolatedPos((Entity)living, event.getPartialTicks());
                final Vec3d topVec = bottomVec.add(new Vec3d(0.0, player.getRenderBoundingBox().maxY - player.posY, 0.0));
                final VectorUtils.ScreenPos top = VectorUtils._toScreen(topVec.x, topVec.y, topVec.z);
                final VectorUtils.ScreenPos bot = VectorUtils._toScreen(bottomVec.x, bottomVec.y, bottomVec.z);
                if (!top.isVisible && !bot.isVisible) {
                    continue;
                }
                final int height;
                final int width = height = bot.y - top.y;
                final int x = (int)(top.x - width / 1.8);
                final int y = top.y;
                ImageESP.mc.renderEngine.bindTexture(this.waifu);
                GlStateManager.color(255.0f, 255.0f, 255.0f);
                Gui.drawScaledCustomSizeModalRect(x, y, 0.0f, 0.0f, width, height, width, height, (float)width, (float)height);
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(final RenderPlayerEvent.Pre event) {
        if (this.noRenderPlayers.getValue() && !event.getEntity().equals((Object)ImageESP.mc.player)) {
            event.setCanceled(true);
        }
    }

    public void onLoad() {
        BufferedImage image = null;
        try {
            if (this.getFile(this.imageUrl.getValue().getName()) != null) {
                image = this.getImage(this.getFile(this.imageUrl.getValue().getName()), ImageIO::read);
            }
            if (image == null) {
                Phobos.LOGGER.warn("Failed to load image");
            }
            else {
                final DynamicTexture dynamicTexture = new DynamicTexture(image);
                dynamicTexture.loadTexture(ImageESP.mc.getResourceManager());
                this.waifu = ImageESP.mc.getTextureManager().getDynamicTextureLocation("XULU_" + this.imageUrl.getValue().name(), dynamicTexture);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream getFile(final String string) {
        return ImageESP.class.getResourceAsStream(string);
    }

    private enum CachedImage
    {
        NSM("/images/nsm.png"),
        FITBEARD("/images/fit.png"),
        BRINQUEDO("/images/brinquedo.png");

        String name;

        private CachedImage(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    @FunctionalInterface
    private interface ThrowingFunction<T, R>
    {
        R apply(final T p0) throws IOException;
    }
}