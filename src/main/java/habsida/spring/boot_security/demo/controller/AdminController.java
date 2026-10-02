package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(
            UserService userService,
            RoleRepository roleRepository
    ) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String adminPage(
            Model model,
            Principal principal
    ) {
        User authenticatedUser =
                (User) userService.loadUserByUsername(
                        principal.getName()
                );

        model.addAttribute(
                "users",
                userService.getAllUsers()
        );

        model.addAttribute(
                "roles",
                roleRepository.findAll()
        );

        model.addAttribute(
                "user",
                new User()
        );

        model.addAttribute(
                "authenticatedUser",
                authenticatedUser
        );

        return "admin";
    }

    @PostMapping("/save")
    public String saveUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model,
            Principal principal
    ) {
        if (user.getId() == null &&
                (user.getPassword() == null ||
                        user.getPassword().isBlank())) {

            bindingResult.rejectValue(
                    "password",
                    "password.required",
                    "Password is required"
            );
        }

        if (bindingResult.hasErrors()) {

            User authenticatedUser =
                    (User) userService.loadUserByUsername(
                            principal.getName()
                    );

            model.addAttribute(
                    "users",
                    userService.getAllUsers()
            );

            model.addAttribute(
                    "roles",
                    roleRepository.findAll()
            );

            model.addAttribute(
                    "authenticatedUser",
                    authenticatedUser
            );

            return "admin";
        }

        if (user.getId() == null) {
            userService.saveUser(user);
        } else {
            userService.updateUser(user);
        }

        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        User user = userService.getUserById(id);

        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "User not found"
            );

            return "redirect:/admin";
        }

        userService.deleteUser(id);

        return "redirect:/admin";
    }
}