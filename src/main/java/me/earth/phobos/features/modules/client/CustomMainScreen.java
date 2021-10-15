package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.custom.GuiCustomMainScreen;
import me.earth.phobos.features.modules.Module;

public class CustomMainScreen extends Module {
    public CustomMainScreen() {
        super("CustomMainScreen", "custom main screen", Category.CLIENT, true, false, false);
    }
    @Override
    public void onEnable() {
        Phobos.customMainScreen = new GuiCustomMainScreen();
    }
    @Override
    public void onDisable() {
        Phobos.customMainScreen = null;
    }
}
