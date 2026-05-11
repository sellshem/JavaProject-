package kz.qazaqlearn.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts String values to/from encrypted database representation.
 * Uses AES/GCM encryption via {@link CryptoService}.
 * 
 * <p>For backward compatibility, if decryption fails (e.g., legacy plaintext data),
 * the original value is returned and a warning is logged. The next update will
 * store the value in encrypted form.</p>
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(EncryptedStringConverter.class);

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : CryptoService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return CryptoService.decrypt(dbData);
        } catch (Exception e) {
            // Likely legacy plaintext data; log and return as-is for compatibility.
            log.warn("Failed to decrypt value, returning plaintext. Consider running data migration. Error: {}", e.getMessage());
            return dbData;
        }
    }
}


