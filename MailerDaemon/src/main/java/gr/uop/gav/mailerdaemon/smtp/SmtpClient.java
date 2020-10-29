package gr.uop.gav.mailerdaemon.smtp;


import gr.uop.gav.mailerdaemon.exceptions.UnexpectedSmtpMessageException;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class SmtpClient {
    String target;
    String host;
    String mailFrom;
    String rcpTo;

    HashMap<String, String> headers;

    private Thread readingThread;
    private SmtpState state;
    private String rfcDate;

    public SmtpClient(String target) {
        this.target = target;
        this.headers = new HashMap<>();



        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        rfcDate = dateFormat.format(new Date(System.currentTimeMillis()));

        headers.put("Date", rfcDate);
        headers.put("Message-Id", "4354354352542");
        headers.put("Subject", "whatever");

        this.state = SmtpState.WAITING_FOR_READY;
    }

    public void run() throws Exception {
        // σύνδεση με τον smtp server στην θύρα 25
        Socket socket = new Socket(target, 25);
        socket.setKeepAlive(true);


        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        readingThread = new Thread() {

            @Override
            public void run() {
                try{
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(!isInterrupted()) {
                        String _msg = in.readLine();
                        System.out.println(_msg);
                        int smtpCode = Integer.parseInt(_msg.split(" ")[0]);
                        String smtpMessage = _msg.substring(_msg.indexOf(" ")+1);

                       // System.out.println(smtpCode + " => " + smtpMessage);

                        if(smtpCode == 220) {
                            if(state != SmtpState.WAITING_FOR_READY) {
                                throw new UnexpectedSmtpMessageException("Expected state to be WAITING_FOR_READY, " +
                                        "instead got " + state.name() + ". [Response: " + smtpCode + "]");
                            }

                            state = SmtpState.WAITING_FOR_HELO_OK;

                            writer.write("HELO arisstath.me\r\n");
                            writer.flush();
                        }

                        if(smtpCode == 250) {
                            if(state == SmtpState.WAITING_FOR_HELO_OK) {
                                state = SmtpState.WAITING_FOR_SENDER_OK;

                                writer.write("MAIL From: <test@arisstath.me>\r\n");
                                writer.flush();
                            }

                            if(state == SmtpState.WAITING_FOR_SENDER_OK) {
                                state = SmtpState.WAITING_FOR_RECIPIENT_OK;

                                writer.write("RCPT To: <test-avv43bqz1@srv1.mail-tester.com>\r\n");
                                writer.flush();
                            }

                            if(state == SmtpState.WAITING_FOR_RECIPIENT_OK) {
                                state = SmtpState.WAITING_FOR_DATA;

                                writer.write("DATA\r\n");
                                writer.flush();
                            }

                            if(state == SmtpState.WAITING_FOR_DELIVERY) {
                                socket.close();
                                interrupt();
                                System.out.println("== email sent ==");
                            }
                        }

                        if(smtpCode == 354) {
                            if(state != SmtpState.WAITING_FOR_DATA) {
                                throw new UnexpectedSmtpMessageException("Expected state to be WAITING_FOR_DATA, " +
                                        "instead got " + state.name() + ". [Response: " + smtpCode + "]");
                            }

                            state = SmtpState.WAITING_FOR_DELIVERY;
                            // Send the headers
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                String headerName = entry.getKey();
                                String headerValue = entry.getValue();

                                writer.write(headerName + ": " + headerValue + "\r\n");
                            }

                            writer.write("\r\n");
                            writer.write("Hello there\r\n");
                            writer.write("How are you\r\n");
                            writer.write(".\r\n");
                            writer.flush();

                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    interrupt();
                }

            }

        };
        readingThread.start();
    }
}
