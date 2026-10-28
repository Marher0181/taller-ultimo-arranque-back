package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.Size;

public record CancelacionRequest(@Size(max = 255) String motivo) {
}
