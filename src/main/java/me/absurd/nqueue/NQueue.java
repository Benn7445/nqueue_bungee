package me.absurd.nqueue;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.absurd.nqueue.listeners.LogoutListener;
import me.absurd.nqueue.listeners.PluginMessageListener;
import me.absurd.nqueue.listeners.ServerConnectListener;
import me.absurd.nqueue.queue.Queue;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class NQueue extends Plugin {

    public List<String> offlines = new ArrayList<>();
    public List<Queue> getQueues = new ArrayList<>();
    public HashMap<ProxiedPlayer, Integer> getPriority = new HashMap<>();
    public Configuration configuration;

    @Override
    public void onEnable() {
        getProxy().registerChannel("nqueue:nqueue");
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        getProxy().getPluginManager().registerListener(this, new LogoutListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
        if (!getDataFolder().exists())
            getDataFolder().mkdir();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            for (String name : configuration.getSection("QUEUES").getKeys()) {
                Queue queue = new Queue(name, configuration.getString("QUEUES." + name + ".BUNGEE"), true, configuration.getBoolean("QUEUES." + name + ".AUTOPAUSE"), configuration.getInt("QUEUES." + name + ".AMOUNT")
                        , configuration.getInt("QUEUES." + name + ".SEC"), new HashMap<>(), new HashMap<>());
                getQueues.add(queue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendQueue();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void sendForwardPluginMessage(ProxiedPlayer player, final String finalMessage, String channel) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeUTF(finalMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte[] data = byteStream.toByteArray();
        out.writeShort(data.length);
        out.write(data);
        if (player != null) {
            if (player.getServer() != null && player.getServer().getInfo() != null) {
                player.getServer().getInfo().sendData("nqueue:nqueue", out.toByteArray());
            }
        }
    }

    public void sendForwardPluginMessageMax(final String finalMessage, String channel) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeUTF(finalMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte[] data = byteStream.toByteArray();
        out.writeShort(data.length);
        out.write(data);
        for (ServerInfo si : getProxy().getServers().values()) {
            si.sendData("nqueue:nqueue", out.toByteArray());
        }
    }

    public void sendQueue() {
        for (Queue queue : getQueues) {
            getProxy().getScheduler().schedule(this, () -> {
                if(!queue.isPause()) {
                    if (configuration != null) {
                        for (int i = configuration.getInt("QUEUE-RUN.PLAYERS"); i >= 0; i--) {
                            if (queue.getPlayers().containsKey(i)) {
                                if (!offlines.contains(queue.getBungeeserver())) {
                                    ProxiedPlayer player = queue.getPlayers().get(i);
                                    if (player != null) {
                                        ServerConnectListener.joinable.add(player);
                                        ServerInfo serverInfo = getProxy().getServers().get(queue.getBungeeserver());
                                        if (serverInfo != null) {
                                            player.connect(serverInfo);
                                            Queue.leaveQueue(player, this, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, 0, configuration.getInt("QUEUE-RUN.SECONDS"), TimeUnit.SECONDS);
            getProxy().getScheduler().schedule(this, () -> getProxy().getServers().get(queue.getBungeeserver()).ping((result, error) -> {
                if(error!=null){
                    offlines.add(queue.getBungeeserver());
                } else {
                    offlines.remove(queue.getBungeeserver());
                }
            }), 0, 5, TimeUnit.SECONDS);
        }
    }
}