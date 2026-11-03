package com.camerarental.controller;

import com.camerarental.entity.Customer;
import com.camerarental.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Long editId,
                                @RequestParam(required = false) Long historyId,
                                Model model) {
        model.addAttribute("customers", customerService.searchCustomers(keyword));
        model.addAttribute("keyword", keyword);

        if (editId != null) {
            model.addAttribute("customerForm", customerService.getCustomerById(editId));
        } else {
            model.addAttribute("customerForm", new Customer());
        }

        if (historyId != null) {
            Customer selectedCustomer = customerService.getCustomerById(historyId);
            model.addAttribute("selectedCustomer", selectedCustomer);
            model.addAttribute("customerOrders", customerService.getCustomerRentalHistory(historyId));
        }

        return "customers";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (customer.getId() == null);
            Customer saved = customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage",
                    (isNew ? "Đã thêm khách hàng mới: " : "Đã cập nhật khách hàng: ") + saved.getFullName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers";
    }
}
