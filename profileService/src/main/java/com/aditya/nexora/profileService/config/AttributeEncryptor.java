package com.aditya.nexora.profileService.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Encrypts persisted attribute values with AES-GCM. The IV is prepended to the
 * ciphertext so it is available when the value is read back from the database.
 */
@Converter
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKeySpec key;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * The key must be Base64-encoded AES key material (16, 24, or 32 bytes).
     */
    public AttributeEncryptor(@Value("${api.security.encryption-key}") String base64SecretKey) {
        this.key = new SecretKeySpec(decodeAndValidateKey(base64SecretKey), KEY_ALGORITHM);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedValue = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedValue, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedValue, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            byte[] encryptedValue = Base64.getDecoder().decode(dbData);
            if (encryptedValue.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Encrypted attribute is missing ciphertext");
            }

            byte[] iv = Arrays.copyOfRange(encryptedValue, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(encryptedValue, IV_LENGTH_BYTES, encryptedValue.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt attribute", e);
        }
    }

    private static byte[] decodeAndValidateKey(String base64SecretKey) {
        final byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64SecretKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("api.security.encryption-key must be Base64-encoded", e);
        }

        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("api.security.encryption-key must decode to 16, 24, or 32 bytes");
        }
        return keyBytes;
    }
}
