package com.example.multipledatasources.repository.cardholder;

import com.example.multipledatasources.model.cardholder.CardHolder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardHolderRepository extends JpaRepository<CardHolder, Long> {

    Optional<CardHolder> findByMemberId(String memberId);
}
