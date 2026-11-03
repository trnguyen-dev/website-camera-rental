package com.camerarental.service;

import com.camerarental.entity.Category;
import com.camerarental.entity.Equipment;
import com.camerarental.repository.CategoryRepository;
import com.camerarental.repository.EquipmentRepository;
import com.camerarental.repository.RentalOrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CategoryRepository categoryRepository;
    private final RentalOrderItemRepository rentalOrderItemRepository;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            CategoryRepository categoryRepository,
                            RentalOrderItemRepository rentalOrderItemRepository) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
        this.rentalOrderItemRepository = rentalOrderItemRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
    }

    @Transactional
    public Category saveCategory(Category category) {
        Optional<Category> existing = categoryRepository.findByNameIgnoreCase(category.getName().trim());
        if (existing.isPresent() && !existing.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Tên danh mục '" + category.getName() + "' đã tồn tại!");
        }
        category.setName(category.getName().trim());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category cat = getCategoryById(id);
        if (!cat.getEquipments().isEmpty()) {
            throw new IllegalArgumentException("Không thể xóa danh mục '" + cat.getName() + "' vì đang có " + cat.getEquipments().size() + " thiết bị thuộc danh mục này!");
        }
        categoryRepository.delete(cat);
    }

    public List<Equipment> searchEquipments(String keyword, Long categoryId, Equipment.Status status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return equipmentRepository.searchAndFilter(cleanKeyword, categoryId, status);
    }

    public List<Equipment> getAllRentableEquipments() {
        return equipmentRepository.findByStatusNotOrderByCategoryIdAscNameAsc(Equipment.Status.MAINTENANCE);
    }

    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị với ID: " + id));
    }

    @Transactional
    public Equipment saveEquipment(Equipment equipment, Long categoryId) {
        Category category = getCategoryById(categoryId);
        equipment.setCategory(category);

        String code = equipment.getCode().trim().toUpperCase();
        Optional<Equipment> existing = equipmentRepository.findByCodeIgnoreCase(code);
        if (existing.isPresent() && !existing.get().getId().equals(equipment.getId())) {
            throw new IllegalArgumentException("Mã thiết bị '" + code + "' đã tồn tại trong hệ thống!");
        }
        equipment.setCode(code);
        equipment.setName(equipment.getName().trim());
        equipment.setBrand(equipment.getBrand().trim());

        if (equipment.getStatus() == null) {
            equipment.setStatus(Equipment.Status.AVAILABLE);
        }
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        Equipment eq = getEquipmentById(id);
        if (eq.getStatus() == Equipment.Status.RENTED) {
            throw new IllegalArgumentException("Không thể xóa thiết bị '" + eq.getName() + "' vì đang trong trạng thái cho thuê!");
        }
        if (rentalOrderItemRepository.existsByEquipmentId(id)) {
            throw new IllegalArgumentException("Thiết bị '" + eq.getName() + "' đã có lịch sử phiếu thuê. Vui lòng chuyển sang trạng thái 'Bảo trì / Hỏng' thay vì xóa!");
        }
        equipmentRepository.delete(eq);
    }
}
