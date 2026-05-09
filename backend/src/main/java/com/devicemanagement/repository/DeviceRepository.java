package com.devicemanagement.repository;

import com.devicemanagement.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByStatus(String status);
    List<Device> findByCategory(String category);

    @Query("SELECT d FROM Device d WHERE " +
           "(:name IS NULL OR d.name LIKE %:name%) AND " +
           "(:category IS NULL OR d.category LIKE %:category%) AND " +
           "(:status IS NULL OR d.status = :status)")
    List<Device> searchDevices(@Param("name") String name,
                               @Param("category") String category,
                               @Param("status") String status);
}
