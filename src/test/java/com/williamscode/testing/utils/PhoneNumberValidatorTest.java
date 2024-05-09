package com.williamscode.testing.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberValidatorTest {

    private PhoneNumberValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new PhoneNumberValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "+998901112233, true",
            "+99890111223344, false",
            "1112233, false"
    })
    void itShouldValidatePhoneNumber(String phoneNumber, boolean expected) {
        //Given
        //When
        boolean isValid = underTest.test(phoneNumber);
        //Then
        assertThat(isValid).isEqualTo(expected);
    }

//    @Test
//    @DisplayName("Should fail when length is bigger than 13")
//    void itShouldValidatePhoneNumberWhenIncorrect() {
//        //Given
//        String phoneNumber = "+99890111223344";
//
//        //When
//        boolean isValid = underTest.test(phoneNumber);
//        //Then
//        assertThat(isValid).isFalse();
//    }
//
//    @Test
//    @DisplayName("Should fail when not start with +998")
//    void itShouldValidatePhoneNumberWhenIncorrectPrefix() {
//        //Given
//        String phoneNumber = "1112233";
//
//        //When
//        boolean isValid = underTest.test(phoneNumber);
//        //Then
//        assertThat(isValid).isFalse();
//    }
}
