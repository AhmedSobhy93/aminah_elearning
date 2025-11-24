package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/list";
    }
    @GetMapping("/{id}")
    public String viewUser(@PathVariable String id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/user-details";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable String id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/user-edit";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable String id, @ModelAttribute("user") User updatedUser) {
        userService.updateUser(id, updatedUser);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    @GetMapping("/enable/{id}")
    public String enableUser(@PathVariable String id) {
        userService.enableUser(id);
        return "redirect:/users";
    }
}
