package com.muoq.main;

import com.muoq.main.abstracts.AbstractCommandProcess;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class ExclusiveCommandProcess extends AbstractCommandProcess {

    private static Map<String, ExclusiveCommandProcess> existingExclusiveProcesses = new HashMap<>();

    private ExclusiveCommandProcess(String command, String flags) {
        super(command, flags);
    }

    public static ExclusiveCommandProcess getInstance(String command, String flags) throws InvalidParameterException {
        initCommandItems();

        if (!commandItems.containsKey(command)) {
            throw new InvalidParameterException();
        }

        for (Map.Entry<String, ExclusiveCommandProcess> set : existingExclusiveProcesses.entrySet()) {
            if (commandItems.get(command).equals(set.getValue().bin)) {
                return null;
            }
        }

        ExclusiveCommandProcess processObject = new ExclusiveCommandProcess(command, flags);
        processObject.bin = commandItems.get(command);
        existingExclusiveProcesses.put(command, processObject);
        return processObject;
    }

    public static ExclusiveCommandProcess getExistingProcess(String command) {
        return existingExclusiveProcesses.get(command);
    }

    //TODO: Override launch() and only permit it to run once

}
