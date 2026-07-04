package com.infratrack.asset;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrCodeGeneratorTest {

    private final QrCodeGenerator generator = new QrCodeGenerator();

    @Test
    void generatePng_shouldReturnNonEmptyPngBytes() {
        byte[] png = generator.generatePng("AST-1A2B3C4D");

        assertThat(png).isNotEmpty();
        assertThat(isPng(png)).isTrue();
    }

    @Test
    void generatePng_shouldBeDeterministicForSameContent() {
        byte[] first = generator.generatePng("AST-1A2B3C4D");
        byte[] second = generator.generatePng("AST-1A2B3C4D");

        assertThat(second).isEqualTo(first);
    }

    @Test
    void generatePng_blankContent_throwsBusinessValidationException() {
        assertThatThrownBy(() -> generator.generatePng("   "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("QR code content is required");
    }

    private static boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && bytes[0] == (byte) 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G';
    }
}
