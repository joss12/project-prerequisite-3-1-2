package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(
            UserService userService
    ) {
        this.userService = userService;
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
                "authenticatedUser",
                authenticatedUser
        );

        return "admin";
    }
}