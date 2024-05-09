package com.williamscode.testing.customer;

import com.williamscode.testing.utils.PhoneNumberValidator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;


class CustomerRegistrationServiceTest {

    private CustomerRegistrationService underTest;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer() {
        //Given a phone number and customer
        String phoneNumber = "777";
        Customer customer = new Customer(UUID.randomUUID(), "Jamila", phoneNumber);
        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... No customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).usingRecursiveComparison().isEqualTo(customer);
    }

    @Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        //Given a phone number and customer
        String phoneNumber = "777";
        Customer customer = new Customer(UUID.randomUUID(), "Jamila", phoneNumber);
        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        given(phoneNumberValidator.test(phoneNumber)).willReturn(false);

        //When
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .hasMessageContaining(String.format("phone number [%s] is not valid", phoneNumber))
                .isInstanceOf(IllegalStateException.class);

        //Then
        then(customerRepository).should(never()).save(customerArgumentCaptor.capture());

    }

    @Test
    void itShouldNotSaveCustomerWhenCustomerExist() {
        //Given a phone number and customer
        String phoneNumber = "777";
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Jamila", phoneNumber);
        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer));
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should(never()).save(any());
//        then(customerRepository).should().selectCustomerByPhoneNumber(phoneNumber);
//        then(customerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        //Given a phone number and customer
        String phoneNumber = "777";
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Jamila", phoneNumber);
        Customer customerTwo = new Customer(id, "Amelia", phoneNumber);
        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customerTwo));
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        //When
        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining(String.format("phone number [%s] is taken", phoneNumber));

        //Finally
        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        //Given a phone number and customer
        String phoneNumber = "777";
        Customer customer = new Customer(null, "Jamila", phoneNumber);
        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... No customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(customer);
        assertThat(customerArgumentCaptor.getValue().getId()).isNotNull();
    }

}
