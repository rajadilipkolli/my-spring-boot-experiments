package com.example.multipledatasources.service.impl;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import com.example.multipledatasources.service.DetailsService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DetailsServiceImpl implements DetailsService {

    private final CardHolderRepository cardHolderRepository;
    private final MemberRepository memberRepository;
    private final Executor asyncExecutor;

    public DetailsServiceImpl(
            CardHolderRepository cardHolderRepository,
            MemberRepository memberRepository,
            @Qualifier("taskExecutor") Executor asyncExecutor) {
        this.cardHolderRepository = cardHolderRepository;
        this.memberRepository = memberRepository;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public ResponseDto getDetails(String memberId) {
        CompletableFuture<Optional<CardHolder>> cardHolderFuture =
                CompletableFuture.supplyAsync(() -> cardHolderRepository.findByMemberId(memberId), asyncExecutor);

        CompletableFuture<Optional<Member>> memberFuture =
                CompletableFuture.supplyAsync(() -> memberRepository.findByMemberId(memberId), asyncExecutor);

        try {
            CompletableFuture.allOf(cardHolderFuture, memberFuture).join();
            Optional<CardHolder> cardHolder = cardHolderFuture.get();
            Optional<Member> member = memberFuture.get();

            if (cardHolder.isPresent() && member.isPresent()) {
                return new ResponseDto(
                        memberId, cardHolder.get().getCardNumber(), member.get().getName());
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching details", e);
        }
        return new ResponseDto(memberId, null, null);
    }
}
