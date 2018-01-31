package com.muoq.main.util;

import com.muoq.main.CommandParser;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProcess {

    static String indexFilePath = "/index/cmd-launch-index";

    private static List<CommandProcess> runningProcesses = new ArrayList<>();

    /* 0:th element is reserved for system level commands
    1:th element is reserved for sender-id
    2:th element element is reserved for command-id
    3:th element is reserved for potential flags for command
     */
    private static Map<String, String> commandItems = new HashMap<>();

    private String bin;
    private String command, flags;
    ProcessBuilder processBuilder;

    ProgramInterface programInterface;

    public CommandProcess(String command, String flags) {
        initCommandItems();

        this.command = command;
        this.flags = (flags == null) ? "" : flags;
        processBuilder = new ProcessBuilder("cmd.exe", "/c", commandItems.get(command), this.flags);

        bin = commandItems.get(command);

    }

//    public static CommandProcess getInstance(String command, String flags) {
//        initCommandItems();
//
//        boolean isAlreadyRunning = false;
//
//        for (CommandProcess runningProcess : runningProcesses) {
//            if (runningProcess.bin == commandItems.get(command)) {
//                isAlreadyRunning = true;
//            }
//        }
//
//        if (isAlreadyRunning) {
//            return null;
//        } else {
//            CommandProcess processObject = new CommandProcess(command, flags);
//
//        }
//    }

    private static void initCommandItems() {

        InputStream indexStream = CommandProcess.class.getResourceAsStream(indexFilePath);

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

    private String launchInternal(ProcessBuilder pb) {
        if (!commandItems.containsKey(command))
            return null;

        try {
            Process process = pb.start();
            InputStream processInputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));

            String processOut = "";
            String line;
            while ((line = reader.readLine()) != null) {
                processOut += line + "\n";
            }

            return processOut;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String launch() {
        return launchInternal(processBuilder);
    }

    public String launch(String command, String flags) {
        flags = (flags == null) ? "" : flags;

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", commandItems.get(command), flags);

        return launchInternal(pb);
    }

    public String launch(String command) {
        return launch(command, null);
    }

    public void printIndex() {
        for (Map.Entry<String, String> item : commandItems.entrySet()) {
            System.out.println(item.getKey() + " | " + item.getValue());
        }
    }

    private void handleInput(String input) {

    }

    private class ProgramInterface implements Runnable {

        Socket localSocket;
        ServerSocket localServerSocket;

        public ProgramInterface(int port) {
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
