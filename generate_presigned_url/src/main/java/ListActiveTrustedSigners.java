import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.security.AWSCredentials;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ListActiveTrustedSigners {

    String keyid;
    String seckey;

    public static void main(String[] args) throws CloudFrontServiceException, ServiceException {
        File file = new File("/home/bcp/.aws/credentials");
        System.out.println(file.exists());

        final ListActiveTrustedSigners app = new ListActiveTrustedSigners();
        app.readFile(file, "[default]");

        app.doit();
    }

    private void readFile(File file, String profile) {
        BufferedReader br = null;
        boolean correctProfile = false;
        try {
            br = new BufferedReader(new FileReader(file));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                if (!correctProfile && sCurrentLine != null && sCurrentLine.contains(profile)) {
                    correctProfile = true;
                } else if (correctProfile && sCurrentLine != null) {
                    if (sCurrentLine.contains("aws_access_key_id")) {
                        keyid = sCurrentLine.split(" = ")[1];
                    } else if (sCurrentLine.contains("aws_secret_access_key")) {
                        seckey = sCurrentLine.split(" = ")[1];
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doit() throws ServiceException, CloudFrontServiceException {
        //List active trusted signers for a private distribution
        CloudFrontService cloudFrontService = new CloudFrontService(new AWSCredentials(keyid, seckey));

        Distribution distribution = cloudFrontService.getDistributionInfo("EBZC9MLPINWF4");
        System.out.println("Active trusted signers: " + distribution.getActiveTrustedSigners());

        //Obtain one of your own (Self) keypair ids that can sign URLs for the distribution

        List selfKeypairIds = (List) distribution.getActiveTrustedSigners().get("Self");
        String keyPairId = (String) selfKeypairIds.get(0);
        System.out.println("Keypair ID: " + keyPairId);
    }
}
