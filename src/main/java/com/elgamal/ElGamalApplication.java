package com.elgamal;

import java.util.Scanner;

public class ElGamalApplication {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Text to Decrypt: ");
        String inputText = scanner.nextLine();
        System.out.println("Enter Key Length: ");
        int bitLength = Integer.parseInt(scanner.nextLine());

        while(!isPowerOfTwo(bitLength)){
            System.out.println("Enter a valid Key Length (64, 128, 256...): ");
            bitLength = Integer.parseInt(scanner.nextLine());
        }
        ElGamal elgamal = new ElGamal();
        final String elgamalCipherText = elgamal.elgamal(Mode.ENCRYPT, inputText,bitLength);
        System.out.println("Encrypted Text: "+elgamalCipherText);

        System.out.println("Decrypted Text: "+elgamal.elgamal(Mode.DECRYPT, elgamalCipherText,bitLength));
    }
    static boolean isPowerOfTwo(int n)
    {
        if (n == 0)
            return false;

        while (n != 1) {
            if (n % 2 != 0)
                return false;
            n = n / 2;
        }
        return true;
    }
}
