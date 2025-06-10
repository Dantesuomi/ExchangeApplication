package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface ClientService {

    boolean isValidPassword (String password);

    boolean isValidEmail (String email);
    Client registerClient(ClientDto clientDto);
//    Client loadClientByUsername(String username);

//    Client updateUser(Long id, UserProfileDto profileDto);

    Boolean doesClientExistById(UUID ID);

    Account addAccountToClient(UUID clientId, Account account);

    List<Account> getAccountsByClientId(UUID clientId);
}
