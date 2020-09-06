package me.absurd.nqueue.listeners;

import me.absurd.nqueue.NQueue;
import me.absurd.nqueue.queue.Queue;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerConnectListener implements Listener {

    public NQueue nQueue;
    public static List<ProxiedPlayer> joinable = new ArrayList<>();
    public static List<ProxiedPlayer> bypass = new ArrayList<>();

    public ServerConnectListener(NQueue nQueue) {
        this.nQueue = nQueue;
    }

    @EventHandler
    public void pluginMessageEvent(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Queue queue = Queue.getQueueByBungee(event.getTarget().getName(), nQueue);
        if (queue != null) {
            PluginMessageListener.askPriority(player, nQueue);
            if (!joinable.contains(player) && Queue.getQueueByPlayer(player, nQueue) == null) {
                nQueue.getProxy().getScheduler().schedule(nQueue, (() -> Queue.joinQueue(player, queue, nQueue.getPriority.getOrDefault(player, 1000), nQueue)), 500, 0, TimeUnit.MILLISECONDS);
                event.setCancelled(true);
            } else if((bypass.contains(player) && nQueue.configuration.getBoolean("BYPASS")) || joinable.contains(player)) {
                joinable.remove(player);
            } else {
                event.setCancelled(true);
            }
        }
    }
}
