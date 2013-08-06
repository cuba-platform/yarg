/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.xlsx;

public final class XlsxUtils {
    private XlsxUtils() {
    }

    public static int getNumberFromColumnReference(String columnReference) {
        int sum = 0;

        for (int i = 0; i < columnReference.length(); i++) {
            char c = columnReference.charAt(i);
            int number = ((int) c) - 64 - 1;

            int pow = columnReference.length() - i - 1;
            sum += Math.pow(26, pow) * (number + 1);
        }
        return sum;
    }

    public static String getColumnReferenceFromNumber(int number) {
        int remain = 0;
        StringBuilder ref = new StringBuilder();
        do {

            remain = (number - 1) % 26;
            number = (number - 1) / 26;

            ref.append((char) (remain + 64 + 1));
        } while (number > 0);

        return ref.reverse().toString();
    }
}
