package gr.uop.gav.mailerdaemon.util;


import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import javax.naming.directory.InitialDirContext;
import java.util.Arrays;
import java.util.Comparator;

public class WebUtil {


    public static MXRecord[] resolveMxRecords(String domain) {
        MXRecord[] records = new MXRecord[]{};

        try {
            /*
            Σύμφωνα με το RFC5321, δίνουμε προτεραιότητα στα mx records με την μικρότερο αριθμό "προτίμησης"
            περισσότερες πληροφορίες https://en.wikipedia.org/wiki/MX_record
            */
            Lookup lookup = new Lookup(domain, Type.MX);
            Record[] mxRecords = lookup.run();

            // Θα κάνουμε sort to records array με αύξουσα σειρά
            Arrays.sort(mxRecords, Comparator.comparingInt(record -> ((MXRecord) record).getPriority()));

            records = new MXRecord[mxRecords.length];

            for(int i = 0; i < mxRecords.length; i++) {
                records[i] = (MXRecord) mxRecords[i];
            }

            return records;
        } catch (Exception ex) {
            ex.printStackTrace();
            return records;
        }

    }
}
