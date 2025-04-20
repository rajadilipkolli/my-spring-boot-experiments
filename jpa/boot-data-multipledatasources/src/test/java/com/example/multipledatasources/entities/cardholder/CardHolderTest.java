package com.example.multipledatasources.entities.cardholder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CardHolderTest {

    @Test
    @DisplayName("Should accept valid credit card numbers")
    void shouldAcceptValidCreditCardNumbers() {
        // Given
        CardHolder cardHolder = new CardHolder();

        // When - VISA format
        cardHolder.setCardNumber("4111111111111111");

        // Then
        assertThat(cardHolder.getCardNumber()).isEqualTo("4111111111111111");

        // When - MasterCard format
        cardHolder.setCardNumber("5555555555554444");

        // Then
        assertThat(cardHolder.getCardNumber()).isEqualTo("5555555555554444");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "1234567890123456", // Invalid format
                "41111", // Too short
                "41111111111111111111", // Too long
                "4111111111111112", // Invalid checksum
                "ABCDEFGHIJKLMNOP" // Non-numeric input
            })
    @DisplayName("Should reject invalid credit card numbers")
    void shouldRejectInvalidCreditCardNumbers(String invalidCreditCard) {
        // Given
        CardHolder cardHolder = new CardHolder();

        // When/Then
        assertThatThrownBy(() -> cardHolder.setCardNumber(invalidCreditCard))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credit card number");
    }
}
