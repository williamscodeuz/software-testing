package com.williamscode.testing.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerRegistrationRequest {

    private final Customer customer;

    public CustomerRegistrationRequest (@JsonProperty("customer") Customer customer) {
        this.customer = customer;
    }
}
