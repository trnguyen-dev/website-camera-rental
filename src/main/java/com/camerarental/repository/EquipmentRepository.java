package com.camerarental.repository;

import com.camerarental.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByCodeIgnoreCase(String code);

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:status IS NULL OR e.status = :status) " +
           "ORDER BY e.id DESC")
    List<Equipment> searchAndFilter(@Param("keyword") String keyword,
                                    @Param("categoryId") Long categoryId,
                                    @Param("status") Equipment.Status status);

    List<Equipment> findByStatusNotOrderByCategoryIdAscNameAsc(Equipment.Status excludedStatus);
}
