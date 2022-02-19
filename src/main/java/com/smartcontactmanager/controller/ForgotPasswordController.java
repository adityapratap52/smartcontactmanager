package com.smartcontactmanager.controller;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.model.User;
import com.smartcontactmanager.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotPasswordController {

    private int otpValue;
    private String emailValue;

    Random random = new Random(1000);


    @Autowired
    UserRepository repository;

    @Autowired
    EmailService emailService;


    @GetMapping("/forgot")
    public String openEmailForm() {

        return "forgot_email_form";
    }


    @PostMapping("send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session, Model model) {

        User user = this.repository.getUserByUserName(email);

        try {

            if (user == null) {

                session.setAttribute("message", new Message("This email id is not registered! Please Enter valid Id", "warning"));

                return "redirect:/forgot";
            }

            otpValue = random.nextInt(999999);

            // send email function code
            String subject = "Smart Contact Manager OTP";

//            String message = "otp: " + otpValue;
            String message = "<div style='border 1px solid red'><h1>OTP is: " + otpValue +"</h1></div>";

            String to = email;

            this.emailValue = email;

            boolean sendSuccess = this.emailService.sendEmail(subject, message, to);

            if (sendSuccess) {

                session.setAttribute("message", new Message("We have sent otp to your gmail!!", "success"));

                return "verify_otp";
            } else {

                session.setAttribute("message", new Message("Otp is not send successfully! Try Again", "danger"));

                return "forgot_email_form";
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            return "redirect:/forgot";
        }

    }


    @PostMapping("/compare-otp")
    public String compareOTP(@RequestParam("otp") int otp, HttpSession session) {

        System.out.println("Email OTP: " + otpValue);
        System.out.println("User OTP: " + otp);

        if (otp == otpValue) {

            return "new_password";
        } else {
            session.setAttribute("message", new Message("Otp is wrong! Please enter valid OTP", "danger"));
            return "verify_otp";
        }
    }


    @PostMapping("/after-changed")
    public String afterChanged(@RequestParam("password") String password, HttpSession session) {


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = this.repository.getUserByUserName(emailValue);

        user.setPassword(encoder.encode(password));
        repository.save(user);

        System.out.println("New Password: " + password);

        return "redirect:/signin?change";
    }
}
