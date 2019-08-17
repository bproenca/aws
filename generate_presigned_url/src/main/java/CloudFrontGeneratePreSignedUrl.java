import org.jets3t.service.CloudFrontService;
import org.jets3t.service.utils.ServiceUtils;

import java.io.FileInputStream;
import java.security.Security;


public class CloudFrontGeneratePreSignedUrl {
    private static String bucketName = "bcp-private-bucket";
    private static String objectKey = "saturno_contraluz_high.jpg";

    public static void main(String[] args) throws Exception {
        // Signed URLs for a private distribution
        // Note that Java only supports SSL certificates in DER format,
        // so you will need to convert your PEM-formatted file to DER format.
        // To do this, you can use openssl:
        // openssl pkcs8 -topk8 -nocrypt -in origin.pem -inform PEM -out new.der
        //    -outform DER
        // So the encoder works correctly, you should also add the bouncy castle jar
        // to your project and then add the provider.

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        String distributionDomain = "d10ekjgd2jkwtr.cloudfront.net";
        String privateKeyFilePath = "/d/aws/keypair/cf-private_key.der";
        String s3ObjectKey = "Java_1.png";
        String policyResourcePath = "http://" + distributionDomain + "/" + s3ObjectKey;
        String keyPairId = "K2TBDGJEUH5S52";

        // Convert your DER file into a byte array.

        byte[] derPrivateKey = ServiceUtils.readInputStreamToBytes(new
                FileInputStream(privateKeyFilePath));

        // Generate a "canned" signed URL to allow access to a
        // specific distribution and object

        String signedUrlCanned = CloudFrontService.signUrlCanned(
                "http://" + distributionDomain + "/" + s3ObjectKey, // Resource URL or Path
                keyPairId,     // Certificate identifier,
                // an active trusted signer for the distribution
                derPrivateKey, // DER Private key data
                ServiceUtils.parseIso8601Date("2019-11-14T22:20:00.000Z") // DateLessThan
        );
        System.out.println(signedUrlCanned);

        // Build a policy document to define custom restrictions for a signed URL.

        String policy = CloudFrontService.buildPolicyForSignedUrl(
                // Resource path (optional, can include '*' and '?' wildcards)
                policyResourcePath,
                // DateLessThan
                ServiceUtils.parseIso8601Date("2011-11-14T22:20:00.000Z"),
                // CIDR IP address restriction (optional, 0.0.0.0/0 means everyone)
                "0.0.0.0/0",
                // DateGreaterThan (optional)
                ServiceUtils.parseIso8601Date("2019-10-16T06:31:56.000Z")
        );

        // Generate a signed URL using a custom policy document.

        String signedUrl = CloudFrontService.signUrl(
                // Resource URL or Path
                "http://" + distributionDomain + "/" + s3ObjectKey,
                // Certificate identifier, an active trusted signer for the distribution
                keyPairId,
                // DER Private key data
                derPrivateKey,
                // Access control policy
                policy
        );
        System.out.println(signedUrl);
    }
}

//        CloudFrontUrlSigner.Protocol protocol = CloudFrontUrlSigner.Protocol.http;
//        String distributionDomain = "d1b2c3a4g5h6.cloudfront.net";
//        File privateKeyFile = new File("/path/to/cfcurlCloud/rsa-private-key.pem");
//        String s3ObjectKey = "a/b/images.jpeg";
//        String keyPairId = "APKAJCEOKRHC3XIVU5NA";
//        Date dateLessThan = DateUtils.parseISO8601Date("2012-11-14T22:20:00.000Z");
//        Date dateGreaterThan = DateUtils.parseISO8601Date("2011-11-14T22:20:00.000Z");
//        String ipRange = "192.168.0.1/24";
//
//        String url1 = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
//                protocol, distributionDomain, privateKeyFile,
//                s3ObjectKey, keyPairId, dateLessThan);
//
//        String url2 = CloudFrontUrlSigner.getSignedURLWithCustomPolicy(
//                protocol, distributionDomain, privateKeyFile,
//                s3ObjectKey, keyPairId, dateLessThan,
//                dateGreaterThan, ipRange);