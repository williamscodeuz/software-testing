package com.williamscode.testing.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamscode.testing.customer.Customer;
import com.williamscode.testing.customer.CustomerRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    private final PaymentRepository paymentRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public PaymentIntegrationTest(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        //Given
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Alexandra", "+998770001122");

        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);
        ResultActions customerRegResultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/api/v1/customer-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Objects.requireNonNull(objectToJson(customerRegistrationRequest)))
        );
        //When
        long paymentId = 1L;
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("1000.00"), Currency.USD, "source", "desc");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        ResultActions paymentResultActions = mockMvc.perform(
                post("/api/v1/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Objects.requireNonNull(objectToJson(paymentRequest)))
        );
        //Then
        customerRegResultActions.andExpect(status().isOk());
        paymentResultActions.andExpect(status().isOk());
        //TODO: Do not use paymentRepository instead create an endpoint to retrieve payments for customers
        assertThat(paymentRepository.findById(paymentId))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p).usingRecursiveComparison().isEqualTo(payment));
        //TODO: Ensure SMS delivered
    }

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to json");
            return null;
        }
    }
}
