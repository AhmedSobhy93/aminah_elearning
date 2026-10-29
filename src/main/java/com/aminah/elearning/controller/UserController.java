package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String getAllUsers(Model model) {
        return "redirect:/admin/users";
    }
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-details";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/user-edit";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User updatedUser, Model model) {
        try {
            userService.updateUser(id, updatedUser);
        } catch (IllegalArgumentException ex) {
            updatedUser.setId(id);
            updatedUser.setUsername(userService.getUserById(id).getUsername());
            model.addAttribute("user", updatedUser);
            model.addAttribute("error", ex.getMessage());
            return "admin/users/user-edit";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        try {
            userService.deleteUser(id);
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return "redirect:/admin/users";
    }
}
