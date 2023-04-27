package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println(username);
		User user = userRepository.getUserByUsername(username);
		
		model.addAttribute("user",user);

	}
	
	
	@GetMapping("/index")
	public String userDashboard(Model model, Principal principal) {
				model.addAttribute("title","Dashboard");
		return "normal/user_dashboard";
	}
	
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(
			@PathVariable("page") Integer page,
			Model model,Principal principal) {
		model.addAttribute("title","View Contacts");
		
		String username = principal.getName();
		User user = userRepository.getUserByUsername(username);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		
		Page<Contact> contacts = contactRepository.findContactsByUserId(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show-contacts";
	}
	
	@GetMapping("/{id}/contact")
	public String showContactDetails(@PathVariable("id") Integer id, Model model, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(id);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		
		if(user.getId()==contact.getUser().getId())
		model.addAttribute("c",contact);
		
		
		return "normal/contact_details";
	}
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, RedirectAttributes rediAttr, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		
		if(user.getId()==contact.getUser().getId()) {
		this.contactRepository.delete(contact);
		rediAttr.addFlashAttribute("message", new Message("Contact successfully deleted!", "success"));
		}
		else {
			rediAttr.addFlashAttribute("message", new Message("Contact you are trying to delete is not in your contact-list!", "danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	@GetMapping("/update-contact/{id}")
	public String updateContact(@PathVariable("id") Integer id, Model model) {
		
		Contact contact = this.contactRepository.findById(id).get();
		model.addAttribute("title", "Update Contact");
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	
//	For Showing Your Profile
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Your Profile");
		return "normal/profile";
	}
	
	
//	For handling post request of updating contacts
	@PostMapping("/update-contact")
	public String updateContactPost(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,
			RedirectAttributes rediAttr) {
		
		
		try {
			
			 Contact oldContactDetails = this.contactRepository.findById(contact.getId()).get();
			
		if(!file.isEmpty()) {
//			Delete the contact
			
			File dltFile = new ClassPathResource("static/img").getFile();
			File deleteFile = new File(dltFile,oldContactDetails.getImage());
			deleteFile.delete();
			
			
//			Update the contact
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			contact.setImage(file.getOriginalFilename());
		}
		else {
			contact.setImage(oldContactDetails.getImage());
		}
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		
		contact.setUser(user);
		
		this.contactRepository.save(contact);
		rediAttr.addFlashAttribute("message", new Message("Contact successfully updated!","success"));
		
		}
		catch(Exception e) {
			e.printStackTrace();
			rediAttr.addFlashAttribute("message", new Message("Something went wrong! Try Again","danger"));
		}
				
		return "redirect:/user/update-contact/"+contact.getId();
	}
	
	
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			RedirectAttributes redirAttrs) {
		try {
		String username = principal.getName();
		User user = userRepository.getUserByUsername(username);
		
		
//		Processing Image
		if(file.isEmpty()) {
		
			System.out.println("FILE IS EMPTY");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image Uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
//		System.out.println(contact);
		
		userRepository.save(user);
		
		redirAttrs.addFlashAttribute("message",new Message("Contact successfully added! ","success"));
		
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			redirAttrs.addFlashAttribute("message",new Message("Something went wrong! Try Again ","danger"));

		}
		
		return "redirect:/user/add-contact";
	}
	
	
	
}
