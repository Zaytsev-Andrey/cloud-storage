package ru.geekbrains.nio.entities;

/**
 * Contains all available server commands and their description
 */
public enum ServerCommand {
    LS_COMMAND ("ls", "view all files and directories"),
    MKDIR_COMMAND ("mkdir","create directory"),
    CHANGE_NICKNAME ("nick", "change nickname"),
    CD_COMMAND ("cd", "change directory"),
    TOUCH_COMMAND ("touch", "create file"),
    RM_COMMAND ("rm", "remove file or directory"),
    CAT_COMMAND ("cat", "view content file"),
    CP_COMMAND ("cp", "copy file or directory"),
    EXIT_COMMAND ("exit", "close session");

    private String name;
    private String info;

    ServerCommand(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("\t%s\t%s%n", name, info);
    }
}
