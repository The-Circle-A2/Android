package com.pedro.tasks;


import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSAIntegrityHandler {

    public static void main(String[] args) throws Exception{
        String test = "Sample text";

        String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDJgK1ROWsIOMonYesOl7YLOi2pnDmKYt7oF0OaCeIa64tVjEbdjjSVwJIN+5Rldxp/ndiBhnTz+Pe1tTXKyWShbQp0bHRJ/gwLpUIoVXIYdKnolGLCGMcj48HPGVBOoENAL5Y8elEMsDEpLWUGs7YBD8I725IYAFnLkgaCwgShN3Vb0FikB2fujeiWR7jmpVevnpSwc36j17eZKjZvmDaqNWN9SYi1gyu77M+B+T2upybM1t9/nBTAq9+NzxGv2M7GjLVuL08aVJzB" +
                "6MAFZ9/d18W3XqmIXAQJBY3Zt1opex0LTp1u5rklNX786GSjffwXN67deDmoYy3zgp5xGvCbAgMBAAECggEATMbdwwQYtyJETGsvz/iYYYDCY0zmU8dGXbJ3jJrVbyJujaMMYWDMwn6EaBWCvoJ6M0PmebvMliU35SFtpJ3F3QgaIrAsQwEsf8dxBMNB3PWew8K+OmyMfC1M7GcdxUelODCpktOcTDRaFvF3++y/nXnurTYFXWXM0RcLqgjZEL/Nmjz0Ey9/9r/TdtL55zeSHwzwLa07DhqateRJxwijc/nEsuy1zbhhZC8Pg5VANN4J" +
                "PUfdbdk+jxlPNTW8l3+9P8oBnEjJq7D6asGuEpO8CWyK2xAHOEVcm7huXABTp1klZEEXq6XXm7heXXXOTSEsKocatVvxQT5i0tWXS89FQQKBgQD+1trZiFGb2fMNpgH72rMBl3IhuB3y50ZYKGiL4y6Zj6UwOZq+dDehbFy6oECt4RcyBGSMprJcO8iqdZRglL5sjibF1d6GR/MauZucNc8sjiHzViW83RhgTbGNN3Oc6ES8wFcQV+yqGd4Yyyf5Xvji9aGydFhHPEgxGOVXhXc/ewKBgQDKa6GPogOopbubmyA9ZfvwWSjmTCuBbhU2" +
                "y/+UrYrbB3Y0bQ4irFfsZSYPQdB4pyqgGV+6y0/jD5jTp8Q+VWG1a1O0NooID5FaN4AC83MHWDFf7oPfiIO8U0AiLHFdHaaQtVsuBtCGlfpcxky/1g+o4uSN1tgYbJSoDuzu0mS5YQKBgC0FVCB+HrzD4laU72DJIDq1wKoIVvIkvZ6xdNbRydfDMtG0O4xOB83y0Ob/jK2rSGfDsufOQ1BcF3VV1GLDpGiVLHBi+i8K4v3jsIQGVZVvfrv4ii41eSr6TtW96CTeO9aEvFzcy6i5bS1AJs29Y4blEi3us/HRyocx+MJfh9OlAoGAPL9Y" +
                "WRayKmayitxfLI3ysgrP721kkQ9MZomKRkBer6TpoIop99Xo/RTN+z4xJkRk0m9K/3AMpYQExWBP0LUnAZ8YG7lSa2DgqkaFsF0HZGjfSH74LJl3j4kcciUMUHmZh0wl9QYuhnV0EWuhlODVCkYteTPE3m+XnWf9Sx7xA4ECgYB7KH/k0+yHXs5YUgmmZziGrfWu8PLK+EwzaHBdJdAfac50GyNpD3TotgYY1T7qWouUxYYY8MT7K7U31LpKMJT+5Pb6DhVF40FMiLFqudh8Yd95Hgphf9PDEqj5xF0UwzQB2zuuto19fUrjaDdxEeep" +
                "NTm1kF45Wf51rvkqlG316w==-----END PRIVATE KEY-----";

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyYCtUTlrCDjKJ2HrDpe2CzotqZw5imLe6BdDmgniGuuLVYxG3Y40lcCSDfuUZXcaf53YgYZ08/j3tbU1yslkoW0KdGx0Sf4MC6VCKFVyGHSp6JRiwhjHI+PBzxlQTqBDQC+WPHpRDLAxKS1lBrO2\n" +
                "AQ/CO9uSGABZy5IGgsIEoTd1W9BYpAdn7o3olke45qVXr56UsHN+o9e3mSo2b5g2qjVjfUmItYMru+zPgfk9rqcmzNbff5wUwKvfjc8Rr9jOxoy1bi9PGlScwejABWff3dfFt16piFwECQWN2bdaKXsdC06dbua5JTV+/Ohko338Fzeu3Xg5qGMt84KecRrwmwIDAQAB";

        String signature = signWithSHA256(test, privateKey);
        System.out.println(signature);

        boolean bool = verify(signature, publicKey);
    }

    private static String signWithSHA256(String test, String strPk) throws Exception {
        String realPK = strPk.replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\n", "");

        byte[] b1 = Base64.getDecoder().decode(realPK);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(kf.generatePrivate(spec));
        privateSignature.update(test.getBytes("UTF-8"));
        byte[] s = privateSignature.sign();
        return Base64.getEncoder().encodeToString(s);
    }

    private static boolean verify(String signature, String publicKey) throws Exception {
        return false;
    }



}
