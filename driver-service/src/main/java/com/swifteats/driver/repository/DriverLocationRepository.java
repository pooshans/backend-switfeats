package com.swifteats.driver.repository;

import com.swifteats.driver.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    List<DriverLocation> findByDriverIdOrderByTimestampDesc(Long driverId);

    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId " +
            "AND dl.timestamp >= :startTime ORDER BY dl.timestamp ASC")
    List<DriverLocation> findDriverLocationHistory(
            @Param("driverId") Long driverId,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId " +
            "ORDER BY dl.timestamp DESC LIMIT 1")
    DriverLocation findLatestLocationByDriverId(@Param("driverId") Long driverId);
}
