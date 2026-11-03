package com.camerarental.controller;

import com.camerarental.config.AuthInterceptor;
import com.camerarental.dto.CreateOrderRequest;
import com.camerarental.dto.ReturnOrderRequest;
import com.camerarental.entity.RentalOrder;
import com.camerarental.entity.User;
import com.camerarental.service.CustomerService;
import com.camerarental.service.EquipmentService;
import com.camerarental.service.RentalOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class RentalOrderController {

    private final RentalOrderService rentalOrderService;
    private final CustomerService customerService;
    private final EquipmentService equipmentService;

    public RentalOrderController(RentalOrderService rentalOrderService,
                                 CustomerService customerService,
                                 EquipmentService equipmentService) {
        this.rentalOrderService = rentalOrderService;
        this.customerService = customerService;
        this.equipmentService = equipmentService;
    }

    @GetMapping("/rentals")
    public String listOrders(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) RentalOrder.Status status,
                             Model model) {
        model.addAttribute("orders", rentalOrderService.searchOrders(keyword, status));
        model.addAttribute("statuses", RentalOrder.Status.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        return "rentals";
    }

    @GetMapping("/rentals/create")
    public String createOrderPage(@RequestParam(required = false) Long customerId,
                                  Model model) {
        CreateOrderRequest form = new CreateOrderRequest();
        LocalDate today = LocalDate.now();
        form.setStartDate(today);
        form.setExpectedReturnDate(today.plusDays(2));
        if (customerId != null) {
            form.setCustomerId(customerId);
        }

        model.addAttribute("orderForm", form);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("equipments", equipmentService.getAllRentableEquipments());
        model.addAttribute("categories", equipmentService.getAllCategories());
        model.addAttribute("conflicts", rentalOrderService.getEquipmentConflicts(form.getStartDate(), form.getExpectedReturnDate()));
        return "rental-create";
    }

    @PostMapping("/rentals/create")
    public String submitCreateOrder(@ModelAttribute("orderForm") CreateOrderRequest orderForm,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
            RentalOrder created = rentalOrderService.createRentalOrder(orderForm, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lập phiếu thuê " + created.getOrderCode() + " thành công! Tổng tiền thuê: "
                    + String.format("%,.0f", created.getTotalRentalFee()) + " đ, Tiền cọc: "
                    + String.format("%,.0f", created.getTotalDeposit()) + " đ.");
            return "redirect:/rentals/" + created.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("orderForm", orderForm);
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("equipments", equipmentService.getAllRentableEquipments());
            model.addAttribute("categories", equipmentService.getAllCategories());
            model.addAttribute("conflicts", rentalOrderService.getEquipmentConflicts(
                    orderForm.getStartDate(), orderForm.getExpectedReturnDate()));
            return "rental-create";
        }
    }

    @GetMapping("/rentals/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        RentalOrder order = rentalOrderService.getOrderById(id);
        model.addAttribute("order", order);

        if (order.getStatus() == RentalOrder.Status.ACTIVE) {
            LocalDate defaultReturnDate = LocalDate.now();
            model.addAttribute("defaultReturnDate", defaultReturnDate);
            model.addAttribute("suggestedLateFee", rentalOrderService.calculateSuggestedLateFee(order, defaultReturnDate));
        }
        return "rental-detail";
    }

    @PostMapping("/rentals/{id}/return")
    public String processReturn(@PathVariable Long id,
                                @ModelAttribute ReturnOrderRequest returnRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            RentalOrder returned = rentalOrderService.processReturnOrder(id, returnRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hoàn tất nhận trả thiết bị cho phiếu " + returned.getOrderCode()
                    + "! Số tiền hoàn cọc cho khách: " + String.format("%,.0f", returned.getRefundAmount()) + " đ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @GetMapping("/api/equipments/conflicts")
    @ResponseBody
    public ResponseEntity<Map<Long, String>> checkConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedReturnDate) {
        return ResponseEntity.ok(rentalOrderService.getEquipmentConflicts(startDate, expectedReturnDate));
    }
}
