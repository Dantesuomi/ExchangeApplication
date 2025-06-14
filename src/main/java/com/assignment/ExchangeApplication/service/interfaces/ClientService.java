package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import org.springframework.stereotype.Service;

@Service
public interface ClientService {
    Client registerClient(ClientDto clientDto);

}
