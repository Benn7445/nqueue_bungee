package me.absurd.nqueue.listeners;

import me.absurd.nqueue.NQueue;
import me.absurd.nqueue.queue.Queue;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LogoutListener implements Listener {

    public NQueue nQueue;

    public LogoutListener(NQueue nQueue) {
        this.nQueue = nQueue;
    }

    @EventHandler
    public void playerDisconnectEvent(PlayerDisconnectEvent event){
        Queue.leaveQueue(event.getPlayer(), nQueue, false);
    }
}
