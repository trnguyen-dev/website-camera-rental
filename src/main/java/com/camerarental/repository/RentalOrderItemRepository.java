package com.camerarental.repository;

import com.camerarental.entity.RentalOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RentalOrderItemRepository extends JpaRepository<RentalOrderItem, Long> {

    @Query("SELECT i.equipment.id, i.equipment.code, i.equipment.name, i.equipment.brand, " +
           "i.equipment.category.name, COUNT(i.id), COALESCE(SUM(i.subTotal), 0) " +
           "FROM RentalOrderItem i " +
           "WHERE i.rentalOrder.status <> 'CANCELLED' " +
           "GROUP BY i.equipment.id, i.equipment.code, i.equipment.name, i.equipment.brand, i.equipment.category.name " +
           "ORDER BY COUNT(i.id) DESC, SUM(i.subTotal) DESC")
    List<Object[]> findTopRentedEquipments();

    boolean existsByEquipmentId(Long equipmentId);
}
