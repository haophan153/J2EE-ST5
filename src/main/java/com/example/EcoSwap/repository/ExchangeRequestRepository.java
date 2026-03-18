package com.example.EcoSwap.repository;

import com.example.EcoSwap.entity.ExchangeRequest;
import com.example.EcoSwap.entity.ExchangeRequest.ExchangeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    
    List<ExchangeRequest> findByRequesterId(Long requesterId);
    
    List<ExchangeRequest> findByOwnerId(Long ownerId);
    
    Page<ExchangeRequest> findByRequesterId(Long requesterId, Pageable pageable);
    
    Page<ExchangeRequest> findByOwnerId(Long ownerId, Pageable pageable);
    
    List<ExchangeRequest> findByRequesterIdOrOwnerId(Long requesterId, Long ownerId);
    
    @Query("SELECT e FROM ExchangeRequest e WHERE e.id = :id AND (e.requester.id = :requesterId OR e.owner.id = :ownerId)")
    Optional<ExchangeRequest> findByIdAndUserId(@Param("id") Long id, @Param("requesterId") Long requesterId, @Param("ownerId") Long ownerId);

    @Query("""
        SELECT e FROM ExchangeRequest e
        WHERE e.status = :status AND (e.requester.id = :userId OR e.owner.id = :userId)
        """)
    Page<ExchangeRequest> findHistoryByUserAndStatus(@Param("userId") Long userId,
                                                     @Param("status") ExchangeStatus status,
                                                     Pageable pageable);
    
    List<ExchangeRequest> findByStatusAndRequesterIdOrOwnerId(
        ExchangeStatus status, Long requesterId, Long ownerId);
    
    boolean existsByOfferedProductIdOrRequestedProductId(
        Long offeredProductId, Long requestedProductId);
    
    boolean existsByRequestedProductIdAndRequesterId(
        Long requestedProductId, Long requesterId);
    
    long countByOwnerIdAndStatusIn(Long ownerId, List<ExchangeStatus> statuses);
}
