package com.example.multipledatasources.bootstrap;

import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class DataInitializer {

    private final CardHolderRepository cardHolderRepository;
    private final MemberRepository memberRepository;

    DataInitializer(CardHolderRepository cardHolderRepository, MemberRepository memberRepository) {
        this.cardHolderRepository = cardHolderRepository;
        this.memberRepository = memberRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    void loadInitialData() {
        this.cardHolderRepository.deleteAll();
        this.memberRepository.deleteAll();
        log.info("Data Boot strapping started");
        String memberId = "1";

        Member member = new Member();
        member.setMemberId(memberId);
        member.setName("raja");

        CardHolder cardHolder = new CardHolder();
        cardHolder.setMemberId(memberId);
        cardHolder.setCardNumber("1234-5678-9012-3456");

        CardHolder savedCardHolder = this.cardHolderRepository.save(cardHolder);
        log.info("Saved CardHolder :{}", savedCardHolder);

        Member savedMember = this.memberRepository.save(member);
        log.info("Saved Member :{}", savedMember);

        log.info("Data Boot strapping completed");
    }
}
