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
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "user-form";
    }

    @GetMapping("/edit/{id}")
    public String editUser(
            @PathVariable Long id,
            Model model,
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

        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());

        return "user-form";
    }

    @PostMapping("/save")
    public String saveUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "user-form";
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