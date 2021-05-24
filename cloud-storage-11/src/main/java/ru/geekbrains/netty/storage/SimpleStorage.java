package ru.geekbrains.netty.storage;

import ru.geekbrains.netty.entities.Client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleStorage {
    public static final Path ROOT_PATH = Path.of("server");

    private static Map<SocketAddress, Client> clients = new ConcurrentHashMap<>();

    public static void addClient(SocketAddress address) {
        Client newClient = new Client(((InetSocketAddress) address).getPort());
        clients.put(address, newClient);
    }

    public static void removeClient(SocketAddress address) {
        clients.remove(address);
    }

    public static Client getClient(SocketAddress address) {
        return clients.get(address);
    }
}
