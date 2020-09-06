package me.absurd.nqueue.queue;

import me.absurd.nqueue.NQueue;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;

import static me.absurd.nqueue.listeners.PluginMessageListener.*;

public class Queue {

    public String name;
    public String bungeeserver;
    public boolean open;
    public boolean pause;
    public int amount;
    public int seconds;
    public HashMap<Integer, ProxiedPlayer> players;
    public HashMap<ProxiedPlayer, Integer> priority;

    public Queue(String name, String bungeeserver, boolean open, boolean pause, int amount, int seconds, HashMap<Integer, ProxiedPlayer> players, HashMap<ProxiedPlayer, Integer> priority) {
        this.name = name;
        this.bungeeserver = bungeeserver;
        this.open = open;
        this.pause = pause;
        this.amount = amount;
        this.seconds = seconds;
        this.players = players;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getBungeeserver() {
        return bungeeserver;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public HashMap<Integer, ProxiedPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<Integer, ProxiedPlayer> players) {
        this.players = players;
    }

    public HashMap<ProxiedPlayer, Integer> getPriority() {
        return priority;
    }

    public static Queue getQueueByName(String name, NQueue nQueue){
        for(Queue queue : nQueue.getQueues){
            if(queue.getName().equalsIgnoreCase(name)){
                return queue;
            }
        }
        return null;
    }
    public static Queue getQueueByBungee(String name, NQueue nQueue){
        for(Queue queue : nQueue.getQueues){
            if(queue.getBungeeserver().equalsIgnoreCase(name)){
                return queue;
            }
        }
        return null;
    }

    public static Queue getQueueByPlayer(ProxiedPlayer proxiedPlayer, NQueue nQueue){
        for(Queue queue : nQueue.getQueues){
            if(queue.getPlayers().containsValue(proxiedPlayer)){
                return queue;
            }
        }
        return null;
    }

    public static int getQueuePos(ProxiedPlayer proxiedPlayer, NQueue nQueue){
        Queue queue = getQueueByPlayer(proxiedPlayer, nQueue);
        if(queue != null){
            for(int position : queue.getPlayers().keySet()){
                if(queue.getPlayers().get(position) == proxiedPlayer){
                    return position;
                }
            }
        }
        return 0;
    }

    public static void joinQueue(ProxiedPlayer player, Queue queue, int priority, NQueue nQueue) {
        if (player != null) {
            if (queue != null) {
                if (queue.isOpen()) {
                    boolean alreadyInQueue = false;
                    for (Queue queues : nQueue.getQueues) {
                        if (!alreadyInQueue) {
                            if (queues.getPlayers().containsValue(player)) {
                                alreadyInQueue = true;
                            }
                        }
                    }
                    if (!alreadyInQueue) {
                        int i = 1;
                        if (queue.getPlayers().size() > 0) {
                            for (int p : queue.getPlayers().keySet()) {
                                ProxiedPlayer players = queue.getPlayers().get(p);
                                if (players != player) {
                                    int otherPriority = queue.getPriority().getOrDefault(players, 1000);
                                    if (priority >= otherPriority) {
                                        i++;
                                    }
                                }
                            }
                        }
                        HashMap<Integer, ProxiedPlayer> newQueue = new HashMap<>();
                        for (int higherN = queue.getPlayers().size(); higherN > 0; higherN--) {
                            if (queue.getPlayers().containsKey(higherN)) {
                                ProxiedPlayer higher = queue.getPlayers().get(higherN);
                                if (higher != player) {
                                    if (higherN >= i) {
                                        newQueue.put(higherN + 1, higher);
                                    } else {
                                        newQueue.put(higherN, higher);
                                    }
                                }
                            }
                        }
                        queue.setPlayers(newQueue);
                        queue.getPlayers().put(i, player);
                        sendPositionAll(queue.getName(), nQueue);
                        queue.getPriority().put(player, priority);
                        nQueue.sendForwardPluginMessage(player, "JOINED-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                        sendPosition(player.getName(), nQueue);
                        sendMax(queue.getName(), nQueue);
                        sendQueue(player.getName(), nQueue);
                    } else {
                        nQueue.sendForwardPluginMessage(player, "ALREADY-IN-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                    }
                } else {
                    nQueue.sendForwardPluginMessage(player, "QUEUE-CLOSED/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                }
            } else {
                nQueue.sendForwardPluginMessage(player, "QUEUE-NOT-FOUND/" + player.getName() + "/" + "None", "nqueue:message");
            }
        }
    }

    public static void leaveQueue(ProxiedPlayer player, NQueue nQueue, boolean message) {

        Queue queue = Queue.getQueueByPlayer(player, nQueue);
        if (queue != null) {
            int i = Queue.getQueuePos(player, nQueue);
            queue.getPriority().remove(player);
            if (queue.getPlayers() != null) {
                HashMap<Integer, ProxiedPlayer> newQueue = new HashMap<>();
                for (int priority : queue.getPlayers().keySet()) {
                    if (queue.getPlayers().containsKey(priority)) {
                        ProxiedPlayer higher = queue.getPlayers().get(priority);
                        if (higher != player) {
                            if (priority >= i) {
                                newQueue.put(priority - 1, higher);
                            } else {
                                newQueue.put(priority, higher);
                            }
                            sendPosition(higher.getName(), nQueue);
                        }
                    }
                }
                queue.setPlayers(newQueue);
            }
            sendMax(queue.getName(), nQueue);
            sendPositionAll(queue.getName(), nQueue);
            if (message) {
                nQueue.sendForwardPluginMessage(player, "LEFT-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
            }
        } else {
            nQueue.sendForwardPluginMessage(player, "NOT-IN-QUEUE/" + player.getName() + "/" + "None", "nqueue:message");
        }
    }
}
