package com.muoq.main;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerApp {

    static final int PORT = 8080;

    SSLContext sslContext;
    char[] keystorepass = "victor1406".toCharArray();

    private void setupNetworking() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream("C:\\Users\\victo\\.keystore\\selfsigned.pks"), keystorepass);

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
        newConnectionThread.start();
    }

    public static void main(String[] args) {
        new ServerApp().start();
    }


    class NewConnectionListener implements Runnable {

        public void run() {

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


    class NewClient implements Runnable {

        SSLSocket sslSocket;
        PrintWriter writer;
        BufferedReader reader;

        public NewClient(SSLSocket sslSocket) {
            this.sslSocket = sslSocket;

            System.out.printf("Connected to client %s on port %d.\n", sslSocket.getInetAddress(), sslSocket.getPort());

            try {
                writer = new PrintWriter(sslSocket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                System.out.println("reader instantiated");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(String message) {
            System.out.println("Message received: " + message);
            writer.println("Hello. This is what you wrote me: " + message + ".");
        }

        public void run() {
            String message;

            try {
                System.out.println("reading");
                while ((message = reader.readLine()) != null) {
                    handleMessage(message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
