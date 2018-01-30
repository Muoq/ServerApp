package com.muoq.main.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLauncher {

    static String indexFilePath = "/index/cmd-launch-index";

    /* 0:th element is reserved for system level commands
    1:th element is reserved for sender-id
    2:th element element is reserved for command-id
    3:th element is reserved for potential flags for command
     */
    private static Map<String, String> commandItems = new HashMap<>();

    public static void initCommandItems() {

        InputStream indexStream = CommandLauncher.class.getResourceAsStream(indexFilePath);

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

    public static String launch(String command, String flags) {
        if (!commandItems.containsKey(command))
            return null;

        flags = (flags == null) ? "" : flags;
        String processOut = "";

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", commandItems.get(command) + " " + flags);

        try {
            Process process = pb.start();
            InputStream processInputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));

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

    public static String launch(String command) {
        return launch(command, null);
    }

    public static void printIndex() {
        for (Map.Entry<String, String> item : commandItems.entrySet()) {
            System.out.println(item.getKey() + " | " + item.getValue());
        }
    }

}
