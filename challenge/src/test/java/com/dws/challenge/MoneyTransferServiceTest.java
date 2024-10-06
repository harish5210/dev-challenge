package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.MoneyTransferService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MoneyTransferServiceTest {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private AccountsRepository accountsRepository;

    private MoneyTransferService transferService;

    @BeforeEach
    public void setUp() {

        // Instantiate MoneyTransferService with the in-memory repository and mock notification service
        transferService = new MoneyTransferService(accountsRepository, notificationService);

        // Clear the existing accounts before each test.
        accountsRepository.clearAccounts();
        accountsRepository.createAccount(new Account("12345", new BigDecimal("1000")));
        accountsRepository.createAccount(new Account("67890", new BigDecimal("500")));
    }

    @Test
    public void validate_SuccessfulTransfer() {

        Account accountFrom = new Account("12300", new BigDecimal("1000"));
        Account accountTo = new Account("67800", new BigDecimal("500"));

        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);

        BigDecimal amount = new BigDecimal("200");

        transferService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), amount);

        assertEquals(new BigDecimal("800"), accountFrom.getBalance());
        assertEquals(new BigDecimal("700"), accountTo.getBalance());

        verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 200 to account "+accountTo.getAccountId());
        verify(notificationService).notifyAboutTransfer(accountTo, "Received 200 from account "+accountFrom.getAccountId());
    }

    // Scenario: Insufficient funds in from account
    @Test
    void transferMoney_InsufficientFunds() {
        TransferRequest transferRequest = new TransferRequest("12345", "67890", new BigDecimal("2000"));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> {
            transferService.transfer(transferRequest.getAccountFrom(), transferRequest.getAccountTo(), transferRequest.getAmount());
        });
        assertEquals("Insufficient balance in From Account 12345", exception.getMessage());
    }

    // Scenario: The "from" account number is not valid
    @Test
    void transferMoney_InvalidFromAccount() {
        TransferRequest transferRequest = new TransferRequest("99999", "67890", new BigDecimal("100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(transferRequest.getAccountFrom(), transferRequest.getAccountTo(), transferRequest.getAmount());
        });
        assertEquals("From Account id 99999 does not exist!", exception.getMessage());
    }

    // Scenario: The "to" account number is not valid
    @Test
    void transferMoney_InvalidToAccount() {
        TransferRequest transferRequest = new TransferRequest("12345", "99999", new BigDecimal("100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(transferRequest.getAccountFrom(), transferRequest.getAccountTo(), transferRequest.getAmount());
        });
        assertEquals("To Account id 99999 does not exist!", exception.getMessage());
    }

    // Scenario: The transfer amount is negative
    @Test
    void transferMoney_NegativeTransferAmount() {
        TransferRequest transferRequest = new TransferRequest("12345", "67890", new BigDecimal("-100.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(transferRequest.getAccountFrom(), transferRequest.getAccountTo(), transferRequest.getAmount());
        });
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }
}

