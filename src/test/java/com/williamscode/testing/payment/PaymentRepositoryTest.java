package com.williamscode.testing.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest(properties = {
        "spring.jpa.properties.javax.persistence.validation.mode=none"
})
class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository underTest;

    @Test
    void itShouldInsertPayment() {
        //Given
        long paymentId = 1L;
        Payment payment = new Payment(
                paymentId,
                UUID.randomUUID(),
                BigDecimal.valueOf(1000L),
                Currency.UZS,
                "card001",
                "donation"
                );
        //When
        underTest.save(payment);
        //Then
        assertThat(underTest.findById(paymentId))
                .isPresent()
                .hasValueSatisfying(
                        p -> assertThat(p).usingRecursiveComparison().isEqualTo(payment)
                );

    }
}