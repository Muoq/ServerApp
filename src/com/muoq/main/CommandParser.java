package com.muoq.main;

public class CommandParser {

    static final char NUL = (char) 0;

    static final int ID_MIN = 3;
    static final int ID_MAX = 4;
    static final int CMD_MIN = 3;
    static final int CMD_MAX = 6;

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

        int sectionsDone = 0;

        char charAtI;
        for (int i = 0; i < cmdMsg.length(); i++) {
            while ((charAtI = cmdMsg.charAt(i)) == NUL) {
                sectionsDone++;
                i++;
                if (i >= cmdMsg.length())
                    return;
            }

            if (sectionsDone == 0) {
                id += charAtI;

                if (id.length() > ID_MAX) {
                    id = "";
                    System.out.println("Invalid cmd-msg: client id too long.");
                    return;
                }
            } else if (sectionsDone == 1) {
                if (id.length() < ID_MIN) {
                    System.out.println("Invalid cmd-msg: client id too short.");
                    return;
                }

                cmd += charAtI;

                if (cmd.length() > CMD_MAX) {
                    System.out.println("Invalid cmd-msg: command identifier too long.");
                    return;
                }
            } else if (sectionsDone == 2) {
                if (cmd.length() < CMD_MIN) {
                    System.out.println("Invalid cmd-msg: command identifier too short.");
                    return;
                }

                flags += charAtI;
            } else if (sectionsDone == 3) {
                return;
            }
        }
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