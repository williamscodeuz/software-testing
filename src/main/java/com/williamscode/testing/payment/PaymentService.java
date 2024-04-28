package com.williamscode.testing.payment;

import com.williamscode.testing.customer.Customer;
import com.williamscode.testing.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;
    private final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.UZS);

    void chargeCard(UUID customerId, PaymentRequest paymentRequest) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (!customerOptional.isPresent()) {
            throw new IllegalStateException(String.format("Customer with id [%s] not found", customerId));
        }

        Payment payment = paymentRequest.getPayment();
        Currency currency = payment.getCurrency();
        if (!ACCEPTED_CURRENCIES.contains(currency)) {
            throw new IllegalStateException(String.format("Currency [%s] is not supported", currency));
        }

        CardPaymentCharge cardPaymentCharge = cardPaymentCharger.chargeCard(
                payment.getSource(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDescription()
        );
        if (!cardPaymentCharge.isCardDebited()) {
            throw new IllegalStateException(String.format("Card not debited for customer %s",  customerId));
        }
        payment.setCustomerId(customerId);
        paymentRepository.save(payment);



    }
}
