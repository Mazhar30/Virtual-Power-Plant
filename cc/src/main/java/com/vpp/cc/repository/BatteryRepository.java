package com.vpp.cc.repository;

import com.vpp.cc.model.Battery;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BatteryRepository extends ReactiveMongoRepository<Battery, String> {

    @Query("{ 'capacity': { $lt: ?0 } }")
    Flux<Battery> findByCapacity(Long capacity);

    @Query("{ 'postcode': { $gte: ?0, $lte: ?1 }, 'capacity': { $gte: ?2, $lte: ?3 } }")
    Flux<Battery> findByPostcodeRangeAndCapacity(String start, String end, Long minCapacity, Long maxCapacity);
}