package fr.but3.cinehub.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import fr.but3.cinehub.entity.RegisterUserDto;
import fr.but3.cinehub.entity.User;
import fr.but3.cinehub.entity.UserSummary;
import fr.but3.cinehub.repository.UserRepository;

import java.util.Optional;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id);
    }
    @GetMapping("/summary/{id}")
    public UserSummary getUserSummary(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        UserSummary userSummary = new UserSummary();
        userSummary.setUsername(user.getName());
        // Set other non-sensitive properties...
        return userSummary;
    }

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody RegisterUserDto registerUserDto) {
        User user = new User();
        user.setName(registerUserDto.getName());
        user.setFirstName(registerUserDto.getFirstName());
        user.setMail(registerUserDto.getMail());
        user.setLanguage(registerUserDto.getLanguage());
        user.setPassword(bCryptPasswordEncoder.encode(registerUserDto.getPassword()));
        user.setRole("admin");

        User savedUser = userRepository.save(user);
        System.out.println(savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/me")
    public Optional<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByMail(username);
    }


    @GetMapping("/isConnected")
    public boolean isConnected() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

    @PatchMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userUpdates) {
        return userRepository.findById(id)
            .map(user -> {
                user.setName(userUpdates.getName());
                user.setFirstName(userUpdates.getFirstName());
                user.setMail(userUpdates.getMail());
                user.setLanguage(userUpdates.getLanguage());
                user.setPassword(userUpdates.getPassword());
                user.setRole(userUpdates.getRole());
                user.setLastConnection(userUpdates.getLastConnection());
                user.setProfilePicture(userUpdates.getProfilePicture());
                return userRepository.save(user);
            })
            .orElseGet(() -> {
                userUpdates.setId(id);
                return userRepository.save(userUpdates);
            });
    }
}