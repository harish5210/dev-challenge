package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import com.dws.challenge.service.MoneyTransferService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MoneyTransferServiceTest {

    @MockBean
    private NotificationService notificationService;

    private AccountsRepository accountsRepository;

    private MoneyTransferService transferService;

    @BeforeEach
    public void setUp() {

        accountsRepository = new AccountsRepositoryInMemory(); // My implementation here

        // Instantiate MoneyTransferService with the in-memory repository and mock notification service
        transferService = new MoneyTransferService(accountsRepository, notificationService);
    }

    @Test
    public void testTransfer() {

        Account accountFrom = new Account("1", new BigDecimal("1000"));
        Account accountTo = new Account("2", new BigDecimal("500"));

        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);

        BigDecimal amount = new BigDecimal("200");

        transferService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), amount);

        assertEquals(new BigDecimal("800"), accountFrom.getBalance());
        assertEquals(new BigDecimal("700"), accountTo.getBalance());

        verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 200 to account 2");
        verify(notificationService).notifyAboutTransfer(accountTo, "Received 200 from account 1");
    }
}

