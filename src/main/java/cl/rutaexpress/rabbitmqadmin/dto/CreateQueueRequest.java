package cl.rutaexpress.rabbitmqadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;


public record createQueueRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]{1,200}$",
                message = "El nombre de la cola solo puede contener letras, números, puntos, guiones y guiones bajos (máximo 200 caracteres)"
        )
        String name,

        boolean durable,

        String deadLetterExchange,

        @PositiveOrZero(message = "messageTtl no puede ser negativo")
        Long messageTtl
) {

    /** Si no se envía, por convención las colas de administración quedan durables. */
    public createQueueRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
