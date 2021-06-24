package com.pedro.tasks;


import com.pedro.rtmp.flv.signature.PrivateKeyGetter;

import org.jetbrains.annotations.NotNull;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSAIntegrityHandler implements PrivateKeyGetter {

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDJgK1ROWsIOMon\n" +
            "YesOl7YLOi2pnDmKYt7oF0OaCeIa64tVjEbdjjSVwJIN+5Rldxp/ndiBhnTz+Pe1\n" +
            "tTXKyWShbQp0bHRJ/gwLpUIoVXIYdKnolGLCGMcj48HPGVBOoENAL5Y8elEMsDEp\n" +
            "LWUGs7YBD8I725IYAFnLkgaCwgShN3Vb0FikB2fujeiWR7jmpVevnpSwc36j17eZ\n" +
            "KjZvmDaqNWN9SYi1gyu77M+B+T2upybM1t9/nBTAq9+NzxGv2M7GjLVuL08aVJzB\n" +
            "6MAFZ9/d18W3XqmIXAQJBY3Zt1opex0LTp1u5rklNX786GSjffwXN67deDmoYy3z\n" +
            "gp5xGvCbAgMBAAECggEATMbdwwQYtyJETGsvz/iYYYDCY0zmU8dGXbJ3jJrVbyJu\n" +
            "jaMMYWDMwn6EaBWCvoJ6M0PmebvMliU35SFtpJ3F3QgaIrAsQwEsf8dxBMNB3PWe\n" +
            "w8K+OmyMfC1M7GcdxUelODCpktOcTDRaFvF3++y/nXnurTYFXWXM0RcLqgjZEL/N\n" +
            "mjz0Ey9/9r/TdtL55zeSHwzwLa07DhqateRJxwijc/nEsuy1zbhhZC8Pg5VANN4J\n" +
            "PUfdbdk+jxlPNTW8l3+9P8oBnEjJq7D6asGuEpO8CWyK2xAHOEVcm7huXABTp1kl\n" +
            "ZEEXq6XXm7heXXXOTSEsKocatVvxQT5i0tWXS89FQQKBgQD+1trZiFGb2fMNpgH7\n" +
            "2rMBl3IhuB3y50ZYKGiL4y6Zj6UwOZq+dDehbFy6oECt4RcyBGSMprJcO8iqdZRg\n" +
            "lL5sjibF1d6GR/MauZucNc8sjiHzViW83RhgTbGNN3Oc6ES8wFcQV+yqGd4Yyyf5\n" +
            "Xvji9aGydFhHPEgxGOVXhXc/ewKBgQDKa6GPogOopbubmyA9ZfvwWSjmTCuBbhU2\n" +
            "y/+UrYrbB3Y0bQ4irFfsZSYPQdB4pyqgGV+6y0/jD5jTp8Q+VWG1a1O0NooID5Fa\n" +
            "N4AC83MHWDFf7oPfiIO8U0AiLHFdHaaQtVsuBtCGlfpcxky/1g+o4uSN1tgYbJSo\n" +
            "Duzu0mS5YQKBgC0FVCB+HrzD4laU72DJIDq1wKoIVvIkvZ6xdNbRydfDMtG0O4xO\n" +
            "B83y0Ob/jK2rSGfDsufOQ1BcF3VV1GLDpGiVLHBi+i8K4v3jsIQGVZVvfrv4ii41\n" +
            "eSr6TtW96CTeO9aEvFzcy6i5bS1AJs29Y4blEi3us/HRyocx+MJfh9OlAoGAPL9Y\n" +
            "WRayKmayitxfLI3ysgrP721kkQ9MZomKRkBer6TpoIop99Xo/RTN+z4xJkRk0m9K\n" +
            "/3AMpYQExWBP0LUnAZ8YG7lSa2DgqkaFsF0HZGjfSH74LJl3j4kcciUMUHmZh0wl\n" +
            "9QYuhnV0EWuhlODVCkYteTPE3m+XnWf9Sx7xA4ECgYB7KH/k0+yHXs5YUgmmZziG\n" +
            "rfWu8PLK+EwzaHBdJdAfac50GyNpD3TotgYY1T7qWouUxYYY8MT7K7U31LpKMJT+\n" +
            "5Pb6DhVF40FMiLFqudh8Yd95Hgphf9PDEqj5xF0UwzQB2zuuto19fUrjaDdxEeep\n" +
            "NTm1kF45Wf51rvkqlG316w==\n" +
            "-----END PRIVATE KEY-----\n";

    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyYCtUTlrCDjKJ2HrDpe2\n" +
            "CzotqZw5imLe6BdDmgniGuuLVYxG3Y40lcCSDfuUZXcaf53YgYZ08/j3tbU1yslk\n" +
            "oW0KdGx0Sf4MC6VCKFVyGHSp6JRiwhjHI+PBzxlQTqBDQC+WPHpRDLAxKS1lBrO2\n" +
            "AQ/CO9uSGABZy5IGgsIEoTd1W9BYpAdn7o3olke45qVXr56UsHN+o9e3mSo2b5g2\n" +
            "qjVjfUmItYMru+zPgfk9rqcmzNbff5wUwKvfjc8Rr9jOxoy1bi9PGlScwejABWff\n" +
            "3dfFt16piFwECQWN2bdaKXsdC06dbua5JTV+/Ohko338Fzeu3Xg5qGMt84KecRrw\n" +
            "mwIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";

    private PrivateKey privateKey;

    public static void main(String[] args) throws Exception{
        String message = "Hallo";

        // avoid static
        RSAIntegrityHandler handler = new RSAIntegrityHandler();

        // call sign func
        byte[] signature = handler.signWithSHA256(message, handler);
        System.out.println("Signature: " + Base64.getEncoder().encodeToString(signature));

        // call verification func
        boolean bool = handler.verify(message, Base64.getEncoder().encodeToString(signature), PUBLIC_KEY);
        System.out.println(bool);
    }

    private byte[] signWithSHA256(String test, PrivateKeyGetter privateKeyGetter) throws Exception {

        if (privateKey == null) {
            privateKey = privateKeyGetter.getPrivateKey();
        }

        // hash the message
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(test.getBytes());

        // sign the message with the private key and return it
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return encryptCipher.doFinal(encodedhash);
    }

    private boolean verify(String message, String signature, String PUBLIC_KEY) throws Exception {
        // strip the public key and create a PublicKey object
        String realPublicKey = PUBLIC_KEY.replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\n", "");
        byte[] byteArray = Base64.getDecoder().decode(realPublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteArray);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey key = kf.generatePublic(spec);

        // decrypt the code with the public key
        byte[] bytes = Base64.getDecoder().decode(signature);
        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, key);
        String hash = Base64.getEncoder().encodeToString(decriptCipher.doFinal(bytes));

        // hash the message and encode it to string
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(message.getBytes());
        String hashBuffer = Base64.getEncoder().encodeToString(encodedhash);

        System.out.println(hashBuffer);
        System.out.println(hash);

        // compare the two hashes
        if (hash.equals(hashBuffer)){
            // signature is correct, message is correct
            return true;
        }
        if (!(hash.equals(hashBuffer))){
            // the signature is not correct and/or the message is altered
            // normally we need to call the logservice here
            // also we would stop the server.
            return false;
        }

        return false;
    }


    @NotNull
    @Override
    public PrivateKey getPrivateKey() {
        String privateKeyPKCS8 = PRIVATE_KEY;

        if (privateKeyPKCS8.isEmpty()) {
            throw new IllegalStateException("PRIVATE_KEY can not be gotten when it's not stored. User shouldn't be in this screen yet!");
        }

        String reducedPrivateKey = privateKeyPKCS8
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "")
                .replaceAll("\\s+","");

        KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(reducedPrivateKey));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Something went wrong while parsing the user's private key!", e);
        }
    }
}
