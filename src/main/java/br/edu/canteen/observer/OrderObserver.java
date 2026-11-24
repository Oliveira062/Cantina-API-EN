package br.edu.canteen.observer;

public interface OrderObserver {

    void refresh(OrderChangedEvent event);
}
