package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(ConsoleHelper.readInt());
            ConsoleHelper.writeMessage("Сервер запущен!");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто " + socket.getRemoteSocketAddress());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message newClientName = connection.receive();
                if (newClientName.getType().equals(MessageType.USER_NAME)
                        && !newClientName.getData().isEmpty()
                        && !connectionMap.containsKey(newClientName.getData())
                ) {
                    connectionMap.put(newClientName.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return newClientName.getData();
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) {
            connectionMap.forEach((name, connect) -> {
                if (!name.equals(userName)) {
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, name));
                        } catch (IOException e) {
                                    e.printStackTrace();
                        }
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Ожибко");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((s, connection) -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
