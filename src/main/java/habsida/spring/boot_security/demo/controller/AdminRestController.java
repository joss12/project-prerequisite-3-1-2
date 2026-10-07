package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.dto.RoleResponse;
import habsida.spring.boot_security.demo.dto.UserRequest;
import habsida.spring.boot_security.demo.dto.UserResponse;
import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminRestController(
            UserService userService,
            RoleRepository roleRepository
    ) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService
                .getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "User not found"
                    ));
        }

        return ResponseEntity.ok(
                toUserResponse(user)
        );
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleRepository
                .findAll()
                .stream()
                .map(this::toRoleResponse)
                .toList();

        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserRequest request,
            BindingResult bindingResult
    ) {
        if (request.password() == null ||
                request.password().isBlank()) {

            bindingResult.rejectValue(
                    "password",
                    "password.required",
                    "Password is required"
            );
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            getValidationErrors(
                                    bindingResult
                            )
                    );
        }

        User user = toUser(request);

        try {
            userService.saveUser(user);

            User savedUser =
                    userService.getUserById(
                            user.getId()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            toUserResponse(savedUser)
                    );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            exception.getMessage()
                    ));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            BindingResult bindingResult
    ) {
        User existingUser =
                userService.getUserById(id);

        if (existingUser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "User not found"
                    ));
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            getValidationErrors(
                                    bindingResult
                            )
                    );
        }

        User user = toUser(request);
        user.setId(id);

        userService.updateUser(user);

        User updatedUser =
                userService.getUserById(id);

        return ResponseEntity.ok(
                toUserResponse(updatedUser)
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "User not found"
                    ));
        }

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    private User toUser(
            UserRequest request
    ) {
        User user = new User();

        user.setFirstName(
                request.firstName()
        );

        user.setLastName(
                request.lastName()
        );

        user.setAge(
                request.age()
        );

        user.setEmail(
                request.email()
        );

        user.setPassword(
                request.password()
        );

        Set<Role> roles =
                new LinkedHashSet<>();

        if (request.roleIds() != null &&
                !request.roleIds().isEmpty()) {

            roles.addAll(
                    roleRepository.findAllById(
                            request.roleIds()
                    )
            );
        }

        user.setRoles(roles);

        return user;
    }

    private UserResponse toUserResponse(
            User user
    ) {
        Set<RoleResponse> roles =
                user.getRoles()
                        .stream()
                        .map(this::toRoleResponse)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getEmail(),
                roles
        );
    }

    private RoleResponse toRoleResponse(
            Role role
    ) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }

    private Map<String, String> getValidationErrors(
            BindingResult bindingResult
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        bindingResult
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return errors;
    }
}