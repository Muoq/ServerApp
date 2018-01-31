package com.muoq.main;

import com.muoq.main.util.CommandProcess;
import com.muoq.main.util.InputReceiver;
import com.muoq.main.util.InputScanner;

import javax.net.ssl.*;
import java.io.*;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerApp {

// ************************************
    static final boolean DEBUG = true;
// ************************************

    static final int PORT = 14000;
    static final String STORE_PATH = "/keystore/serverstore.pks";
    static final char NUL = (char) 0;

    SSLContext sslContext;
    char[] keystorepass = "victor".toCharArray();

    InputScanner inputScanner;

    public ServerApp() {

        inputScanner = new InputScanner();
        Thread scannerThread = new Thread(inputScanner);
        scannerThread.start();
    }

    private void setupNetworking() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(getClass().getResourceAsStream(STORE_PATH), keystorepass);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keystorepass);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        setupNetworking();
        NewConnectionListener newConnectionListener = new NewConnectionListener();
        Thread newConnectionThread = new Thread(newConnectionListener);
        newConnectionThread.setName("ClientAcceptThread");
        newConnectionThread.start();


        if (DEBUG) {
            System.out.println("DEBUG = ON");
            test();
        }
    }

    private void test() {
    }

    public static void main(String[] args) {
        new ServerApp().start();
    }


    class NewConnectionListener implements Runnable {

        public void run() {
            System.out.println("Listening for new connections...");

            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            try {
                SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);

                while (true) {
                    SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                    Thread newClientThread = new Thread(new NewClient(sslSocket));
                    newClientThread.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    class NewClient implements Runnable, InputReceiver {

        SSLSocket sslSocket;
        PrintWriter writer;
        BufferedReader reader;

        public NewClient(SSLSocket sslSocket) {
            this.sslSocket = sslSocket;

            System.out.printf("Connected to client %s on port %d.\n", sslSocket.getInetAddress(), sslSocket.getPort());

            try {
                writer = new PrintWriter(sslSocket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputScanner.addInputReceiver(this);
        }

        private void handleMessage(String message) {
            if (DEBUG) {
                System.out.println("Message received: " + message.replace(String.valueOf(NUL), "(NUL)"));
                message = CommandParser.escapeString(message);
            }

            String launcherOutput;
            CommandParser cp = new CommandParser(message);
            if (cp.isParseSuccess()) {
                CommandProcess commandProcess = new CommandProcess(cp.getCmd(), cp.getFlags());

                launcherOutput = commandProcess.launch();
                if (launcherOutput != null) {
                    writer.print(launcherOutput);
                    writer.flush();
                } else {
                    writer.println(String.format("Command \'%s\' not found.", cp.getCmd()));
                    writer.flush();
                }
            } else {
                writer.println("Invalid command-message.");
                writer.flush();
            }
        }

        public void receive(String message) {
            writer.println(message);
            writer.flush();
        }

        public void run() {
            String message;

            try {
                while ((message = reader.readLine()) != null) {
                    handleMessage(message);
                }

            } catch (SocketException e) {
                System.out.printf("Client %s disconnected from port %d\n", sslSocket.getInetAddress(), sslSocket.getPort());

                try {
                    sslSocket.close();
                    reader.close();
                    writer.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
