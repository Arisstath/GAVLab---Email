package gr.uop.gav.mailerdaemon.smtp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class SmtpClient {
    String target;
    String host;
    String mailFrom;
    String rcpTo;

    HashMap<String, String> headers;

    private Thread readingThread;

    public SmtpClient(String target) {
        this.target = target;
    }

    public void run() throws Exception {
        // σύνδεση με τον smtp server στην θύρα 25
        Socket socket = new Socket(target, 25);
        socket.setKeepAlive(true);

        readingThread = new Thread() {

            @Override
            public void run() {
                try{
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(!isInterrupted()) {
                        String _msg = in.readLine();

                        int smtpCode = Integer.parseInt(_msg.split(" ")[0]);
                        String smtpMessage = _msg.substring(_msg.indexOf(" ")+1);
                        System.out.println(smtpMessage);

                        if(smtpCode == 220) {
                            System.out.println("SMTP server is ready!");


                        }

                    }
                } catch (Exception ex) {
                    interrupt();
                }

            }

        };
        readingThread.start();
    }
}
