package com.muoq.main;

import com.muoq.main.abstracts.AbstractCommandProcess;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    static final char NUL = (char) 0;

    static final int ID_MIN = 3;
    static final int SECTIONS = 4;
    static final int CMD_MAX = 6;

    private boolean isParseSuccessful;
    String error;

    private String sysCmd;
    private String id;
    private String cmd;
    private String flags;

    public CommandParser(String cmdMsg) {
        sysCmd = "";
        id = "";
        cmd = "";
        flags = "";

        parse(cmdMsg);
    }

    private void parse(String cmdMsg)  {
        if (cmdMsg.length() == 0)
            return;

        isParseSuccessful = false;

        String[] splitCmdMsg = cmdMsg.split(":");
        if (splitCmdMsg.length == 1) {
            error = "Invalid cmd-msg.";
            return;
        }

        cmdMsg = splitCmdMsg[1];
        sysCmd = splitCmdMsg[0];

        int sectionsDone = 0;

        char charAtI;
        for (int i = 0; i < cmdMsg.length(); i++) {
            while ((charAtI = cmdMsg.charAt(i)) == NUL) {
                sectionsDone++;
                i++;
                if (i >= cmdMsg.length()) {
                    if (sectionsDone != SECTIONS) {
                        nullAll();
                        return;
                    } else {

                        if (!AbstractCommandProcess.isValidCommand(cmd)) {
                            error = String.format("Command \'%s\' not found.", cmd);
                            nullAll();
                            return;
                        }

                        isParseSuccessful = true;
                        return;
                    }
                }
            }

            if (sectionsDone == 0) {

                id += charAtI;

            } else if (sectionsDone == 1) {

                cmd += charAtI;

            } else if (sectionsDone == 2) {

                flags += charAtI;

            } else if (sectionsDone == SECTIONS) {

                if (id.length() < ID_MIN) {
                    nullAll();
                    error = "Invalid cmd-msg.";
                    System.out.println("Invalid cmd-msg: client id too short.");
                    return;
                }
                if (cmd.length() > CMD_MAX) {
                    nullAll();
                    error = "Invalid cmd-msg.";
                    System.out.println("Invalid cmd-msg: command identifier too long.");
                    return;
                }

                if (!AbstractCommandProcess.isValidCommand(cmd)) {
                    error = String.format("Command \'%s\' not found.", cmd);
                    nullAll();
                    return;
                }

                isParseSuccessful = true;
                return;
            }
        }
    }

    private void nullAll() {
        sysCmd = null;
        id = null;
        cmd = null;
        flags = null;
    }

    public static String escapeString(String unescaped) {
//          NON ESCAPED CHARACTERS
//        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
//        's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.', '_', '-', '+', '@',
//        ':', '/', '%', '~', NUL

        List<Character> allowedChars = new ArrayList<>();
        List<Integer> asciiDec = new ArrayList<>();
        for (int i = 43; i < 59; i++) {
            asciiDec.add(i);
        }
        for (int i = 64; i < 91; i++) {
            asciiDec.add(i);
        }
        for (int i = 97; i < 123; i++) {
            asciiDec.add(i);
        }
        asciiDec.add(0);

        for (int asciiDecimalCode : asciiDec) {
            allowedChars.add((char) asciiDecimalCode);
        }
        allowedChars.add('_');
        allowedChars.add('%');
        allowedChars.add('\\');

        String escapee = unescaped;
        List<Character> charsInString = new ArrayList<>();

        for (int i = 0; i < escapee.length(); i++) {
            char charAtI = escapee.charAt(i);

            if (!charsInString.contains(charAtI) && !allowedChars.contains(charAtI)) {
                charsInString.add(charAtI);
            }
        }

        escapee = escapee.replace("\\", "\\\\");
        for (Character character : charsInString) {
            escapee = escapee.replace(character.toString(), "\\" + character.toString());
        }
        escapee = escapee.replace("\\ ", " ");

        return escapee;
    }

    public boolean isParseSuccess() {
        return isParseSuccessful;
    }

    public String getError() {
        return error;
    }

    public String getSysCmd() {
        return sysCmd;
    }

    public String getId() {
        return id;
    }

    public String getCmd() {
        return cmd;
    }

    public String getFlags() {
        return flags;
    }

}