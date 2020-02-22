package jp.minecraftday.minecraftinstrumentality.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SampleEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public SampleEvent(){
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
