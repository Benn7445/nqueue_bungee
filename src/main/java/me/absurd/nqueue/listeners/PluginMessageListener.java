package me.absurd.nqueue.listeners;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.absurd.nqueue.NQueue;
import me.absurd.nqueue.queue.Queue;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class PluginMessageListener implements Listener {

    public NQueue nQueue;

    public PluginMessageListener(NQueue nQueue) {
        this.nQueue = nQueue;
    }

    @EventHandler
    public void pluginMessageEvent(PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase("nqueue:nqueue")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
            try {
                String forward = in.readUTF();
                String servers = in.readUTF();
                String subchannel = in.readUTF();
                String data = in.readUTF().substring(2);
                if (subchannel.equalsIgnoreCase("nqueue:joinqueue")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data.split("/")[0]);
                    if (player != null) {
                        Queue queue = Queue.getQueueByName(data.split("/")[1], nQueue);
                        if (queue != null) {
                            int priority = Integer.parseInt(data.split("/")[2]);
                            boolean bypass = Boolean.parseBoolean(data.split("/")[3]);
                            boolean donator = Boolean.parseBoolean(data.split("/")[4]);
                            if (donator) {
                                if (Queue.getQueueByName("donator-" + queue.getName(), nQueue) != null) {
                                    queue = Queue.getQueueByName("donator-" + queue.getName(), nQueue);
                                }
                            }
                            if (queue != null) {
                                if (bypass) {
                                    ServerConnectListener.joinable.add(player);
                                    ServerConnectListener.bypass.add(player);
                                    player.connect(nQueue.getProxy().getServerInfo(queue.getBungeeserver()));
                                } else {
                                    Queue.joinQueue(player, queue, priority, nQueue);
                                }
                            }
                        }
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:leavequeue")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data);
                    if (player != null) {
                        Queue.leaveQueue(player, nQueue, true);
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:kickqueue")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data);
                    if (player != null) {
                        Queue.leaveQueue(player, nQueue, true);
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:pause")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data.split("/")[0]);
                    if (player == null) {
                        player = Iterables.getFirst(nQueue.getProxy().getPlayers(), null);
                    }
                    if (player != null) {
                        Queue queue = Queue.getQueueByName(data.split("/")[1], nQueue);
                        if (queue != null) {
                            if (!queue.isPause()) {
                                queue.setPause(true);
                                if (player.getName().equalsIgnoreCase(data.split("/")[0])) {
                                    nQueue.sendForwardPluginMessage(player, "PAUSED-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                                }
                            } else {
                                nQueue.sendForwardPluginMessage(player, "ALREADY-PAUSED/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                            }
                        } else {
                            nQueue.sendForwardPluginMessage(player, "QUEUE-NOT-FOUND/" + player.getName() + "/" + "none", "nqueue:message");
                        }
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:unpause")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data.split("/")[0]);
                    if (player == null) {
                        player = Iterables.getFirst(nQueue.getProxy().getPlayers(), null);
                    }
                    if (player != null) {
                        Queue queue = Queue.getQueueByName(data.split("/")[1], nQueue);
                        if (queue != null) {
                            if (queue.isPause()) {
                                queue.setPause(false);
                                if (player.getName().equalsIgnoreCase(data.split("/")[0])) {
                                    nQueue.sendForwardPluginMessage(player, "UNPAUSED-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                                }
                            } else {
                                nQueue.sendForwardPluginMessage(player, "ALREADY-UNPAUSED/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                            }
                        } else {
                            nQueue.sendForwardPluginMessage(player, "QUEUE-NOT-FOUND/" + player.getName() + "/" + "none", "nqueue:message");
                        }
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:open")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data.split("/")[0]);
                    if (player == null) {
                        player = Iterables.getFirst(nQueue.getProxy().getPlayers(), null);
                    }
                    if (player != null) {
                        Queue queue = Queue.getQueueByName(data.split("/")[1], nQueue);
                        if (queue != null) {
                            if (!queue.isOpen()) {
                                queue.setOpen(true);
                                if (player.getName().equalsIgnoreCase(data.split("/")[0])) {
                                    nQueue.sendForwardPluginMessage(player, "OPENED-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                                }
                            } else {
                                nQueue.sendForwardPluginMessage(player, "ALREADY-OPEN/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                            }
                        } else {
                            nQueue.sendForwardPluginMessage(player, "QUEUE-NOT-FOUND/" + player.getName() + "/" + "none", "nqueue:message");
                        }
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:close")) {
                    ProxiedPlayer player = nQueue.getProxy().getPlayer(data.split("/")[0]);
                    if (player == null) {
                        player = Iterables.getFirst(nQueue.getProxy().getPlayers(), null);
                    }
                    if (player != null) {
                        Queue queue = Queue.getQueueByName(data.split("/")[1], nQueue);
                        if (queue != null) {
                            if (queue.isOpen()) {
                                queue.setOpen(false);
                                if (player.getName().equalsIgnoreCase(data.split("/")[0])) {
                                    nQueue.sendForwardPluginMessage(player, "CLOSED-QUEUE/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                                }
                            } else {
                                nQueue.sendForwardPluginMessage(player, "ALREADY-CLOSED/" + player.getName() + "/" + queue.getName(), "nqueue:message");
                            }
                        } else {
                            nQueue.sendForwardPluginMessage(player, "QUEUE-NOT-FOUND/" + player.getName() + "/" + "none", "nqueue:message");
                        }
                    }
                } else if (subchannel.equalsIgnoreCase("nqueue:queue")) {
                    sendQueue(data, nQueue);
                } else if (subchannel.equalsIgnoreCase("nqueue:position")) {
                    sendPosition(data, nQueue);
                } else if (subchannel.equalsIgnoreCase("nqueue:maxamount")) {
                    sendMax(data, nQueue);
                } else if (subchannel.equalsIgnoreCase("nqueue:priority")) {
                    nQueue.getPriority.put(nQueue.getProxy().getPlayer(data.split("/")[1]), Integer.valueOf(data.split("/")[0]));
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void sendQueue(String data, NQueue nQueue) {
        ProxiedPlayer player = nQueue.getProxy().getPlayer(data);
        if (player != null) {
            Queue queue = Queue.getQueueByPlayer(player, nQueue);
            if (queue != null) {
                nQueue.sendForwardPluginMessage(player, player.getName() + "/" + queue.getName(), "nqueue:queue");
            } else {
                nQueue.sendForwardPluginMessage(player, player.getName() + "/" + "null", "nqueue:queue");
            }
        }
    }

    public static void sendPosition(String data, NQueue nQueue) {
        ProxiedPlayer player = nQueue.getProxy().getPlayer(data);
        if (player != null) {
            Queue queue = Queue.getQueueByPlayer(player, nQueue);
            if (queue != null) {
                nQueue.sendForwardPluginMessage(player, player.getName() + "/" + Queue.getQueuePos(player, nQueue), "nqueue:position");
            }
        }
    }

    public static void sendPositionAll(String data, NQueue nQueue) {
        Queue queue = Queue.getQueueByName(data, nQueue);
        if (queue != null) {
            StringBuilder sb = new StringBuilder();
            for (ProxiedPlayer player : nQueue.getProxy().getPlayers()) {
                sb.append(player.getName()).append(":").append(Queue.getQueuePos(player, nQueue)).append("/");
            }
            if (sb.toString().length() > 0) {
                nQueue.sendForwardPluginMessageMax(sb.toString().substring(0, sb.toString().length() - 1), "nqueue:positionall");
            }
        }
    }

    public static void sendMax(String data, NQueue nQueue) {
        for (ServerInfo s : nQueue.getProxy().getServers().values()) {
            String servername = s.getName();
            if (servername != null) {
                ProxiedPlayer player = null;
                for (ProxiedPlayer players : nQueue.getProxy().getPlayers()) {
                    if (players.getServer() != null && players.getServer().getInfo() != null)
                        if (players.getServer().getInfo().getName().equals(servername)) {
                            player = players;
                        }
                }
                if (player != null) {
                    Queue queue = Queue.getQueueByName(data, nQueue);
                    if (queue != null) {
                        nQueue.sendForwardPluginMessageMax(queue.getName() + "/" + queue.getPlayers().size(), "nqueue:maxamount");
                    }
                }
            }
        }
    }

    public static void askPriority(ProxiedPlayer player, NQueue nQueue) {
        if (player != null) {
            nQueue.sendForwardPluginMessageMax(player.getName(), "nqueue:askpriority");
        }
    }
}