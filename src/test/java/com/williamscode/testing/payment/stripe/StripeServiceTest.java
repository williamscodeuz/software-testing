package com.williamscode.testing.payment.stripe;


import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import com.williamscode.testing.payment.CardPaymentCharge;
import com.williamscode.testing.payment.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class StripeServiceTest {

    @Mock
    private StripeApi stripeApi;

    private StripeService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new StripeService(stripeApi);
    }

    @Test
    void itShouldChargeCard() throws StripeException {
        //Given
        String cardSource = "001";
        BigDecimal amount = BigDecimal.valueOf(1000L);
        Currency currency = Currency.UZS;
        String description = "";

        Charge charge = new Charge();
        charge.setPaid(true);
        given(stripeApi.create(any(), any())).willReturn(charge);

        //When
        CardPaymentCharge cardPaymentCharge = underTest.chargeCard(cardSource, amount, currency, description);

        //Then
        ArgumentCaptor<ChargeCreateParams> chargeCreateParamsArgumentCaptor = ArgumentCaptor.forClass(ChargeCreateParams.class);
        ArgumentCaptor<RequestOptions> requestOptionsArgumentCaptor = ArgumentCaptor.forClass(RequestOptions.class);

        then(stripeApi).should().create(
                chargeCreateParamsArgumentCaptor.capture(),
                requestOptionsArgumentCaptor.capture()
        );
        ChargeCreateParams chargeCreateParamsArgumentCaptorValue = chargeCreateParamsArgumentCaptor.getValue();
        assertThat(chargeCreateParamsArgumentCaptorValue.getSource()).isEqualTo(cardSource);
        assertThat(chargeCreateParamsArgumentCaptorValue.getAmount()).isEqualTo(amount.longValue());
        assertThat(chargeCreateParamsArgumentCaptorValue.getCurrency()).isEqualTo(currency.name());
        assertThat(chargeCreateParamsArgumentCaptorValue.getDescription()).isEqualTo(description);

        RequestOptions requestOptionsArgumentCaptorValue = requestOptionsArgumentCaptor.getValue();
        assertThat(requestOptionsArgumentCaptorValue).isNotNull();
        assertThat(cardPaymentCharge.isCardDebited()).isTrue();
    }

    @Test
    void itShouldNotChargeWhenApiThrowsException() throws StripeException {
        // Given
        String cardSource = "001";
        BigDecimal amount = BigDecimal.valueOf(1000L);
        Currency currency = Currency.UZS;
        String description = "";

        // Throw exception when stripe api is called
        StripeException stripeException = mock(StripeException.class);
        doThrow(stripeException).when(stripeApi).create(any(), any());

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(cardSource, amount, currency, description))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCause(stripeException)
                .hasMessageContaining("Cannot make stripe charge");
    }
}