package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    @Override
    public void saveUser(User user) {

        /*
         * Defensive validation:
         * a newly-created account must always have
         * a nonblank password.
         */
        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required when creating a user"
            );
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {

        User existingUser = userRepository.findById(user.getId())
                .orElse(null);

        if (existingUser == null) {
            return;
        }

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRoles(user.getRoles());

        /*
         * Blank password during editing means:
         * keep the current password.
         *
         * Only encode and replace the password when
         * the administrator entered a new one.
         */
        if (user.getPassword() != null &&
                !user.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(
                            user.getPassword()
                    )
            );
        }

        userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );
    }
}