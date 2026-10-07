package habsida.spring.boot_security.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequest(
        @NotBlank(message = "First name is required")
        @Size(
                min = 2,
                max = 50,
                message = "First name must be between 2 and 50 characters"
        )
        String firstName,

        @NotBlank(message = "Last name required")
        @Size(
                min = 2,
                max = 50,
                message = "Last name must be between 2 and 50 characters"
        )
        String lastName,

        @NotNull(message = "Age is required")
        Integer age,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is required")
        String email,

        String password,

        Set<Long> roleIds
) {
}
