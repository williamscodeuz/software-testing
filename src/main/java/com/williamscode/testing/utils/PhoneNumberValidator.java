package com.williamscode.testing.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class PhoneNumberValidator implements Predicate<String> {
    @Override
    public boolean test(String phoneNumber) {
        return phoneNumber.startsWith("+998") && phoneNumber.length() == 13;
    }
}
