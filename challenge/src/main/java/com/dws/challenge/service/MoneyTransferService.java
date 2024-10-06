package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MoneyTransferService {

    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;

    private final Map<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    @Autowired
    public MoneyTransferService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Account accountFrom = accountsRepository.getAccount(fromAccountId);
        Account accountTo = accountsRepository.getAccount(toAccountId);

        if (accountFrom == null) {
            throw new IllegalArgumentException("From Account id " + fromAccountId + " does not exist!");
        }
        if (accountTo == null) {
            throw new IllegalArgumentException("To Account id " + toAccountId + " does not exist!");
        }

        // locks for both accounts in a consistent order
        ReentrantLock lock1 = getLock(fromAccountId, toAccountId);
        ReentrantLock lock2 = getLock(toAccountId, fromAccountId);

        boolean lock1Acquired = false;
        boolean lock2Acquired = false;

        try {
            // get both locks
            lock1.lock();
            lock1Acquired = true;
            lock2.lock();
            lock2Acquired = true;

            // validate if sufficient funds exist in accountFrom
            if (accountFrom.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance in From Account " + accountFrom.getAccountId());
            }

            // Perform the transfer
            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            accountTo.setBalance(accountTo.getBalance().add(amount));

            // Notify both account holders
            notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to account " + accountTo.getAccountId());
            notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from account " + accountFrom.getAccountId());

        } finally {
            // Release both locks only if acquired and avoid IllegalMonitorStateException
            if (lock2Acquired) {
                lock2.unlock();
            }
            if (lock1Acquired) {
                lock1.unlock();
            }
        }
    }

    private ReentrantLock getLock(String id1, String id2) {
        return id1.compareTo(id2) < 0 ?
                accountLocks.computeIfAbsent(id1, k -> new ReentrantLock())
                : accountLocks.computeIfAbsent(id2, k -> new ReentrantLock());
    }
}


