package com.williamscode.testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.properties.javax.persistence.validation.mode=none"
})
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;

    @Test
    void itShouldNotSelectCustomerByPhoneNumberWhenNumberDoesNotExist() {
        //Given
        String phoneNumber = "998990001122";
       //When
        Optional<Customer> customerOptional = underTest.selectCustomerByPhoneNumber(phoneNumber);
        //Then
        assertThat(customerOptional).isNotPresent();
    }

    @Test
    void itShouldSaveCustomer() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Jamila", "998990001122");
        //When
        underTest.save(customer);
        //Then
        Optional<Customer> optionalCustomer = underTest.findById(id);
        assertThat(optionalCustomer)
                .isPresent()
                .hasValueSatisfying(c -> {
//                    assertThat(c.getId()).isEqualTo(id);
//                    assertThat(c.getName()).isEqualTo("Jamila");
//                    assertThat(c.getPhoneNumber()).isEqualTo("998990001122");
//                    assertThat(c).isEqualToComparingFieldByField(customer); //DEPRECATED
                    assertThat(c).usingRecursiveComparison().isEqualTo(customer);
                });
    }

    @Test
    void itShouldNotSaveCustomerWhenNameIsNull() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, null, "998990001122");
        //When
        //Then
        assertThatThrownBy(() -> underTest.save(customer))
                .hasMessageContaining("not-null property references a null or transient value")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveCustomerWhenPhoneIsNull() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Victoria", null);
        //When
        //Then
        assertThatThrownBy(() -> underTest.save(customer))
                .hasMessageContaining("not-null property references a null or transient value")
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
