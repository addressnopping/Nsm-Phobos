package me.earth.phobos.event.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class TotemPopEvent1 extends Event {

    private final String name;
    private final int popCount;
    private final int entId;

    public TotemPopEvent1(String name, int count, int entId) {
        this.name = name;
        this.popCount = count;
        this.entId = entId;
    }

    public String getName() {
        return name;
    }

    public int getPopCount() {
        return popCount;
    }

    public int getEntityId() {
        return entId;
    }
}
