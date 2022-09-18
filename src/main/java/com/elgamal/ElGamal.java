package com.elgamal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ElGamal {
    private static final String PROVIDER = "BC";
    private SecureRandom random = null;
    private KeyPair keypair = null;
    private Cipher xCipher = null;
    private Cipher sCipher = null;
    private IvParameterSpec sIvSpec = null;
    private Key sKey = null;
    private byte[] keyBlock = null;

    private Base64 base64 = new Base64();

    // Provider
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    // Constructor
    public ElGamal() {
        random = new SecureRandom();
        keypair = createKeyPair();

        // Get instances
        xCipher = getCipherInstance("ElGamal/None/PKCS1Padding");
        sCipher = getCipherInstance("AES/CTR/NoPadding");
    }

    /**
     * Generate the ElGamal Key Pair Note that some tutorials have BC and SC, that
     * means provider, BC is BouncyCastle and SC is SpongyCastle provider.
     *
     * @return KeyPair object
     */
    private KeyPair createKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("ELGamal", PROVIDER);
            generator.initialize(512, random);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    /**
     * Symmetric key/iv wrapping step
     *
     * @return
     */
    private Cipher getCipherInstance(final String cipherInstance) {
        try {
            return Cipher.getInstance(cipherInstance, PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    public String elgamal(Mode mode, String input, int bitLength) {
        try {

            if (mode == Mode.ENCRYPT) {

                // Create the symmetric key and iv
                sKey = createKeyForAES(bitLength, random);
                sIvSpec = createCtrIvForAES(0, random);
                xCipher.init(Cipher.ENCRYPT_MODE, keypair.getPublic(), random);
                keyBlock = xCipher.doFinal(packKeyAndIv(sKey, sIvSpec));

                // Encryption step
                sCipher.init(Cipher.ENCRYPT_MODE, sKey, sIvSpec);
                byte[] cipherText = sCipher.doFinal(input.getBytes());


                return base64.encodeToString(cipherText);
            }

            if (mode == Mode.DECRYPT) {
                byte[] inputBytes = base64.decode(input.getBytes(StandardCharsets.UTF_8));

                // Symmetric key/iv unwrapping step
                xCipher.init(Cipher.DECRYPT_MODE, keypair.getPrivate());
                Object[] keyIv = unpackKeyAndIV(xCipher.doFinal(keyBlock));

                // Decryption step
                sCipher.init(Cipher.DECRYPT_MODE, (Key) keyIv[0], (IvParameterSpec) keyIv[1]);
                byte[] plainText = sCipher.doFinal(inputBytes);

                return new String(plainText, StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }

    /**
     * Create a key for use with AES.
     *
     * @param bitLength
     * @param random
     * @return an AES key.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static SecretKey createKeyForAES(int bitLength, SecureRandom random) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES", PROVIDER);
            generator.init(bitLength, random);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    /**
     * Create an IV suitable for using with AES in CTR mode. The IV will be composed
     * of 4 bytes of message number, 4 bytes of random data, and a counter of 8
     * bytes.
     *
     * @param messageNumber the number of the message.
     * @param random        a source of randomness
     * @return an initialized IvParameterSpec
     */
    public static IvParameterSpec createCtrIvForAES(int messageNumber, SecureRandom random) {
        byte[] ivBytes = new byte[16];
        // initially randomize
        random.nextBytes(ivBytes);
        // set the message number bytes
        ivBytes[0] = (byte) (messageNumber >> 24);
        ivBytes[1] = (byte) (messageNumber >> 16);
        ivBytes[2] = (byte) (messageNumber >> 8);
        ivBytes[3] = (byte) (messageNumber >> 0);
        // set the counter bytes to 1
        for (int i = 0; i != 7; i++) {
            ivBytes[8 + i] = 0;
        }
        ivBytes[15] = 1;
        return new IvParameterSpec(ivBytes);
    }

    /**
     * packKeyAndIv
     *
     * @param key
     * @param ivSpec
     * @return
     * @throws IOException
     */
    private static byte[] packKeyAndIv(Key key, IvParameterSpec ivSpec) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bOut.write(ivSpec.getIV());
        bOut.write(key.getEncoded());
        return bOut.toByteArray();
    }

    /**
     * unpackKeyAndIV
     *
     * @param data
     * @return
     */
    private static Object[] unpackKeyAndIV(byte[] data) {
        return new Object[] { new SecretKeySpec(data, 16, data.length - 16, "AES"), new IvParameterSpec(data, 0, 16) };
    }

    /**
     * Returns public key as base 64 for viewing purposes
     *
     * @return base64 public key
     */
    public String getPublicKeyStr() {
        return base64.encodeToString(keypair.getPublic().getEncoded());
    }

    /**
     * Returns private key as base 64 for viewing purposes
     *
     * @return base64 private key
     */
    public String getPrivateKeyStr() {
        return base64.encodeToString(keypair.getPrivate().getEncoded());
    }

    /**
     * Get key block length
     *
     * @return integer
     */
    public int getKeyBlockLength() {
        return keyBlock.length;
    }

}
