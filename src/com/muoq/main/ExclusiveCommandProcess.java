package com.muoq.main;

import com.muoq.main.abstracts.AbstractCommandProcess;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class ExclusiveCommandProcess extends AbstractCommandProcess {

    private static Map<String, ExclusiveCommandProcess> existingExclusiveProcesses = new HashMap<>();

    private ExclusiveCommandProcess(ServerApp.Client client, String command, String flags) {
        super(client, command, flags);
    }

    public static ExclusiveCommandProcess getInstance(ServerApp.Client client, String command, String flags) throws InvalidParameterException {
        initCommandItems();

        if (!commandItems.containsKey(command)) {
            throw new InvalidParameterException();
        }

        for (Map.Entry<String, ExclusiveCommandProcess> set : existingExclusiveProcesses.entrySet()) {
            if (commandItems.get(command).equals(set.getValue().bin)) {
                return null;
            }
        }

        ExclusiveCommandProcess processObject = new ExclusiveCommandProcess(client, command, flags);
        processObject.bin = commandItems.get(command);
        existingExclusiveProcesses.put(command, processObject);
        return processObject;
    }

    public static ExclusiveCommandProcess getExistingProcess(ServerApp.Client client, String command) {
        ExclusiveCommandProcess existingProcess = existingExclusiveProcesses.get(command);
        existingProcess.addClient(client);
        return existingProcess;
    }

    //TODO: Override launch() and only permit it to run once

}
