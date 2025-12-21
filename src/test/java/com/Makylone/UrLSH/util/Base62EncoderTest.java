package com.Makylone.UrLSH.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void shouldEncodeSpecificValuesCorrectly() {
        // Test Case 1: The number 0 should be "0"
        assertThat(encoder.encode(0)).isEqualTo("0");

        // Test Case 2: The number 1 should be "1"
        assertThat(encoder.encode(1)).isEqualTo("1");

        // Test Case 3: The number 61 should be "Z" (Last single digit)
        assertThat(encoder.encode(61)).isEqualTo("Z");

        // Test Case 4: The number 62 should be "10" (Base 62 rollover)
        assertThat(encoder.encode(62)).isEqualTo("10");
        
        // Test Case 5: A larger arbitrary number
        // 1005 -> 1005 / 62 = 16 rem 13 ('d')
        //           16 / 62 = 0  rem 16 ('g')
        // result reversed -> "gd"
        assertThat(encoder.encode(1005)).isEqualTo("gd");
    }

    @Test
    void shouldDecodeSpecificValuesCorrectly() {
        assertThat(encoder.decode("0")).isEqualTo(0);
        assertThat(encoder.decode("Z")).isEqualTo(61);
        assertThat(encoder.decode("10")).isEqualTo(62);
        assertThat(encoder.decode("gd")).isEqualTo(1005);
    }

    @Test
    void shouldVerifyRoundTrip() {
        // Logic Check: If I encode X, then decode the result, I should get X back.
        long originalId = 999_999L;
        
        String shortCode = encoder.encode(originalId);
        long decodedId = encoder.decode(shortCode);

        assertThat(decodedId).isEqualTo(originalId);
    }

    @Test
    void shouldThrowException_WhenInputInvalid() {
        // If someone tries to decode a symbol not in our alphabet (like space or &)
        // logic expects an Exception.
        assertThrows(IllegalArgumentException.class, () -> {
            encoder.decode("Abc&1"); 
        });
    }
}
