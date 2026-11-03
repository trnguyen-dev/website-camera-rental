package com.camerarental.repository;

import com.camerarental.entity.RentalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {

    List<RentalOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT DISTINCT o FROM RentalOrder o JOIN o.items i WHERE " +
           "i.equipment.id = :equipmentId AND " +
           "o.status = 'ACTIVE' AND " +
           "o.startDate <= :endDate AND o.expectedReturnDate >= :startDate")
    List<RentalOrder> findConflictingOrdersForEquipment(@Param("equipmentId") Long equipmentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT o FROM RentalOrder o WHERE " +
           "o.status = 'ACTIVE' AND o.expectedReturnDate < :today " +
           "ORDER BY o.expectedReturnDate ASC")
    List<RentalOrder> findOverdueOrders(@Param("today") LocalDate today);

    @Query("SELECT o FROM RentalOrder o WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.customer.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    List<RentalOrder> searchOrders(@Param("keyword") String keyword,
                                   @Param("status") RentalOrder.Status status);

    @Query("SELECT COUNT(i) > 0 FROM RentalOrder o JOIN o.items i WHERE " +
           "i.equipment.id = :equipmentId AND o.status = 'ACTIVE'")
    boolean hasActiveOrdersForEquipment(@Param("equipmentId") Long equipmentId);
}
