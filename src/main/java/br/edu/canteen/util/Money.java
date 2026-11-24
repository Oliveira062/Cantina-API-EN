package br.edu.canteen.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Money {

    private static final Locale LOCALE = Locale.US;

    private Money() {
    }

    public static String format(BigDecimal value) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        return String.format(LOCALE, "$%,.2f", v);
    }
}
