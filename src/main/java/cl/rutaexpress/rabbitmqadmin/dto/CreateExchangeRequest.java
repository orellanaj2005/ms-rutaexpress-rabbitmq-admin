package cl.rutaexpress.rabbitmqadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record CreateExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]{1,200}$",
                message = "El nombre del exchange solo puede contener letras, números, puntos, guiones y guiones bajos (máximo 200 caracteres)"
        )
        String name,

        @NotNull(message = "El tipo de exchange es obligatorio (DIRECT, TOPIC, FANOUT o HEADERS)")
        ExchangeType type,

        boolean durable
) {
    public CreateExchangeRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
