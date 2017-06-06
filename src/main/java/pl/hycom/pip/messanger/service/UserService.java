package pl.hycom.pip.messanger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.hycom.pip.messanger.controller.model.RoleDTO;
import pl.hycom.pip.messanger.controller.model.UserDTO;
import pl.hycom.pip.messanger.mail.Message;
import pl.hycom.pip.messanger.model.PasswordResetToken;
import pl.hycom.pip.messanger.repository.PasswordResetTokenRepository;
import pl.hycom.pip.messanger.repository.RoleRepository;
import pl.hycom.pip.messanger.exception.EmailNotUniqueException;
import pl.hycom.pip.messanger.repository.model.User;
import pl.hycom.pip.messanger.repository.UserRepository;
import pl.hycom.pip.messanger.repository.model.Role;
import pl.hycom.pip.messanger.util.RequestHelper;

import javax.inject.Inject;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Monia on 2017-05-20.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Log4j2
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MapperFacade orikaMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> findAllUsers() {
        log.info("Searching all users");

        return orikaMapper.mapAsList(StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()), UserDTO.class);
    }

    public UserDTO findUserById(Integer id) {
        log.info("Searching for user with id[" + id + "]");

        return orikaMapper.map(userRepository.findOne(id),UserDTO.class);
    }

    public UserDTO addOrUpdateUser(UserDTO user, String requestUrl) throws EmailNotUniqueException{
        User userToUpdateOrAdd = orikaMapper.map(user, User.class);
        if (user.getId() != null && user.getId() != 0) {
            return orikaMapper.map(updateUser(userToUpdateOrAdd, requestUrl), UserDTO.class);
        } else {
            return orikaMapper.map(addUser(userToUpdateOrAdd, requestUrl), UserDTO.class);
        }
    }

    public User addUser(User user, String requestUrl) throws EmailNotUniqueException {
        log.info("Adding user: " + user);
        return trySaveUser(user, true, requestUrl);
    }

    private void setDefaultRole(User user) {
        log.info("setUserRoleIfNoneGranted method invoked for user: " + user);
        if (CollectionUtils.isEmpty(user.getAuthorities())) {
            roleRepository.findByAuthorityIgnoreCase(Role.RoleName.ROLE_USER.name())
                    .ifPresent(role -> user.setRoles(Collections.singleton(role)));
        }
    }

    public User updateUser(User user, String requestUrl) throws EmailNotUniqueException {
        log.info("Updating user: " + user);
        User userToUpdate = userRepository.findOne(user.getId());
        userToUpdate.setFirstName(user.getFirstName());
        userToUpdate.setLastName(user.getLastName());
        userToUpdate.setPhoneNumber(user.getPhoneNumber());
        userToUpdate.setEmail(user.getEmail().toLowerCase());
        Collection<Role> roles = user.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            setDefaultRole(userToUpdate);
        } else {
            userToUpdate.setRoles(roles);
        }
        return trySaveUser(userToUpdate, false, requestUrl);
    }

    private User trySaveUser(User user, boolean isNewUser, String requestUrl) throws EmailNotUniqueException{
        User userToSave = null;
        try {
            userToSave = userRepository.save(user);
            if (isNewUser) {
                String token = generateToken();
                createPasswordResetTokenForUser(userToSave, token);
                emailService.sendEmail(constructResetTokenEmail(requestUrl, user, token));
            }
        } catch (DataIntegrityViolationException e) {
            throw new EmailNotUniqueException(e.getCause());
        }
        return userToSave;
    }

    public void deleteUser(Integer id) {
        log.info("Deleting user[" + id + "]");
        userRepository.delete(id);
    }

    public boolean isChosenAccountCurrentUser(Integer id) {
        User auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findOne(id);
        return auth.getId().equals(user.getId());
    }

    @Override
    public User loadUserByUsername(String email) {
        log.info("loadUserByUsername method from UserService invoked");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email=%s was not found", email)));
    }

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public User findUserByEmail(String email) {
        log.info("findUserByEmail method from UserService invoked");
        return userRepository.findByEmail(email).get();
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(resetToken);
    }

    public boolean validatePasswordResetToken(String token, String email) {
        log.info("validatePasswordResetToken method invoke");
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        User user = resetToken.getUser();
        if (resetToken == null) {
            log.info("Token is invalid");
            return false;
        }

        if (!user.getEmail().equals(email)) {
            log.info("Token is invalid");
            return false;
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.info("Token expired");
            return false;
        }
        tokenRepository.delete(resetToken);
        log.info("token " + token + " is valid");
        log.info("token " + token + " removed from database");
        return true;
    }

    public void changePassword(User user, String password) {
        User userToUpdate = userRepository.findOne(user.getId());
        userToUpdate.setPassword(passwordEncoder.encode((password)));
        userRepository.save(userToUpdate);
        log.info("User " + user.getUsername() + " changed password for " + password);
    }

    public SimpleMailMessage constructResetTokenEmail(String contextPath, User user, String token) {
        List<String> to = new ArrayList<>();
        to.add(user.getEmail());
        String url = contextPath + "/account/password/change/reset/token/" + token;
        Message message = new Message("messenger.recommendations2017@gmail.com", to, "Reset password", url);
        return message.constructEmail();
    }

    //lub Set<String>
    public Set<Integer> findUserRoles(Integer id) {
        return userRepository.findOne(id).getRoles().stream().map(Role::getId).collect(Collectors.toSet());
    }
}
