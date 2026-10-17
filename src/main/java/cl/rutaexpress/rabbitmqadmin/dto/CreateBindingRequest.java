package cl.rutaexpress.rabbitmqadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record CreateBindingRequest(

        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,200}$", message = "El nombre del exchange no es válido")
        String exchange,

        @NotBlank(message = "La cola es obligatoria")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,200}$", message = "El nombre de la cola no es válido")
        String queue,

        @NotBlank(message = "La routing key es obligatoria")
        @Pattern(
                regexp = "^[a-zA-Z0-9._#*-]{1,255}$",
                message = "La routing key solo puede contener letras, números, puntos, guiones, # y * (máximo 255 caracteres)"
        )
        String routingKey
) {
}
