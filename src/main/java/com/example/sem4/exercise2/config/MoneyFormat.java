package com.example.sem4.exercise2.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Component("moneyFormat")
public class MoneyFormat {

    public String vnd(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        format.setMaximumFractionDigits(0);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(value) + " đ";
    }
}
