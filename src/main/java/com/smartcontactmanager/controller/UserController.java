package com.smartcontactmanager.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smartcontactmanager.dao.ContactRepository;
import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.model.Contact;
import com.smartcontactmanager.model.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    //always run with every method of user
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME " + userName);

        // get the user using username
        User user = userRepository.getUserByUserName(userName);

        model.addAttribute("user", user);
        System.out.println(user);
    }


    // dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model) {

        model.addAttribute("title", "User Dashboard");

        return "normal12/user_dashboard";
    }


    // open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal12/add_contact_form";
    }


    // processing at contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal, HttpSession session) {

        try {

            String userName = principal.getName();

            User user = this.userRepository.getUserByUserName(userName);
            //----------------------------------------------------------------
            // only testing purpose
//            if (3>2) {
//                throw new Exception();
//            }
            //----------------------------------------------------------------
            // processing and uploading file
            if (file.isEmpty()) {
                System.out.println("File is empty");
                contact.setImage("contact.png");
            } else {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }

            user.getContacts().add(contact);
            contact.setUser(user);

            this.userRepository.save(user);

            System.out.println("Data " + contact);

            System.out.println("Added to database");

            // message success
            session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();

            // message error
            session.setAttribute("message", new Message("Something went wrong ! Try again", "danger"));
        }

        return "normal12/add_contact_form";
    }


    // show all contacts of particular user
    @GetMapping("/view-contacts/{page}")
    public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);

        // pageable-> current per page
        // and contact per page - 5
        Pageable pageable = PageRequest.of(page, 2);

        Page<Contact> contacts = this.contactRepository.findContactsByUserId(user.getId(), pageable);

        model.addAttribute("title", "View Contacts Page");
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        //---------Testing Purpose-----------------------------
//        System.out.println("Total Contacts: " + contacts);
//        System.out.println("Current Page: " + page);
//        System.out.println("Total Pages: " + contacts.getTotalPages());
        //-----------------------------------------------------
        return "normal12/view_contacts";
    }


    // show particular contact details
    @GetMapping("/{cId}/contact")
    public String showContactDetails(@PathVariable("cId") int cId, Model model, Principal principal) {

        System.out.println("Our CID " + cId);

        Contact contact = contactRepository.findById(cId).get();

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);

        if (user.getId() == contact.getUser().getId()) {

            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }

        return "normal12/contact_details";
    }


    // delete particular contact
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") int cId, HttpSession session, Principal principal) {
        System.out.println("Delete Id: " + cId);
        Contact contact = this.contactRepository.findById(cId).get();

        User user = userRepository.getUserByUserName(principal.getName());

        if (user.getId() == contact.getUser().getId()) {


            //------------------------------------------------------------

            File saveFile = null;
            try {
                saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contact.getImage());
                Files.delete(path);

                System.out.println("----------Deleted-------------");
            } catch (IOException e) {
                e.printStackTrace();
            }


            //------------------------------------------------------------

            //Bug -> data is not delete from DB only user null
//            contactRepository.delete(contact);

            //Bug solve -> data is not delete from DB only user null
            user.getContacts().remove(contact);
            this.userRepository.save(user);
            System.out.println("Successfully deleted");
            session.setAttribute("message", new Message("Contact Successfully deleted!", "success"));

        } else {
            session.setAttribute("message", new Message("Contact is not deleted!", "danger"));
        }


        return "redirect:/user/view-contacts/0";
    }


    // update form handler
    // with Post request person can't fetch data from url it is more safe than Get
    @PostMapping("/update-contact/{cid}")
    public String updateContact(@PathVariable("cid") int cid, Model model) {

        Contact contact = this.contactRepository.findById(cid).get();

        model.addAttribute("title", "Update Form");
        model.addAttribute("contact", contact);

        return "normal12/update_form";
    }


    // update process handler
    @RequestMapping(value = "/update-process", method = RequestMethod.POST)
    public String updateProcess(@ModelAttribute Contact contact,
                                @RequestParam("profileImage") MultipartFile file, Model model,
                                Principal principal, HttpSession session) {


//        System.out.println("CONTACT NAME: " + contact.getName());
//        System.out.println("CONTACT ID: " + contact.getcId());


        Contact oldContactDetails = contactRepository.findById(contact.getcId()).get();

        try {

            // image work
            if (!file.isEmpty()) {
                // if image file is selected by user

                // delete old image
                File deleteFile = new ClassPathResource("static/img").getFile();

//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + oldContactDetails.getImage());
//                Files.delete(path);
                //-------OR----------
                File file1 = new File(deleteFile, oldContactDetails.getImage());
                file1.delete();

                // update new image
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                // save image in static/img
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                // save image in DB
                contact.setImage(file.getOriginalFilename());

            } else {
                // if image file is not selected by user
                contact.setImage(oldContactDetails.getImage());

            }

            User user = userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);

            contactRepository.save(contact);

            session.setAttribute("message", new Message("Contact Successfully Updated", "success"));

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "redirect:/user/" + contact.getcId() + "/contact";
    }


    // user profile handler
    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);
        model.addAttribute("title","User Profile");
        model.addAttribute("user", user);

        return "normal12/profile";
    }


    @GetMapping("/settings")
    public String settings() {

        return "normal12/settings";
    }


    @GetMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal, HttpSession session) {

        String name = principal.getName();
        User currentUser = userRepository.getUserByUserName(name);

        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

            // change the password
            currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your password is successfully changed", "success"));

            return "redirect:/user/index";

        }else {

            // error
            session.setAttribute("message", new Message("Your password is not changed! Please enter valid password", "danger"));

        }

        System.out.println("USER: " + currentUser.getPassword());

        return "redirect:/user/settings";
    }


    // creating order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public String payment(@RequestBody Map<String, Object> data) throws RazorpayException {

        System.out.println(data);

        int amt = Integer.parseInt(data.get("amount").toString());

        var client = new RazorpayClient("rzp_test_AIWgmWc9Y5bFxH", "5hd1tywHYnUkfd3lsaQQ5KBT");

        JSONObject ob = new JSONObject();
        ob.put("amount", amt*100);
        ob.put("currency", "INR");
        ob.put("receipt", "txn_235425");

        // creating new order
        Order order = client.Orders.create(ob);
        System.out.println(order);

        return order.toString();
    }

}
