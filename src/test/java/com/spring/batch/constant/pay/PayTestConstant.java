package com.spring.batch.constant.pay;

import java.time.LocalDateTime;
import java.time.Month;

public final class PayTestConstant {

    public static final long AMOUNT_1 = 1000L;
    public static final long AMOUNT_2 = 2000L;
    public static final long AMOUNT_3 = 3000L;
    public static final String TX_NAME_1 = "trade1";
    public static final String TX_NAME_2 = "trade2";
    public static final String TX_NAME_3 = "trade3";
    public static final LocalDateTime DEFAULT_TX_DATE_TIME = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0);
}
