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
                set.getValue().addClient(client);
                return set.getValue();
            }
        }

        ExclusiveCommandProcess processObject = new ExclusiveCommandProcess(client, command, flags);
        processObject.bin = commandItems.get(command);
        existingExclusiveProcesses.put(command, processObject);
        return processObject;
    }

    //TODO: Override launch() and only permit it to run once

}
