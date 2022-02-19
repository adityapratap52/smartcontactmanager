package com.smartcontactmanager.controller;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private UserRepository repository;

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

        model.addAttribute("title","SignUp - Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }


    // this handler for registering user
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement",defaultValue = "false") boolean agreement, Model model, HttpSession session) {

        try {

            if (!agreement){
                System.out.println("You have not agreed terms and conditions");
                throw new Exception("You have not agreed terms and conditions");
            }

            if (result1.hasErrors()) {
                System.out.println(result1);
                model.addAttribute("user",user);
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("Default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            System.out.println("Agreement " + agreement);
            System.out.println("USER " + user);

            User result = repository.save(user);

//        model.addAttribute("user",new User());
            model.addAttribute("user",new User());
            session.setAttribute("message", new Message("Successfully Register!! ", "alert-success"));
            return "signup";

        }catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user",user);
            session.setAttribute("message", new Message("Something went wrong!! "+e.getMessage(), "alert-danger"));
            return "signup";
        }
    }


    // handler for custom login
    @GetMapping("/signin")
    public String customLogin(Model model) {
        model.addAttribute("title","Login Page");

        return "login";
    }

}
