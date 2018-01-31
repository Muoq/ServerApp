package com.muoq.main.abstracts;

import com.muoq.main.ServerApp;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandProcess {

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

    protected AbstractProgramInterface programInterface;

    protected List<ServerApp.Client> clientList;

    protected AbstractCommandProcess(String command, String flags) {
        initCommandItems();

        this.command = command;
        this.flags = (flags == null) ? "" : " " + flags;
        processBuilder = new ProcessBuilder("/bin/bash", "-c", commandItems.get(command) + this.flags);

        bin = commandItems.get(command);

        clientList = new ArrayList<>();
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
            Process process = processBuilder.start();
            InputStream processInputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));

            System.out.println(processBuilder.command());
            String processOut = "";
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("line: " + line);
                processOut += line + "\n";
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

    public String getBin() {
        return bin;
    }

    protected abstract class AbstractProgramInterface implements Runnable {

        Socket localSocket;
        ServerSocket localServerSocket;

        protected AbstractProgramInterface(int port) {
            try {
                localServerSocket = new ServerSocket(port, 1, InetAddress.getLocalHost());
                localSocket = localServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
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
