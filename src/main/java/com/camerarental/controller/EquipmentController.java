package com.camerarental.controller;

import com.camerarental.entity.Category;
import com.camerarental.entity.Equipment;
import com.camerarental.service.EquipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("/equipments")
    public String listEquipments(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) Equipment.Status status,
                                 @RequestParam(required = false) Long editId,
                                 Model model) {
        model.addAttribute("equipments", equipmentService.searchEquipments(keyword, categoryId, status));
        model.addAttribute("categories", equipmentService.getAllCategories());
        model.addAttribute("statuses", Equipment.Status.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);

        if (editId != null) {
            model.addAttribute("equipmentForm", equipmentService.getEquipmentById(editId));
        } else {
            model.addAttribute("equipmentForm", new Equipment());
        }
        return "equipments";
    }

    @PostMapping("/equipments/save")
    public String saveEquipment(@ModelAttribute Equipment equipment,
                                @RequestParam Long categoryId,
                                RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (equipment.getId() == null);
            Equipment saved = equipmentService.saveEquipment(equipment, categoryId);
            redirectAttributes.addFlashAttribute("successMessage",
                    (isNew ? "Đã thêm thiết bị mới: " : "Đã cập nhật thiết bị: ") + saved.getCode() + " - " + saved.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/equipments";
    }

    @PostMapping("/equipments/delete/{id}")
    public String deleteEquipment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.deleteEquipment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thiết bị thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/equipments";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (category.getId() == null);
            Category saved = equipmentService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage",
                    (isNew ? "Đã thêm danh mục: " : "Đã cập nhật danh mục: ") + saved.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/equipments";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/equipments";
    }
}
