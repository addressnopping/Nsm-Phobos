package me.earth.phobos.features.modules.combat;

import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.features.command.Command;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BowBomb extends Module {

    private boolean shooting;
    private long lastShootTime;

    public BowBomb() { super("BowBomb", "Swagger bow module", Category.COMBAT, true, false, false); }

    public Setting <Boolean> Bows = this.register ( new Setting <> ( "Bows", true ) );
    public Setting <Boolean> pearls = this.register ( new Setting <> ( "Pearls", true ) );
    public Setting <Boolean> eggs = this.register ( new Setting <> ( "Eggs", true ) );
    public Setting <Boolean> snowballs = this.register ( new Setting <> ( "SnowBallz", true ) );
    public Setting <Integer> Timeout = this.register ( new Setting <> ( "Timeout", 5000, 100, 20000 ) );
    public Setting <Integer> spoofs = this.register ( new Setting <> ( "Spoofs", 10, 1, 1000 ) ); // BOOOOOOOOOOOOOOOOOOOOOOOOOOOM
    public Setting <Boolean> cancelMotion = this.register ( new Setting <> ( "Cancel Motion", false ) );
    public Setting <Boolean> debug = this.register ( new Setting <> ( "Debug", false ) );
    public Setting <Boolean> bypass = this.register ( new Setting <> ( "Bypass", false ) );

    private boolean shouldCancelMotion = false;

    @Override
    public void onEnable() {
        if ( this.isEnabled()) {
            shooting = false;
            lastShootTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() != 0) return;

        if (event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();

            if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                ItemStack handStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

                if (!handStack.isEmpty() && handStack.getItem() != null && handStack.getItem() instanceof ItemBow && Bows.getValue()) {
                    doSpoofs();
                    if (debug.getValue()) Command.sendMessage("Trying to spoof");
                }
            }

        } else if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem packet2 = (CPacketPlayerTryUseItem) event.getPacket();

            if (packet2.getHand() == EnumHand.MAIN_HAND) {
                ItemStack handStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

                if (!handStack.isEmpty() && handStack.getItem() != null) {
                    if (handStack.getItem() instanceof ItemEgg && eggs.getValue()) {
                        doSpoofs();
                    } else if (handStack.getItem() instanceof ItemEnderPearl && pearls.getValue()) {
                        doSpoofs();
                    } else if (handStack.getItem() instanceof ItemSnowball && snowballs.getValue()) {
                        doSpoofs();
                    }
                }
            }
        }
    }

    private void doSpoofs() {
        if (System.currentTimeMillis() - lastShootTime >= Timeout.getValue()) {
            shooting = true;
            lastShootTime = System.currentTimeMillis();

            //mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

            if(cancelMotion.getValue()) {
                shouldCancelMotion = true;
            }

            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

            for (int index = 0; index < spoofs.getValue(); ++index) {
                if (bypass.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 1e-10, mc.player.posY + 1e-10, mc.player.posZ + 1e-10, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - 1e-10, mc.player.posY - 1e-10, mc.player.posZ - 1e-10, true));
                } else {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - 1e-10, mc.player.posY - 1e-10, mc.player.posZ - 1e-10, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 1e-10, mc.player.posY + 1e-10, mc.player.posZ + 1e-10, false));
                }

            }

            shouldCancelMotion = false;
            if (debug.getValue()) Command.sendMessage("Spoofed");

            shooting = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMove(MoveEvent event) {
        event.setCanceled(shouldCancelMotion);
    }
}