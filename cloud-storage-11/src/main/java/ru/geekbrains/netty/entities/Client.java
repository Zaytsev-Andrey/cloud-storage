package ru.geekbrains.netty.entities;

import ru.geekbrains.netty.storage.SimpleStorage;

import java.nio.file.Path;

/**
 * Storage info about active telnet clients.
 * Nickname returns as <nickname>@<port>
 */
public class Client {
    private final int port;
    private String nickname;
    private Path path;

    public Client(int port) {
        this.nickname = "localhost";
        this.port = port;
        this.path = SimpleStorage.ROOT_PATH;
    }

    public String getNickname() {
        return String.format("%s@%d", nickname, port);
    }

    public Path getPath() {
        return path;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
