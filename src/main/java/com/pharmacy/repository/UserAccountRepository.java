package com.pharmacy.repository;

import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserAccount> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    Optional<UserAccount> findByIdAndPharmacy(UUID id, Pharmacy pharmacy);
}
