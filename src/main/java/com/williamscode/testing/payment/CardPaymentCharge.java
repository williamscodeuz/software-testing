package com.williamscode.testing.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class CardPaymentCharge {

    private final boolean isCardDebited;
}
