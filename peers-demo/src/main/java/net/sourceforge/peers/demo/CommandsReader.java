package net.sourceforge.peers.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandsReader extends Thread {

    public static final String CALL = "call";
    public static final String HANGUP = "hangup";

    private boolean isRunning;
    private EventManager eventManager;
    
    public CommandsReader(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        setRunning(true);
        while (isRunning()) {
            String command;
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            command = command.trim();
            if (command.startsWith(CALL)) {
                String callee = command.substring(
                        command.lastIndexOf(' ') + 1);
                eventManager.call(callee);
            } else if (command.startsWith(HANGUP)) {
                eventManager.hangup();
            } else {
                System.out.println("unknown command " + command);
            }
        }
    }

    
    public synchronized boolean isRunning() {
        return isRunning;
    }


    public synchronized void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    
}
