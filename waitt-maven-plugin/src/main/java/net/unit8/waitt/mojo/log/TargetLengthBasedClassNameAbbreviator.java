package net.unit8.waitt.mojo.log;

/**
 *
 * Original code from
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * @author kawasima
 */
public class TargetLengthBasedClassNameAbbreviator {
    final int targetLength;

    public TargetLengthBasedClassNameAbbreviator(int targetLength) {
        this.targetLength = targetLength;
    }

    public String abbreviate(String fqClassName) {
        if (fqClassName == null) {
            return "<unknown>";
        }
        StringBuilder buf = new StringBuilder(targetLength);

        int inLen = fqClassName.length();
        if (inLen < targetLength) {
            return fqClassName;
        }

        int[] dotIndexesArray = new int[16];
        // a.b.c contains 2 dots but 2+1 parts.
        // see also http://jira.qos.ch/browse/LBCLASSIC-110
        int[] lengthArray = new int[16 + 1];

        int dotCount = computeDotIndexes(fqClassName, dotIndexesArray);

        // if there are no dots then abbreviation is not possible
        if (dotCount == 0) {
            return fqClassName;
        }
        computeLengthArray(fqClassName, dotIndexesArray, lengthArray, dotCount);
        for (int i = 0; i <= dotCount; i++) {
            if (i == 0) {
                buf.append(fqClassName.substring(0, lengthArray[i] - 1));
            } else {
                buf.append(fqClassName.substring(dotIndexesArray[i - 1],
                        dotIndexesArray[i - 1] + lengthArray[i]));
            }
        }

        return buf.toString();
    }

    static int computeDotIndexes(final String className, int[] dotArray) {
        int dotCount = 0;
        int k = 0;
        while (true) {
            // ignore the $ separator in our computations. This is both convenient
            // and sensible.
            k = className.indexOf('.', k);
            if (k != -1 && dotCount < 16) {
                dotArray[dotCount] = k;
                dotCount++;
                k++;
            } else {
                break;
            }
        }
        return dotCount;
    }

    void computeLengthArray(final String className, int[] dotArray,
                            int[] lengthArray, int dotCount) {
        int toTrim = className.length() - targetLength;

        int len;
        for (int i = 0; i < dotCount; i++) {
            int previousDotPosition = -1;
            if (i > 0) {
                previousDotPosition = dotArray[i - 1];
            }
            int available = dotArray[i] - previousDotPosition - 1;

            if (toTrim > 0) {
                len = (available < 1) ? available : 1;
            } else {
                len = available;
            }
            toTrim -= (available - len);
            lengthArray[i] = len + 1;
        }

        int lastDotIndex = dotCount - 1;
        lengthArray[dotCount] = className.length() - dotArray[lastDotIndex];
    }
}
