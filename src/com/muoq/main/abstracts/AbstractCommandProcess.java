package com.muoq.main.abstracts;

import com.muoq.main.ServerApp;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandProcess {

    static final String END_MSG = String.valueOf((char) 0) + String.valueOf((char) 0);

    static int PORT = 14001;
    static String indexFilePath = "/index/cmd-launch-index";

    /* 0:th element is reserved for system level commands
    1:th element is reserved for sender-id
    2:th element element is reserved for command-id
    3:th element is reserved for potential flags for command
     */
    protected static Map<String, String> commandItems = new HashMap<>();

    protected String bin;
    protected String command, flags;
    ProcessBuilder processBuilder;

    protected ProgramInterface programInterface;
    protected boolean isLaunched;

    protected List<ServerApp.Client> clientList;

    protected AbstractCommandProcess(ServerApp.Client client, String command, String flags) {
        initCommandItems();

        this.command = command;
        this.flags = (flags == null) ? "" : " " + flags;
        processBuilder = new ProcessBuilder("/bin/bash", "-c", commandItems.get(command) + " " + PORT + this.flags);

        bin = commandItems.get(command);

        clientList = new ArrayList<>();
        clientList.add(client);

        isLaunched = false;
    }

    public static boolean isValidCommand(String command) {
        if (commandItems.isEmpty())
            initCommandItems();

        return commandItems.containsKey(command);
    }

    protected static void initCommandItems() {

        InputStream indexStream = AbstractCommandProcess.class.getResourceAsStream(indexFilePath);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream));

            String line;

            while ((line = reader.readLine()) != null) {
                String command;
                String launchBin;
                String launchConfig;

                launchConfig = line.split(":", 2)[1];
                command = launchConfig.split("\\|")[0].replace(" ", "").replace("\n", "");
                launchBin = launchConfig.split("\\|")[1].replace("\n", "");

                commandItems.put(command, launchBin);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getCommandItems() {
        if (commandItems.isEmpty()) {
            initCommandItems();
            return commandItems;
        } else {
            return commandItems;
        }
    }

    public boolean launch() {
        try {
            programInterface = new ProgramInterface();
            Thread interfaceThread = new Thread(programInterface);
            interfaceThread.setName("Inter Process Thread");
            interfaceThread.start();

            Process process = processBuilder.start();
            InputStream processInputStream = process.getInputStream();

            byte[] bytes;
            boolean isConnectSuccess = false;

            long connectTimeout = 80000000L;
            long timePast = System.nanoTime();
            while (System.nanoTime() - timePast < connectTimeout) {
                bytes = new byte[8];

                int length = processInputStream.read(bytes);
                StringBuilder processInputBuild = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    processInputBuild.append((char) bytes[i]);
                }

                if (processInputBuild.toString().equals("CON\n")) {
                    System.out.println("launch success");
                    isConnectSuccess = true;
                    break;
                }
            }

            if (!isConnectSuccess) {
                interfaceThread.interrupt();
                programInterface.close();
                return false;
            }

            isLaunched = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void newFlags(String flags) {
        this.flags = (flags == null) ? "" : " " + flags;
        processBuilder = new ProcessBuilder("/bin/bash", "-c", bin + this.flags);
    }

    public void addClient(ServerApp.Client client) {
        if (!clientList.contains(client))
            clientList.add(client);
    }

    public boolean hasLaunched() {
        return isLaunched;
    }

    public static void printIndex() {
        if (commandItems.isEmpty()) {
            initCommandItems();
        }

        for (Map.Entry<String, String> item : commandItems.entrySet()) {
            System.out.println(item.getKey() + " | " + item.getValue());
        }
    }

    protected void handleInput(String input) {
        for (ServerApp.Client client : clientList) {
            client.receiveFromProcess(input.replace(END_MSG, ""));
            System.out.println("Sent to client: " + input.replace(END_MSG, ""));
        }
    }

    public String sendCommand(String command) throws IllegalStateException {
        if (programInterface == null) {
            throw new IllegalStateException("Error: Cannot send command to process that has not launched.");
        }
        return programInterface.sendCommand(command);
    }

    public String getBin() {
        return bin;
    }

    protected class ProgramInterface implements Runnable {

        Socket localSocket;
        PrintWriter writer;

        private StringBuilder internalBuild, bufferBuild;
        private boolean scanningForReturnMessages, isResetReady;

        private ProgramInterface() {
            scanningForReturnMessages = false;
            isResetReady = false;

            internalBuild = new StringBuilder();
            bufferBuild = new StringBuilder();
        }

        private String sendCommand(String command) {
            scanningForReturnMessages = true;

            writer.println(command);
            writer.flush();

            String message;
            while ((message = getCompleteBuild()) == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return message.replace(END_MSG, "");
        }

        private String getCompleteBuild() {

            if (internalBuild.toString().contains(END_MSG)) {
                String build = internalBuild.toString();
                internalBuild = new StringBuilder();
                return build;
            }

            return null;
        }

        private void close() {
            try {
                if (localSocket != null)
                    localSocket.close();
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void buildInternal(String line) {
            if (isResetReady) {
                bufferBuild = new StringBuilder();
                isResetReady = false;
            }
            if (scanningForReturnMessages) {
                bufferBuild.append(line).append("\n");
            }
            if (line.equals(END_MSG)) {
                internalBuild = new StringBuilder(bufferBuild.toString());
                scanningForReturnMessages = false;
                isResetReady = true;
            }
        }

        public void run() {
            try {
                ServerSocket localServerSocket = new ServerSocket(PORT, 0, InetAddress.getByName("localhost"));
                localSocket = localServerSocket.accept();
                localServerSocket.close();

                writer = new PrintWriter(localSocket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));

                writer.println("connect success!");
                writer.flush();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!scanningForReturnMessages)
                        handleInput(line);

                    buildInternal(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
