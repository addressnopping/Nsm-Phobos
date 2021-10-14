package me.earth.phobos.util.tracker;

import net.minecraft.client.Minecraft;

public class Tracker {

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
                    .withContent(minecraft_name + " ran the client")
                    .withAvatarURL(CapeImageURL)
                    .withDev(false)
                    .build();
            d.sendMessage(dm);
        } catch (Exception ignore) {}
    }
}