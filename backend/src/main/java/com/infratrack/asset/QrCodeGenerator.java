package com.infratrack.asset;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.infratrack.exception.BusinessValidationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generates PNG QR codes encoding plain text content (V2.4.0 Sprint M4-BE2).
 * Reusable for future printable asset labels without changing generation logic.
 */
@Component
public class QrCodeGenerator {

    static final int IMAGE_SIZE_PX = 512;
    static final String PNG_FORMAT = "PNG";

    public byte[] generatePng(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessValidationException("QR code content is required");
        }
        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    content.trim(),
                    BarcodeFormat.QR_CODE,
                    IMAGE_SIZE_PX,
                    IMAGE_SIZE_PX,
                    encodingHints());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, PNG_FORMAT, output);
            return output.toByteArray();
        } catch (WriterException ex) {
            throw new BusinessValidationException("Unable to generate QR code");
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to write QR code image", ex);
        }
    }

    private static Map<EncodeHintType, Object> encodingHints() {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        return hints;
    }
}
