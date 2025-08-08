package com.vodovod.controller;

import com.vodovod.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
  private final UserRepository userRepository;

  @GetMapping
  public String list(Model model) {
    model.addAttribute("users", userRepository.findAll());
    return "users/list";
  }
}