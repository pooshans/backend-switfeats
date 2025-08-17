package com.swifteats.driver.repository;

import com.swifteats.driver.model.Driver;
import com.swifteats.driver.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByEmail(String email);

    // Find drivers by status - we'll implement spatial filtering in the service
    // layer
    List<Driver> findByStatus(DriverStatus status);

    // This is a dummy implementation to satisfy Spring Data JPA's reflection
    // mechanism
    // It will never be called in our code
    default List<Driver> findNearbyDriversByStatus(Double longitude, Double latitude, Double radiusInMeters,
            DriverStatus status) {
        return Collections.emptyList();
    }
}
