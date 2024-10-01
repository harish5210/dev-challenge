package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final MoneyTransferService moneyTransferService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, MoneyTransferService moneyTransferService) {
        this.accountsRepository = accountsRepository;
        this.moneyTransferService = moneyTransferService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        this.moneyTransferService.transfer(fromAccountId, toAccountId, amount);
    }
}
