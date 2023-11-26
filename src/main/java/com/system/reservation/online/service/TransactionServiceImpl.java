package com.system.reservation.online.service;

import com.system.reservation.online.dto.ReservationDto;
import com.system.reservation.online.entity.Item;
import com.system.reservation.online.entity.Transaction;
import com.system.reservation.online.entity.User;
import com.system.reservation.online.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService{

    private UserService userService;
    private ItemService itemService;

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(UserService userService,
                                  ItemService itemService,
                                  TransactionRepository transactionRepository) {
        this.userService = userService;
        this.itemService = itemService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void reserveItem(ReservationDto reservationDto, Long itemId) {

        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Get email
        String email = auth.getName();

        // Find user by email
        User user = userService.findUserByEmail(email);

        // Get current item by id
        Item item = itemService.findById(itemId);


        // Get data from reservation dto
        String receivedDate = reservationDto.getPickupDate();
        Integer reserveItem = reservationDto.getReserveItem();

        // Get the current stock of item
        Integer stock = item.getQuantity();

        // Subtract the reserve item from the current stock of item
        Integer currentStock = stock - reserveItem;

        // If the difference is negative, throw an error
        if (currentStock < 0) {
            throw new RuntimeException("The stock must be greater than the reserve item");
        }

        // Set the new stock
        item.setQuantity(currentStock);

        // Get the current date
        LocalDate localDate = LocalDate.now();

        // Initialize transaction
        Transaction transaction = new Transaction(receivedDate,
                                                  reserveItem,
                                                  localDate.toString(),
                                                  " --- ",
                                                  " --- ",
                                                  "Pending",
                                                  user,
                                                  item);


        // Save transaction in database
        transactionRepository.save(transaction);


    }

    @Override
    public List<Transaction> viewTransactions() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Get current email of user
        String email = auth.getName();

        // Get the current user by email
        User user = userService.findUserByEmail(email);

        // Get list of transactions of user
        List<Transaction> transactions = transactionRepository.findByUser_Id(user.getId());

        // return list of transactions to controller layer
        return transactions;
    }


    @Override
    public Page<Transaction> findAllPaginated(Integer currentPage, Integer pageSize) {
        return transactionRepository.findAll(PageRequest.of(currentPage, pageSize));
    }


    @Override
    public Page<Transaction> findByItemByNameContaining(String name, Integer currentPage, Integer pageSize) {
        List<Transaction> transactionList = transactionRepository.findByUser_NameContainingIgnoreCase(name);

        Page<Transaction> page = new PageImpl<>(transactionList, PageRequest.of(currentPage, pageSize), transactionList.size());

        return page;
    }

    @Override
    public Page<Transaction> findAllPaginatedByUserId(Long id, Integer currentPage, Integer pageSize) {
        List<Transaction> transactionList = transactionRepository.findByUser_Id(id);

        // Ensure currentPage is not less than 0
        currentPage = Math.max(currentPage, 0);

        int start = Math.min(currentPage * pageSize, transactionList.size());
        int end = Math.min(start + pageSize, transactionList.size());

        Page<Transaction> page = new PageImpl<>(transactionList.subList(start, end), PageRequest.of(currentPage, pageSize), transactionList.size());

        return page;
    }


    @Override
    public Transaction findById(Long id) {

        return transactionRepository.findById(id).
                orElseThrow(()-> new RuntimeException("Didn't find id - " + id));
    }

    @Override
    public void cancelTransaction(Long transactionId) {

        // Find transaction by id
        Transaction transaction = findById(transactionId);

        // Get the number of reserved item
        Integer reservedItem = transaction.getQuantity();

        // Get the item
        Item item = transaction.getItem();

        // Put the reserved item count on the current stock of item
        item.setQuantity(item.getQuantity() + reservedItem);

        // Set the remarks status as "Cancelled"
        transaction.setRemarks("Cancelled");

        // Get the current date
        LocalDate localDate = LocalDate.now();

        // Set the cancelled date
        transaction.setCancelledDate(localDate.toString());

        // Save the transaction in database
        transactionRepository.save(transaction);

    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public void approveTransaction(Long transactionId) {

        // Get transaction by transactionId
        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Id not fount - " + transactionId));


        // Change the status of transaction to approve
        transaction.setRemarks("Approved");

        // Save the newly updated transaction to database
        transactionRepository.save(transaction);
    }
}
