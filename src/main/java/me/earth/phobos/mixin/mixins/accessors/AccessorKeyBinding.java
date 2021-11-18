package me.earth.phobos.mixin.mixins.accessors;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {
    @Accessor("pressed")
    void setPressed(boolean pressed);
}
