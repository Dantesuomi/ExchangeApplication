package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.UserRole;
import com.assignment.ExchangeApplication.exceptions.EmailExistsException;
import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.EMAIL_IN_USER_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.USERNAME_IN_USE_ERROR;

@Service
public class ClientServiceImpl implements ClientService, UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;


    public ClientServiceImpl(ClientRepository clientRepository,
                              PasswordEncoder passwordEncoder
    ) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public Client registerClient(ClientDto clientDto){
        if (!isValidEmail(clientDto.getEmail())) {
            log.warn("Incorrect email input {}", clientDto.getEmail());
            throw new IllegalArgumentException("Incorrect email input " + clientDto.getEmail());
        } else if (!isValidPassword(clientDto.getPassword())) {
            log.warn("Password must include number, upper and lower case character and min length of 8");
            throw new IllegalArgumentException("Password must include number, upper and lower case character and min length of 8");
        }

        if (clientRepository.existsByEmail(clientDto.getEmail())) {
            log.warn("Failed to register client, email is in use: {}", clientDto.getEmail());
            throw new EmailExistsException(EMAIL_IN_USER_ERROR);
        } else if (clientRepository.existsByUsername(clientDto.getUsername())) {
            log.warn("Failed to register client, username is in use: {}", clientDto.getUsername());
            throw new UsernameExistsException(USERNAME_IN_USE_ERROR);
        }

        Client client = new Client();
        try {
            client.setEmail(clientDto.getEmail());
            client.setPassword(passwordEncoder.encode(clientDto.getPassword()));
            client.setName(clientDto.getName());
            client.setUsername(clientDto.getUsername());
            client.setRole(UserRole.USER);

            log.info("Registering new User");
            clientRepository.save(client);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to register user");
        }
        return client;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Client> client = clientRepository.findByUsername(username);

        if (client.isEmpty())
            throw new UsernameNotFoundException("Client not found");

        return new Client(
                client.get().getId(),
                client.get().getEmail(),
                client.get().getPassword(),
                client.get().getName(),
                client.get().getUsername(),
                client.get().getCreatedAt(),
                Collections.singletonList(new SimpleGrantedAuthority(client.get().getRole().toString())),
                client.get().getRole()
        );
    }


    private boolean isValidPassword(String password) {
        // must include number, upper and lower case character and min length of 8
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        Pattern regex = Pattern.compile(pattern);
        return regex.matcher(password).matches();
    }

    private boolean isValidEmail(String email) {
        String pattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern regex = Pattern.compile(pattern);
        return regex.matcher(email).matches();
    }

}
