package com.aditya.nexora.profileService.config;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttributeEncryptorTest {

    private static final String KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    private final AttributeEncryptor encryptor = new AttributeEncryptor(KEY);

    @Test
    void encryptsAndDecryptsAccessToken() {
        String token = "gho_exampleGitHubAccessToken";

        String encrypted = encryptor.convertToDatabaseColumn(token);

        assertNotEquals(token, encrypted);
        assertEquals(token, encryptor.convertToEntityAttribute(encrypted));
    }

    @Test
    void usesDifferentCiphertextForEachEncryption() {
        String token = "gho_exampleGitHubAccessToken";

        assertNotEquals(
                encryptor.convertToDatabaseColumn(token),
                encryptor.convertToDatabaseColumn(token)
        );
    }

    @Test
    void rejectsModifiedCiphertext() {
        String encrypted = encryptor.convertToDatabaseColumn("gho_exampleGitHubAccessToken");
        String modified = encrypted.substring(0, encrypted.length() - 2) + "AA";

        assertThrows(IllegalStateException.class, () -> encryptor.convertToEntityAttribute(modified));
    }

    @Test
    void preservesNullValues() {
        assertNull(encryptor.convertToDatabaseColumn(null));
        assertNull(encryptor.convertToEntityAttribute(null));
    }
}
