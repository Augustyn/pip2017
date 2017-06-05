package pl.hycom.pip.messanger.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.hycom.pip.messanger.controller.model.ResetPassword;

import pl.hycom.pip.messanger.controller.model.UserEmail;
import pl.hycom.pip.messanger.repository.model.User;
import pl.hycom.pip.messanger.service.EmailService;
import pl.hycom.pip.messanger.service.UserService;
import pl.hycom.pip.messanger.util.RequestHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Piotr on 21.05.2017.
 */
@Controller
@Log4j2
public class ResetPasswordController {

    private static final String FORGET_VIEW = "password/forget";
    private static final String CHANGE_VIEW = "password/change";
    private static final String REDIRECT_FORGET_VIEW = "redirect:/user/password/change";

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("account/password/change")
    public String getForgetPasswordView(@ModelAttribute UserEmail userEmail, Model model) {
        model.addAttribute("userEmail", userEmail);
        return FORGET_VIEW;
    }

    @PostMapping("account/password/change/reset/token/send")
    public String sendEmail(@Valid UserEmail userEmail, BindingResult bindingResult, Model model, HttpServletRequest request, RedirectAttributes attributes) throws MalformedURLException {

        if(bindingResult.hasErrors()) {
            model.addAttribute("userEmail", userEmail);
            log.info("ResetPassword validation error." + bindingResult.getAllErrors());
        }

        if (userEmail.getUserMail().isEmpty()) {
            model.addAttribute("error", new ObjectError("mailIsEmpty", "You need to write your email."));
            return REDIRECT_FORGET_VIEW;
        }
        User user = userService.findUserByEmail(userEmail.getUserMail());

        if(user == null) {
            model.addAttribute("info", new ObjectError("sendOrNotSend", "If user exists, mail was sent."));
            return REDIRECT_FORGET_VIEW;
        }

        String token = userService.generateToken();
        userService.createPasswordResetTokenForUser(user, token);
        emailService.sendEmail(userService.constructResetTokenEmail(RequestHelper.getURLBase(request), user, token));
        attributes.addFlashAttribute("sendOrNotSend", "If user exists, mail was sent.");
        return "redirect:/login";
    }

    @GetMapping("account/password/change/reset/token/{token}")
    public String getChangePasswordView(@PathVariable("token") final String token, @ModelAttribute ResetPassword resetPassword, Model model) {
        resetPassword.setResetToken(token);
        model.addAttribute("resetPassword", resetPassword);
        return CHANGE_VIEW;
    }

    @PostMapping(value = "account/password/change/save")
    public String changePassword(@Valid ResetPassword resetPassword, BindingResult bindingResult, Model model, RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("resetPassword", resetPassword);
            log.info("Reset token validation error." + bindingResult.getAllErrors());
            return CHANGE_VIEW;
        }

        if (resetPassword == null) {
            log.info("token is null");
            return REDIRECT_FORGET_VIEW;
        }

        if (userService.validatePasswordResetToken(resetPassword.getResetToken(), resetPassword.getUserMail())) {
                userService.changePassword(userService.findUserByEmail(resetPassword.getUserMail()), resetPassword.getNewPassword());
                attributes.addFlashAttribute("passwordChangedOrNotChanged", "Jeśli użytkownik istnieje hasło zostało zmienione");
                return "redirect:/login";
        }
        log.info("Failed to change password user " +  userService.findUserByEmail(resetPassword.getUserMail()).getUsername());
        attributes.addFlashAttribute("passwordChangedOrNotChanged", "If user exists, mail was changed.");
        return "redirect:/login";
    }
}
