package habsida.spring.boot_security.demo.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        Integer age,
        String email,
        Set<RoleResponse> roles
) {
}