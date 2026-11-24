package br.edu.canteen.payment;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentFactory {

    private final Map<PaymentType, PaymentStrategy> strategies = new EnumMap<>(PaymentType.class);

    public PaymentFactory(List<PaymentStrategy> available) {
        for (PaymentStrategy strategy : available) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    public PaymentStrategy create(String type) {
        return create(PaymentType.from(type));
    }

    public PaymentStrategy create(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No strategy registered for " + type);
        }
        return strategy;
    }
}
