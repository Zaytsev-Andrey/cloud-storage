package ru.geekbrains.nio;

import ru.geekbrains.nio.entities.ServerCommand;
import ru.geekbrains.nio.entities.TelnetClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class NioTelnetServer {
	private final ByteBuffer buffer = ByteBuffer.allocate(512);

	public NioTelnetServer() throws IOException {
		ServerSocketChannel server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(5678));
		server.configureBlocking(false);
		// OP_ACCEPT, OP_READ, OP_WRITE
		Selector selector = Selector.open();

		server.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server started");

		while (server.isOpen()) {
			selector.select();

			var selectionKeys = selector.selectedKeys();
			var iterator = selectionKeys.iterator();

			while (iterator.hasNext()) {
				var key = iterator.next();
				if (key.isAcceptable()) {
					handleAccept(key, selector);
				} else if (key.isReadable()) {
					handleRead(key, selector);
				}
				iterator.remove();
			}
		}
	}

	private void handleRead(SelectionKey key, Selector selector) throws IOException {
		SocketChannel channel = ((SocketChannel) key.channel());
		SocketAddress client = channel.getRemoteAddress();
		TelnetClient telnetClient = (TelnetClient) key.attachment();
		int readBytes = channel.read(buffer);
		if (readBytes < 0) {
			channel.close();
			return;
		} else if (readBytes == 0) {
			return;
		}

		buffer.flip();

		StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining()) {
			sb.append((char) buffer.get());
		}

		buffer.clear();

		// TODO
		// touch [filename] - создание файла
		// mkdir [dirname] - создание директории
		// cd [path] - перемещение по каталогу (.. | ~ )
		// rm [filename | dirname] - удаление файла или папки
		// copy [src] [target] - копирование файла или папки
		// cat [filename] - просмотр содержимого
		// вывод nickname в начале строки

		// NIO
		// NIO telnet server

		if (key.isValid()) {
			String[] command = sb
					.toString()
					.trim()
					.replace(System.getProperty("line.separator"), "")
					.split("\\s+");

			if ("--help".equals(command[0])) {
				handleHelpCommand(selector, client);
			} else if (ServerCommand.LS_COMMAND.getName().equals(command[0])) {
				sendMessage(getFileList(telnetClient).concat(System.lineSeparator()), selector, client);
			} else if (ServerCommand.CD_COMMAND.getName().equals(command[0])) {
				handlerCDCommand(command, telnetClient, selector, client);
			} else if (ServerCommand.MKDIR_COMMAND.getName().equals(command[0])) {
				handlerMKDIRCommand(command, telnetClient);
			} else if (ServerCommand.CHANGE_NICKNAME.getName().equals(command[0])) {
				handlerChangeNicknameCommand(command, telnetClient);
			} else if (ServerCommand.TOUCH_COMMAND.getName().equals(command[0])) {
				handlerTouchCommand(command, telnetClient, selector, client);
			} else if (ServerCommand.RM_COMMAND.getName().equals(command[0])) {
				handlerRMCommand(command, telnetClient, selector, client);
			} else if (ServerCommand.CAT_COMMAND.getName().equals(command[0])) {
				handlerCatCommand(command, telnetClient, selector, client);
			} else if (ServerCommand.CP_COMMAND.getName().equals(command[0])) {
				handlerCPCommand(command, telnetClient, selector, client);
			} else if (ServerCommand.EXIT_COMMAND.getName().equals(command[0])) {
				System.out.println("Client logged out. IP: " + channel.getRemoteAddress());
				channel.close();
				return;
			}
//			else {
//				String errMsg = String.format("wrong: %s: command not found", String.join(" ", command));
//				sendMessage(errMsg, selector, client);
//			}

			// Send command prompt
			sendMessage(getCommandPrompt(telnetClient), selector, client);
		}
	}

	private String getFileList(TelnetClient telnetClient) {
		return String.join(" ", telnetClient.getAbsolutePath().toFile().list());
	}

	private String getCommandPrompt(TelnetClient client) {
		Path currentPath = client.getAbsolutePath();
		Path rootPath = Path.of("").toAbsolutePath();
		String path = "";
		if (rootPath.equals(currentPath)) {
			path = "~";
		} else if (currentPath.equals(currentPath.getRoot())) {
			path = currentPath.toString();
		} else {
			path = currentPath.getFileName().toString();
		}
		return String.format("[%s %s] ", client.getNickname(), path);
	}

	private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {
				if (((SocketChannel)key.channel()).getRemoteAddress().equals(client)) {
					((SocketChannel)key.channel())
							.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
				}
			}
		}
	}

	private void handleHelpCommand(Selector selector, SocketAddress client) throws IOException {
		for (ServerCommand command : ServerCommand.values()) {
			sendMessage(command.toString(), selector, client);
		}
	}

	private void handlerCDCommand(String[] command, TelnetClient telnetClient, Selector selector, SocketAddress client) throws IOException {
		if (command.length < 2) {
			return;
		}

		Path currentPath = telnetClient.getAbsolutePath();
		Path targetPath ;

		if ("..".equals(command[1])) {
			if (currentPath.equals(currentPath.getRoot())) {
				return;
			}
			targetPath = currentPath.getParent();
		} else if ("~".equals(command[1])) {
			targetPath = Path.of("").toAbsolutePath();
		} else {
			targetPath = currentPath.resolve(command[1]);
			if (!Files.exists(targetPath)) {
				String errMsg = String.format("wrong: cd: %s: No such file or directory%n", command[1]);
				sendMessage(errMsg, selector, client);
				return;
			}
		}

		telnetClient.setAbsolutePath(targetPath);
	}

	private void handlerMKDIRCommand(String[] command, TelnetClient telnetClient) throws IOException {
		if (command.length < 2) {
			return;
		}

		Path currentPath = telnetClient.getAbsolutePath();
		Files.createDirectories(currentPath.resolve(command[1]));
	}

	private void handlerChangeNicknameCommand(String[] command, TelnetClient telnetClient) {
		if (command.length < 2) {
			return;
		}

		telnetClient.setNickname(command[1]);
	}

	private void handlerTouchCommand(String[] command, TelnetClient telnetClient, Selector selector, SocketAddress client) throws IOException {
		if (command.length < 2) {
			return;
		}

		Path targetPath = telnetClient.getAbsolutePath().resolve(command[1]);
		try {
			Files.createFile(targetPath);
		} catch (IOException e) {

			String errMsg = String.format("touch: cannot %s: No such file or directory%n", String.join(" ", command));
			sendMessage(errMsg, selector, client);
		}
	}

	private void handlerRMCommand(String[] command, TelnetClient telnetClient, Selector selector, SocketAddress client) throws IOException {
		if (command.length < 2) {
			return;
		}

		Path targetPath = telnetClient.getAbsolutePath().resolve(command[1]);
		try {
			Files.delete(targetPath);
		} catch (IOException e) {
			String errMsg;
			if (e instanceof NoSuchFileException) {
				errMsg = String.format("remove: cannot %s: No such file or directory%n", String.join(" ", command));
			} else if (e instanceof DirectoryNotEmptyException) {
				errMsg = String.format("remove: cannot %s: Directory not empty%n", String.join(" ", command));
			} else {
				errMsg = String.format("remove: cannot %s: Unknown error%n", String.join(" ", command));
			}
			sendMessage(errMsg, selector, client);
		}
	}

	private void handlerCatCommand(String[] command, TelnetClient telnetClient, Selector selector, SocketAddress client) throws IOException {
		if (command.length < 2) {
			return;
		}

		Path file = telnetClient.getAbsolutePath().resolve(command[1]);

		if (!Files.exists(file)) {
			String errMsg = String.format("cat: cannot %s: No such file or directory%n", String.join(" ", command));
			sendMessage(errMsg, selector, client);
			return;
		}

		for (var str : Files.readAllLines(file)) {
			sendMessage(str.concat(System.lineSeparator()), selector, client);
		}
	}

	private void handlerCPCommand(String[] command, TelnetClient telnetClient, Selector selector, SocketAddress client) throws IOException {
		String errMsg;

		if (command.length < 2) {
			return;
		}

		if (command.length < 3) {
			errMsg = String.format("cp: missing destination file operand after %s%n", command[1]);
			sendMessage(errMsg, selector, client);
			return;
		}

		Path sourcePath = telnetClient.getAbsolutePath().resolve(command[1]);
		Path targetPath = telnetClient.getAbsolutePath().resolve(command[2]);

		if (!Files.exists(sourcePath)) {
			errMsg = String.format("cp: cannot stat %s: No such file or directory%n", command[1]);
			sendMessage(errMsg, selector, client);
			return;
		}

		try {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			if (e instanceof DirectoryNotEmptyException) {
				errMsg = String.format("cp: cannot %s: Directory is not empty%n", String.join(" ", command));
			} else if (e instanceof NoSuchFileException) {
				errMsg = String.format("cp: cannot %s: No such file or directory%n", String.join(" ", command));
			} else {
				errMsg = String.format("cp: cannot %s: Unknown error%n", String.join(" ", command));
			}
			sendMessage(errMsg, selector, client);
		}

	}

	private void handleAccept(SelectionKey key, Selector selector) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);
		System.out.println("Client accepted. IP: " + channel.getRemoteAddress());

		InetSocketAddress clientAddress = (InetSocketAddress) channel.getRemoteAddress();
		TelnetClient telnetClient = new TelnetClient("user", clientAddress.getPort(), Path.of("").toAbsolutePath());

		channel.register(selector, SelectionKey.OP_READ, telnetClient);
		channel.write(ByteBuffer.wrap(("Hello user!" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
		channel.write(ByteBuffer.wrap(("Enter --help for support info" + System.lineSeparator())
				.getBytes(StandardCharsets.UTF_8)));
	}

	public static void main(String[] args) throws IOException {
		new NioTelnetServer();
	}
}
