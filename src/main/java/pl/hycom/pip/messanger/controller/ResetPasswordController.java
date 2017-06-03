package pl.hycom.pip.messanger.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.hycom.pip.messanger.controller.model.ResetPassword;
import pl.hycom.pip.messanger.model.PasswordResetToken;
import pl.hycom.pip.messanger.repository.model.User;
import pl.hycom.pip.messanger.service.EmailService;
import pl.hycom.pip.messanger.service.UserService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Piotr on 21.05.2017.
 */
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Log4j2
public class ResetPasswordController {

    private static final String FORGET_VIEW = "forgetPassword";
    private static final String CHANGE_VIEW = "changePassword";
    private static final String REDIRECT_FORGET_VIEW = "redirect:/reset/password";

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/reset/password")
    public String getForgetPasswordView(Model model) {
        model.addAttribute("resetPassword", new ResetPassword());
        return FORGET_VIEW;
    }

    @PostMapping("reset/password/token/send")
    public String sendEmail(@Valid ResetPassword resetPassword, BindingResult bindingResult, Model model, HttpServletRequest request) throws MalformedURLException {

        if(bindingResult.hasErrors()) {
            model.addAttribute("resetPassword", resetPassword);
            log.info("ResetPassword validation error." + bindingResult.getAllErrors());
        }

        if (resetPassword.getUserMail().isEmpty()) {
            model.addAttribute("error", new ObjectError("mailIsEmpty", "You need to write your email, dumbass."));
            return REDIRECT_FORGET_VIEW;
        }
        User user = userService.findUserByEmail(resetPassword.getUserMail());

        if(user == null) {
            model.addAttribute("info", new ObjectError("sendOrNotSend", "If user exists, mail was sent."));
            return REDIRECT_FORGET_VIEW;
        }

        String token = userService.generateToken();
        userService.createPasswordResetTokenForUser(user, token);
        emailService.sendEmail(userService.constructResetTokenEmail(getURLBase(request), user, token));
        return "redirect:/login";
    }

    @GetMapping("/change/password/token/{token}")
    public String getChangePasswordView(@PathVariable("token") final String token, Model model) {
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setResetToken(token);
        model.addAttribute("resetPassword", resetPassword);
        return CHANGE_VIEW;
    }

    @PostMapping(value = "/change/password")
    public String changePassword(@Valid ResetPassword resetPassword, BindingResult bindingResult, Model model) {

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
                log.info("User " + userService.findUserByEmail(resetPassword.getUserMail()).getUsername() + " changed password for: " + resetPassword.getNewPassword());
                return "redirect:/login";
        }
        log.info("Failed to change password user " +  userService.findUserByEmail(resetPassword.getUserMail()).getUsername());
        return REDIRECT_FORGET_VIEW;
    }

    private String getURLBase(HttpServletRequest request) throws MalformedURLException {

        URL requestURL = new URL(request.getRequestURL().toString());
        String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
        return requestURL.getProtocol() + "://" + requestURL.getHost() + port;
    }
}
