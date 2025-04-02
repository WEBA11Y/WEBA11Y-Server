package com.weba11y.server.jpa.repository;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.domain.enums.InspectionItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {
    @Query(value = "SELECT ir FROM InspectionResult ir " +
            "WHERE ir.inspectionUrl.id = :inspectionUrlId " +
            "AND ir.createDate = :createDate")
    List<InspectionResult> findInspectionResultsByUrlIdAndCreateDate(@Param("inspectionUrlId") Long inspectionUrlId,
                                                                     @Param("createDate") LocalDate createDate);

    @Query(value = "SELECT DISTINCT CAST(ir.createDate AS LocalDate) " +
            "FROM InspectionResult ir WHERE ir.inspectionUrl.id = :inspectionUrlId")
    List<LocalDate> findCreateDatesByInspectionUrlId(@Param("inspectionUrlId") Long inspectionUrlId);

    @Query(value = "SELECT ir FROM InspectionResult ir " +
            "WHERE ir.inspectionUrl.id = :urlId " +
            "AND ir.inspectionItems IN :items " +
            "AND DATE(ir.createDate) = :date " +
            "ORDER BY ir.inspectionItems")
    Page<InspectionResult> findByUrlIdAndDateAndItems(Pageable pageable,
                                                      @Param("urlId") Long urlId,
                                                      @Param("date") LocalDate date,
                                                      @Param("items") List<InspectionItems> items);

}