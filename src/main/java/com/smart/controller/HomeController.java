package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Sign Up - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title","Login - Smart Contact Manager");
		return "login";
	}
	
	
	@PostMapping("/do_register")
	public String do_register(@Validated @ModelAttribute("user") User user,BindingResult res, @RequestParam(value="agreement", defaultValue="false") boolean agreement, Model model,
			HttpSession session, RedirectAttributes redirAttrs) {
		try {	
		if(!agreement) {
			throw new Exception("You have not agreed terms and conditions!");
		}
		if(res.hasErrors()) {
			model.addAttribute("user",user);
			return "signup";
		}
		user.setRole("ROLE_USER");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		this.userRepository.save(user);  
		model.addAttribute("user", new User());
		redirAttrs.addFlashAttribute("message",new Message("Registration Successfully done!", "alert-success"));
		}
		catch(Exception e) {
			redirAttrs.addFlashAttribute("message", new Message("Something went wrong! "+e.getMessage(),"alert-danger"));
			model.addAttribute("user", user);
		}
		return "redirect:/signup";
	}
}
