package gr.uop.gav.mailerdaemon;

import gr.uop.gav.mailerdaemon.smtp.SmtpClient;
import gr.uop.gav.mailerdaemon.util.WebUtil;
import org.xbill.DNS.MXRecord;

public class MailerDaemon {


    public static void main(String[] args) {
        for(MXRecord record : WebUtil.resolveMxRecords("yahoo.com")) {
            try {
                System.out.println("Mailer connecting to : " + record.getTarget());

                new SmtpClient(record.getTarget().toString().substring(0, record.getTarget().toString().length()-1)).run();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
