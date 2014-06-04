package fr.xebia.conference.companion.core.utils;

import android.nfc.Tag;

public class NfcFormatter {


    public static String formatNfcId(Tag tag) {
        String hexString = bytesToHexString(tag.getId()) + "9000";
        return hexString.substring(2, hexString.length())
                .toUpperCase()
                .replaceAll("(..)", "$0 ").trim();
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }
}
