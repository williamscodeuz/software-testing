package com.williamscode.testing.payment;

import com.williamscode.testing.customer.Customer;
import com.williamscode.testing.customer.CustomerRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardPaymentCharger cardPaymentCharger;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new PaymentService(customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldChargeCardSuccessfully() {
        //Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                    null,
                    null,
                    BigDecimal.valueOf(1000L),
                    Currency.UZS,
                    "card001xx",
                    "Donation"
                )
        );

        Payment payment = paymentRequest.getPayment();
        given(cardPaymentCharger.chargeCard(
                payment.getSource(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDescription()
        )).willReturn(new CardPaymentCharge(true));

        //When
        underTest.chargeCard(customerId, paymentRequest);

        //Then
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);
        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();
        assertThat(paymentArgumentCaptorValue)
                .usingRecursiveComparison().ignoringFields("customerId").isEqualTo(payment);
        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void itShouldThrowWhenCardNotCharged() {
        //Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        BigDecimal.valueOf(1000L),
                        Currency.UZS,
                        "card001xx",
                        "Donation"
                )
        );

        Payment payment = paymentRequest.getPayment();
        given(cardPaymentCharger.chargeCard(
                payment.getSource(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDescription()
        )).willReturn(new CardPaymentCharge(false));

        //When
        //Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Card not debited for customer %s",  customerId));
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    @Test
    void itShouldThrowWhenCurrencyNotSupported() {
        //Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        BigDecimal.valueOf(1000L),
                        Currency.GBP,
                        "card001xx",
                        "Donation"
                )
        );

        //When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Currency [%s] is not supported", Currency.GBP));

        //Then
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldThrowWhenCustomerNotFound() {
        //Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        BigDecimal.valueOf(1000L),
                        Currency.USD,
                        "card001xx",
                        "Donation"
                )
        );

        //When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format(String.format("Customer with id [%s] not found", customerId)));

        //Then
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }
}