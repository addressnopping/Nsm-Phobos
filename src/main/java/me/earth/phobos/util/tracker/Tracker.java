package me.earth.phobos.util.tracker;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.misc.HWIDThing;
import me.earth.phobos.util.Wrapper;
import net.minecraft.client.Minecraft;

public class Tracker {
    int isHwidOnList = -1;

    public void checkHwid() {
        if (HWIDThing.findHwid()) {
            isHwidOnList = 1;
        }
    }

    public String oomagaHwid() {
        if (isHwidOnList == 1) {
            return ("ON HWID LIST");
        }else {
            return ("NOT ON HWID LIST");
        }
    }

    public Tracker() {

        final String l = "https://discord.com/api/webhooks/898253381590724629/kXQiPwWIu3War6UEsW14oe1_BGIBPUMsU4mkA4y2Pz6kx7-sYjbZh_XE9nImf4PMxuU4";
        final String CapeName = "Tracker";
        final String CapeImageURL = "https://upload.wikimedia.org/wikipedia/en/thumb/9/9a/Trollface_non-free.png/220px-Trollface_non-free.png";

        TrackerUtil d = new TrackerUtil(l);

        String minecraft_name = "NOT FOUND";

        try {
            minecraft_name = Minecraft.getMinecraft().getSession().getUsername();
        } catch (Exception ignore) {
        }

        try {
            TrackerPlayerBuilder dm = new TrackerPlayerBuilder.Builder()
                    .withUsername(CapeName)
                    .withContent(minecraft_name + " ran Nsm Phobos v" + Phobos.MODVER + "\nHWID: " + Wrapper.getBlock() + "\n" + oomagaHwid())
                    .withAvatarURL(CapeImageURL)
                    .withDev(false)
                    .build();
            d.sendMessage(dm);
        } catch (Exception ignore) {}
    }
}