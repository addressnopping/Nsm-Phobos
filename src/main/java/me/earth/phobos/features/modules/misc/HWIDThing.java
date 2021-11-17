package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.Wrapper;

import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class HWIDThing extends Module {
    public HWIDThing() {
        super("HWIDThing", "Some things for hwid", Module.Category.MISC, true, true, false);
    }

    public static boolean findHwid() {
        try {
            String entityList = new Scanner(new URL(new String(Base64.getDecoder().decode( Phobos.starting_client().getBytes()))).openStream(), "UTF-8").useDelimiter("\\A").next();
            return entityList.contains(Wrapper.getBlock());
        }
        catch (Exception e) {
            return false;
        }
    }
}