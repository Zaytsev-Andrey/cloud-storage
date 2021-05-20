package ru.geekbrains.nio.entities;

import java.nio.file.Path;

/**
 * Storage info about active telnet clients.
 * Path must contain absolute path.
 * Nickname returns as <nickname>@<port>
 */
public class TelnetClient {
    private String nickname;
    private int port;
    private Path absolutePath;

    public TelnetClient(String nickname, int port, Path absolutePath) {
        this.nickname = nickname;
        this.port = port;
        this.absolutePath = absolutePath;
    }

    public String getNickname() {
        return String.format("%s@%d", nickname, port);
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAbsolutePath(Path absolutePath) {
        this.absolutePath = absolutePath;
    }
}
