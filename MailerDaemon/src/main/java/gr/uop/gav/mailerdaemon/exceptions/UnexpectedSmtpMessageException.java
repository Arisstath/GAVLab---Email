package gr.uop.gav.mailerdaemon.exceptions;

public class UnexpectedSmtpMessageException extends Exception {

    public UnexpectedSmtpMessageException(String s) {
        super(s);
    }
}
