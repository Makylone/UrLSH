package com.Makylone.UrLSH.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    // The "Alphabet": The 62 unique symbols we use to represent numbers.
    // 0-9 (10 chars) + a-z (26 chars) + A-Z (26 chars) = 62 characters
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    // The Base: The number of unique values a single digit can represent.
    private static final int BASE = ALPHABET.length(); // 62

    /**
     * ENCODING: Convert a Database ID (Long) -> Short Code (String)
     * Math Strategy: Repeated Modulo and Division (Change of Base)
     */
    public String encode(long value) {
        // Edge case: If ID is 0, just return the first character "0"
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();

        // While we still have value left to process...
        while (value > 0) {
            // Step 1: Modulo (%) gives us the "remainder".
            // Ideally, this maps to the index of the character we want.
            // Example: If value is 63. 63 % 62 = 1. The character at index 1 is '1'.
            int remainder = (int) (value % BASE);

            // Step 2: Append the character that corresponds to that remainder.
            sb.append(ALPHABET.charAt(remainder));

            // Step 3: Divide (/) moves us to the next "place value".
            // Integer division discards the remainder we just processed.
            // Example: 63 / 62 = 1. We have 1 left to process.
            value /= BASE;
        }

        // We built the string backwards (Least Significant Digit first), so we must reverse it.
        return sb.reverse().toString();
    }

    /**
     * DECODING: Convert Short Code (String) -> Database ID (Long)
     * Math Strategy: Polynomial Expansion (Horner's Method)
     * Formula: result = result * 62 + digit_value
     */
    public long decode(String str) {
        long result = 0;

        // Loop through each character of the short code (e.g., "1C")
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            // Step 1: Find the numerical value of the character.
            // '0' -> 0, 'a' -> 10, 'Z' -> 61
            int digit = ALPHABET.indexOf(c);
            
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + c);
            }

            // Step 2: Accumulate the result.
            // This is like reading "123" in decimal:
            // 1. Start with 0. 
            // 2. See '1': (0 * 10) + 1 = 1
            // 3. See '2': (1 * 10) + 2 = 12
            // 4. See '3': (12 * 10) + 3 = 123
            // We do the same here, but multiply by 62 instead of 10.
            result = result * BASE + digit;
        }
        return result;
    }
}