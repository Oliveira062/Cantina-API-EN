package br.edu.canteen.dto;

public record CancellationResponse(boolean stockReturned, OrderResponse order) {
}
