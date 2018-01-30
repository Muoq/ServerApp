package com.muoq.main;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    static final char NUL = (char) 0;

    static final int ID_MIN = 3;
    static final int CMD_MIN = 3;
    static final int CMD_MAX = 6;

    private boolean isParseSuccessful;

    private String id;
    private String cmd;
    private String flags;

    public CommandParser(String cmdMsg) {
        id = "";
        cmd = "";
        flags = "";
        parse(cmdMsg);
    }

    public void parse (String cmdMsg) {
        if (cmdMsg.length() == 0)
            return;

        isParseSuccessful = false;
        int sectionsDone = 0;

        char charAtI;
        for (int i = 0; i < cmdMsg.length(); i++) {
            while ((charAtI = cmdMsg.charAt(i)) == NUL) {
                sectionsDone++;
                i++;
                if (i >= cmdMsg.length()) {
                    if (sectionsDone != 4) {
                        nullAll();
                        return;
                    } else {
                        isParseSuccessful = true;
                        return;
                    }
                }
            }

            if (sectionsDone == 0) {
                id += charAtI;
            } else if (sectionsDone == 1) {
                if (id.length() < ID_MIN) {
                    nullAll();
                    System.out.println("Invalid cmd-msg: client id too short.");
                    return;
                }

                cmd += charAtI;

                if (cmd.length() > CMD_MAX) {
                    nullAll();
                    System.out.println("Invalid cmd-msg: command identifier too long.");
                    return;
                }
            } else if (sectionsDone == 2) {
                if (cmd.length() < CMD_MIN) {
                    nullAll();
                    System.out.println("Invalid cmd-msg: command identifier too short.");
                    return;
                }

                flags += charAtI;
            } else if (sectionsDone == 4) {
                isParseSuccessful = true;
                return;
            }
        }
    }

    private void nullAll() {
        id = null;
        cmd = null;
        flags = null;
    }

    public static String escapeStringSimple(String unescaped) {
        String escapee = unescaped;
        List<Character> charsInString = new ArrayList<>();

        int length = escapee.length();
        for (int i = 0; i < length; i++) {
            if (!charsInString.contains(escapee.charAt(i)) && escapee.charAt(i) != '\\') {
                charsInString.add(escapee.charAt(i));
            }
        }

        escapee = escapee.replace("\\", "\\\\");
        for (Character character : charsInString) {
            escapee = escapee.replace(character.toString(), "\\" + character.toString());
        }

        return escapee;
    }

    public static String unescapeString(String escaped) {
        char[] nonEscape = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.', '_', '-', '+', '@',
            ':', '/', '%', (char) 0};
        List<Character> nonEscapeList = new ArrayList<>();
        for (char character : nonEscape) {
            nonEscapeList.add(character);
        }

        String unescapee = escaped;
        List<Character> charsInString = new ArrayList<>();

        int length = unescapee.length();
        for (int i = 0; i < length; i++) {
            if (!charsInString.contains(unescapee.charAt(i)) && nonEscapeList.contains(unescapee.charAt(i))) {
                charsInString.add(unescapee.charAt(i));
            }
        }

        for (Character character : charsInString) {
            unescapee = unescapee.replace("\\" + character.toString(), character.toString());
        }
        unescapee = unescapee.replace("\\ ", " ");

        return unescapee;
    }

    public static String escapeString(String unescaped) {
        return unescapeString(escapeStringSimple(unescaped));
    }

    public boolean isParseSuccess() {return isParseSuccessful;}

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