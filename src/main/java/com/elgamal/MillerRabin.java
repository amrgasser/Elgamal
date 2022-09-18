package com.elgamal;

import java.util.Scanner;
import java.util.Random;
import java.math.BigInteger;


public class MillerRabin {

    // Just used to test out our MillerRabinTest

    public static boolean FermatTest(BigInteger n, Random r) {

        // Ensures that temp > 1 and temp < n.
        BigInteger temp = BigInteger.ZERO;
        do {
            temp = new BigInteger(n.bitLength()-1, r);
        } while (temp.compareTo(BigInteger.ONE) <= 0);

        // Just calculate temp^*(n-1) mod n
        BigInteger ans = temp.modPow(n.subtract(BigInteger.ONE), n);

        // Return true iff it passes the Fermat Test!
        return (ans.equals(BigInteger.ONE));
    }

    private static boolean MyMillerRabin(BigInteger n, Random r) {

        // Ensures that temp > 1 and temp < n.
        BigInteger temp = BigInteger.ZERO;
        do {
            temp = new BigInteger(n.bitLength()-1, r);
        } while (temp.compareTo(BigInteger.ONE) <= 0);

        // Screen out n if our random number happens to share a factor with n.
        if (!n.gcd(temp).equals(BigInteger.ONE)) return false;

        // For debugging, prints out the integer to test with.
        //System.out.println("Testing with " + temp);

        BigInteger base = n.subtract(BigInteger.ONE);
        BigInteger TWO = new BigInteger("2");

        // Figure out the largest power of two that divides evenly into n-1.
        int k=0;
        while ( (base.mod(TWO)).equals(BigInteger.ZERO)) {
            base = base.divide(TWO);
            k++;
        }

        // This is the odd value r, as described in our text.
        //System.out.println("base is " + base);

        BigInteger curValue = temp.modPow(base,n);

        // If this works out, we just say it's prime.
        if (curValue.equals(BigInteger.ONE))
            return true;

        // Otherwise, we will check to see if this value successively
        // squared ever yields -1.
        for (int i=0; i<k; i++) {

            // We need to really check n-1 which is equivalent to -1.
            if (curValue.equals(n.subtract(BigInteger.ONE)))
                return true;

                // Square this previous number - here I am just doubling the
                // exponent. A more efficient implementation would store the
                // value of the exponentiation and square it mod n.
            else
                curValue = curValue.modPow(TWO, n);
        }

        // If none of our tests pass, we return false. The number is
        // definitively composite if we ever get here.
        return false;
    }

    public static boolean MillerRabin(BigInteger n, int numTimes) {

        Random r = new Random();

        // Run Miller-Rabin numTimes number of times.
        for (int i=0; i<numTimes; i++)
            if (!MyMillerRabin(n,r)) return false;

        // If we get here, we assume n is prime. This will be incorrect with
        // a probability no greater than 1/4^numTimes.
        return true;
    }
}