package pl.edu.pjatk.lnpayments.webservice.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.pjatk.lnpayments.webservice.auth.converter.UserConverter;
import pl.edu.pjatk.lnpayments.webservice.auth.repository.UserRepository;
import pl.edu.pjatk.lnpayments.webservice.auth.resource.dto.LoginResponse;
import pl.edu.pjatk.lnpayments.webservice.auth.resource.dto.RegisterRequest;
import pl.edu.pjatk.lnpayments.webservice.common.entity.Role;
import pl.edu.pjatk.lnpayments.webservice.common.entity.User;

import javax.transaction.Transactional;
import javax.validation.ValidationException;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @Autowired
    public UserService(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Transactional
    public void createUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("User with mail " + request.getEmail() + " exists!");
        }

        User user = userConverter.convertToEntity(request, Role.ROLE_USER);
        userRepository.save(user);
    }

    public LoginResponse findAndConvertLoggedUser(String username, String jwtToken) {
        User user = findUser(userRepository, username);
        return userConverter.convertToLoginResponse(user, jwtToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUser(userRepository, username);
        return userConverter.convertToUserDetails(user);
    }

    private User findUser(UserRepository userRepository, String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found!"));
    }

}
