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

    protected List<ServerApp.Client> clientList;

    protected AbstractCommandProcess(ServerApp.Client client, String command, String flags) {
        initCommandItems();

        this.command = command;
        this.flags = (flags == null) ? "" : " " + flags;
        processBuilder = new ProcessBuilder("/bin/bash", "-c", commandItems.get(command) + " " + PORT + this.flags);

        bin = commandItems.get(command);

        clientList = new ArrayList<>();
        clientList.add(client);
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

    public String launch() {
        try {

            programInterface = new ProgramInterface();
            Thread interfaceThread = new Thread(programInterface);
            interfaceThread.setName("Inter Process Thread");
            interfaceThread.start();

            Process process = processBuilder.start();
            InputStream processInputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));

            System.out.println(processBuilder.command());
            String processOut = "";
            String line;
            while ((line = reader.readLine()) != null && !line.replace("\n", "").equals("CON")) {
                System.out.println("line: " + line);
                processOut += line + "\n";
            }
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println("Error: " + line);
            }

            return processOut;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void newFlags(String flags) {
        this.flags = (flags == null) ? "" : " " + flags;
        processBuilder = new ProcessBuilder("/bin/bash", "-c", bin + this.flags);
    }

    public void addClient(ServerApp.Client client) {
        if (!clientList.contains(client))
            clientList.add(client);
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

    }

    public String sendCommand(String command) {
        return programInterface.sendCommand(command);
    }

    public String getBin() {
        return bin;
    }

    protected class ProgramInterface implements Runnable {

        Socket localSocket;
        PrintWriter writer;

        public String sendCommand(String command) {
            writer.println(command);
            writer.flush();

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
                String input;
                StringBuilder builder = new StringBuilder();

                while ((input = reader.readLine()) != null) {
                    builder.append(input);
                }

                return builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void run() {
            try {
                ServerSocket localServerSocket = new ServerSocket(PORT);
                localSocket = localServerSocket.accept();
                localServerSocket.close();

                writer = new PrintWriter(localSocket.getOutputStream());
                writer.println("connect success!");
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
                String input;

                while((input = reader.readLine()) != null) {
                    handleInput(input);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
