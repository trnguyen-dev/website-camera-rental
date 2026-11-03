package com.camerarental.controller;

import com.camerarental.entity.RentalOrder;
import com.camerarental.service.RentalOrderService;
import com.camerarental.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final RentalOrderService rentalOrderService;

    public DashboardController(ReportService reportService, RentalOrderService rentalOrderService) {
        this.reportService = reportService;
        this.rentalOrderService = rentalOrderService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("totalEquipments", reportService.getTotalEquipmentCount());
        model.addAttribute("availableEquipments", reportService.getAvailableEquipmentCount());
        model.addAttribute("activeOrdersCount", reportService.getActiveOrderCount());
        model.addAttribute("totalCustomers", reportService.getTotalCustomerCount());
        model.addAttribute("totalRevenue", reportService.getTotalSystemRevenue());

        List<RentalOrder> overdueOrders = reportService.getOverdueOrders();
        model.addAttribute("overdueOrders", overdueOrders);

        List<RentalOrder> activeOrders = rentalOrderService.searchOrders(null, RentalOrder.Status.ACTIVE);
        model.addAttribute("activeOrders", activeOrders);

        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
        }
        return "dashboard";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("monthlyRevenues", reportService.getMonthlyRevenues());
        model.addAttribute("overdueOrders", reportService.getOverdueOrders());
        model.addAttribute("topEquipments", reportService.getTopRentedEquipments());
        model.addAttribute("totalRevenue", reportService.getTotalSystemRevenue());
        return "statistics";
    }
}
