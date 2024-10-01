package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.MoneyTransferService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
//import javax.validation.Valid;
// Commented as this is moved to jakarta package

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody @Valid TransferRequest transferRequest) {
        log.info("Initiating money transfer {}", transferRequest);

        String fromAccountId = transferRequest.getAccountFrom();
        String toAccountId = transferRequest.getAccountTo();
        BigDecimal amount = transferRequest.getAmount();

        try {
            accountsService.transfer(fromAccountId, toAccountId, amount);
            return ResponseEntity.ok("Transfer successful");
        } catch (IllegalArgumentException | InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
