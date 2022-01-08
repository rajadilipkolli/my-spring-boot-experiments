package com.example.multipledatasources.bootstrap;

import com.example.multipledatasources.model.cardholder.CardHolder;
import com.example.multipledatasources.model.member.Member;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitilizer {

    private final CardHolderRepository cardHolderRepository;
    private final MemberRepository memberRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    void loadInitialData() {

        String memberId = "1";

        Member member =  new Member();
        member.setMemberId(memberId);
        member.setName("raja");

        CardHolder cardHolder = new CardHolder();
        cardHolder.setMemberId(memberId);
        cardHolder.setCardNumber("1234-5678-9012-3456");

        this.cardHolderRepository.save(cardHolder);
        this.memberRepository.save(member);
        log.info("Inserted data into both databases");
    }
}
