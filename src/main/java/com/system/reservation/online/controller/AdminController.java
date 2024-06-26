package com.system.reservation.online.controller;

import com.system.reservation.online.dto.EmailDto;
import com.system.reservation.online.dto.ItemDto;
import com.system.reservation.online.dto.RemarkDto;
import com.system.reservation.online.dto.UserDto;
import com.system.reservation.online.entity.Item;
import com.system.reservation.online.entity.Transaction;
import com.system.reservation.online.entity.User;
import com.system.reservation.online.enums.Remark;
import com.system.reservation.online.service.ItemService;
import com.system.reservation.online.service.TransactionService;
import com.system.reservation.online.service.UserService;
import com.system.reservation.online.utils.FileUploadUtil;
import com.system.reservation.online.utils.UserExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admins")
public class AdminController {

    private UserService userService;
    private ItemService itemService;
    private TransactionService transactionService;

    @Autowired
    public AdminController(UserService userService,
                           ItemService itemService,
                           TransactionService transactionService) {
        this.userService = userService;
        this.itemService = itemService;
        this.transactionService = transactionService;
    }

    @GetMapping({"/dashboard", "/", ""})
    public String dashboard() {

        return "admin/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(required = false) String name) {


        // Instantiate page
        Page<User> users;

        // Instantiate userDto for form
        UserDto user = new UserDto();

        // Get the list of users

        if (name != null) {
            // If the name is present in the @RequestParam, display students that contains the name
            users = userService.findStudentByNameContaining(name, page, 10);
        } else {
            // Otherwise, display all the list of students
            users = userService.findAllPaginated(page, 10);
        }

        model.addAttribute("users", users);
        model.addAttribute("userDto", user);
        model.addAttribute("page", page);

        return "admin/account";
    }

    @PostMapping("/accounts")
    public String addAccount(@Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result,
                             Model model) {

        // Check if user exists
        boolean isUserExists = userService.isUserExists(userDto);

        if (isUserExists) {
            result.rejectValue("email", null, "Email already exists!");
        }

        // Error handler if there's empty field
        if (result.hasErrors()) {
            model.addAttribute("userDto", userDto);
            return "admin/account";
        }

        // Save user
        userService.saveUser(userDto);

        return "redirect:/admins/accounts?success";
    }


    @GetMapping("/accounts/{studentId}")
    public String viewStudentDetails(@RequestParam(defaultValue = "0") Integer page,
                                     @PathVariable Long studentId,
                                     Model model) {

        // Find student by id
        User student = userService.findByStudentId(studentId);

        // Map user object to Dto
        UserDto studentDto = userService.mapUserToDto(student);

        // Get transactions
        Page<Transaction> transactions = transactionService.findAllPaginatedByUserId(student.getId(), page, 10);

        boolean isAdmin = student.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", page);


        model.addAttribute("student", student);
        model.addAttribute("studentDto", studentDto);

        return "admin/student-details";
    }

    @PutMapping("/accounts/{studentId}")
    public String updateStudentDetails(@PathVariable Long studentId,
                                       @ModelAttribute("studentDto") UserDto userDto) {

        // Update student details
        userService.updateStudentDetailsById(studentId, userDto);

        return "redirect:/admins/accounts/" + studentId + "?success";
    }

    @DeleteMapping("/accounts/{studentId}")
    public String deleteStudent(@PathVariable Long studentId) {

        // Delete student by id
        userService.deleteStudentById(studentId);

        return "redirect:/admins/accounts?deleted";
    }


    @GetMapping("/items")
    public String viewItems(@RequestParam(defaultValue = "0") Integer page,
                            @RequestParam(name = "name", required = false) String name,
                            Model model) {

        // Get all items
        Page<Item> items;

        if (name != null) {
            // If the name is present in the @RequestParam, display students that contains the name
            items = itemService.findItemByNameContaining(name, page, 10);
        } else {
            // Otherwise, display the list of students
            items = itemService.findAllPaginated(page, 10);
        }

        model.addAttribute("items", items);
        model.addAttribute("page", page);

        return "admin/items";
    }

    @GetMapping("/items/item")
    public String addItem(Model model) {

        // Initialize itemDto
        ItemDto itemDto = new ItemDto();

        model.addAttribute("itemDto", itemDto);

        return "admin/item";
    }


    @PostMapping("/items/item")
    public String addItem(@RequestParam("image") MultipartFile multipartFile,
                          @Valid @ModelAttribute("itemDto") ItemDto itemDto,
                          BindingResult result,
                          Model model) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("itemDto", itemDto);
            return "admin/item";
        }


        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        Long id = itemService.saveItem(itemDto, fileName);

        String uploadDir = "item-photos/" + id;

        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

        return "redirect:/admins/items/item?success";

    }

    @GetMapping("/items/item/{itemId}")
    public String viewItem(@PathVariable Long itemId,
                           Model model) {

        // Find item by id
        Item item = itemService.findById(itemId);

        // Initialize item dto
        ItemDto itemDto = new ItemDto();

        model.addAttribute("itemDto", itemDto);
        model.addAttribute("item", item);

        return "admin/update-item";
    }

    @PutMapping("/items/item/{itemId}")
    public String updateItem(@RequestParam("image") MultipartFile multipartFile,
                             @PathVariable Long itemId,
                             @Valid @ModelAttribute("itemDto") ItemDto itemDto,
                             BindingResult result,
                             Model model) throws IOException {


        System.out.println("item id - " + itemId);

        // Form validations
        if (result.hasErrors()) {
            model.addAttribute("itemDto", itemDto);
            return "admin/update-item";
        }


        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        itemService.updateItem(itemId, itemDto, fileName);

        String uploadDir = "item-photos/" + itemId;

        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

        return "redirect:/admins/items/item/" + itemId + "?success";


    }


    @DeleteMapping("/items/item/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {

        System.out.println("Deleted item with id " + itemId);

        // Delete item by id
        itemService.deleteById(itemId);

        return "redirect:/admins/items?deleted";
    }

    @GetMapping("/transactions")
    public String getTransactions(@RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(name = "name", required = false) String name,
                                  @RequestParam(name = "remark", required = false) String remark,
                                  @RequestParam(name = "pickup", required = false) String pickupDate,
                                  Model model) {

        // Get all transactions
        Page<Transaction> transactions;

        if (pickupDate != null) {
            // Get the current date
            String currentDate = LocalDate.now().toString();

            transactions = transactionService.findAllByReceivedDate(currentDate, page, 10);
        } else if (remark != null) {
            if (remark.equals("All")) {
                transactions = transactionService.findAllPaginated(page, 10);
            } else {
                transactions = transactionService.findAllByRemarks(remark, page, 10);
            }
        } else if (name != null) {
            // If the name is present in the @RequestParam, display students that contains the name
            transactions = transactionService.findByItemByNameContaining(name, page, 10);
        } else {
            // Otherwise, display the list of students
            transactions = transactionService.findAllPaginated(page, 10);
        }

        List<Remark> remarks = Arrays.asList(Remark.values());

        model.addAttribute("remarkDto", new RemarkDto());
        model.addAttribute("remarks", remarks);
        model.addAttribute("transactions", transactions);
        model.addAttribute("page", page);


        return "admin/transactions";
    }

    @GetMapping("/transactions/{transactionId}")
    public String viewTransaction(@PathVariable Long transactionId,
                                  Model model) {

        //  Get transaction by id
        Transaction transaction = transactionService.findById(transactionId);

        model.addAttribute("transaction", transaction);

        return "admin/transaction-details";
    }

    @PostMapping("/transactions/{transactionId}")
    public String approveTransaction(@PathVariable Long transactionId) {

        // Approve transaction
        transactionService.approveTransaction(transactionId);

        return "redirect:/admins/transactions/" + transactionId + "?approve";
    }

    @DeleteMapping("/transactions/{transactionId}")
    public String deleteTransaction(@PathVariable Long transactionId) {

        transactionService.deleteTransaction(transactionId);
        return "redirect:/admins/transactions?deleted";

    }

    @PostMapping("/transactions/{transactionId}/complete")
    public String completeTransaction(@PathVariable Long transactionId) {

        // Complete transaction
        transactionService.completeTransaction(transactionId);

        return "redirect:/admins/transactions/" + transactionId + "?complete";
    }

    @PostMapping("/transactions/{transactionId}/cancel")
    public String cancelTransaction(@PathVariable Long transactionId) {

        // Cancel transaction
        transactionService.cancelTransaction(transactionId);

        return "redirect:/admins/transactions/" + transactionId + "?cancel";
    }

    @GetMapping("/accounts/{accountId}/transactions/{transactionId}")
    public String viewTransactionDetails(@PathVariable Long accountId,
                                         @PathVariable Long transactionId,
                                         Model model) {


        // Find user by user id
        User user = userService.findByStudentId(accountId);

        // Find transaction by transaction id
        Transaction transaction = transactionService.findById(transactionId);

        model.addAttribute("student", user);
        model.addAttribute("transaction", transaction);

        return "admin/account-transaction-details";

    }

    @GetMapping("/report")
    public String viewReportPage(Model model) {


        List<Remark> remarks = Arrays.asList(Remark.values());

        model.addAttribute("remarks", remarks);
        model.addAttribute("remarkDto", new RemarkDto());
        model.addAttribute("emailDto", new EmailDto());

        return "admin/generate-report";
    }

    @GetMapping("/export/excel")
    public String exportToExcel(@RequestParam(value = "remark", required = false) String remark,
                                @RequestParam(value = "email", required = false) String email,
                                HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue;

        List<Transaction> transactions;

        if (email != null) {
            headerValue = "attachment; filename=transactions_" + email + "_" + currentDateTime + ".xlsx";

            User user = userService.findUserByEmail(email);

            if (user == null) {
                return "redirect:/admins/report?error";
            }
            transactions = transactionService.findByUserEmail(email);
        } else {
            headerValue = "attachment; filename=transactions_" + remark + "_" + currentDateTime + ".xlsx";
            if (remark.equals("All")) {
                transactions = transactionService.getAllTransactions();
            } else {
                transactions = transactionService.findByRemark(remark.toString());
            }
        }

        response.setHeader(headerKey, headerValue);

        UserExcelExporter excelExporter = new UserExcelExporter(transactions);

        excelExporter.export(response);

        return "redirect:/admins/report";
    }


}
