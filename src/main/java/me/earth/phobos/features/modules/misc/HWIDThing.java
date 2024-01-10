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
}
