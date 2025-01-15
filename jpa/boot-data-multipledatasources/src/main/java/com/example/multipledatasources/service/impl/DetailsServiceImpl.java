package com.example.multipledatasources.service.impl;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.exception.CustomServiceException;
import com.example.multipledatasources.exception.MemberNotFoundException;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import com.example.multipledatasources.service.DetailsService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DetailsServiceImpl implements DetailsService {

    private final CardHolderRepository cardHolderRepository;
    private final MemberRepository memberRepository;
    private final Executor asyncExecutor;

    public DetailsServiceImpl(
            CardHolderRepository cardHolderRepository,
            MemberRepository memberRepository,
            @Qualifier("applicationTaskExecutor") Executor asyncExecutor) {
        this.cardHolderRepository = cardHolderRepository;
        this.memberRepository = memberRepository;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public ResponseDto getDetails(String memberId) throws CustomServiceException {
        boolean exists = memberRepository.existsByMemberIdIgnoreCase(memberId);
        if (exists) {
            CompletableFuture<Optional<CardHolder>> cardHolderFuture =
                    CompletableFuture.supplyAsync(() -> cardHolderRepository.findByMemberId(memberId), asyncExecutor);
            CompletableFuture<Optional<Member>> memberFuture =
                    CompletableFuture.supplyAsync(() -> memberRepository.findByMemberId(memberId), asyncExecutor);

            CompletableFuture<ResponseDto> responseFuture = cardHolderFuture.thenCombine(
                    memberFuture, (cardHolder, member) -> mapToResponse(cardHolder, member, memberId));

            try {
                return responseFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CustomServiceException(
                        "Operation interrupted while fetching details for member: " + memberId, e);
            } catch (ExecutionException e) {
                throw new CustomServiceException("Failed to fetch details for member: " + memberId, e.getCause());
            } catch (TimeoutException e) {
                throw new CustomServiceException(
                        "Operation timed out while fetching details for member: " + memberId,
                        e,
                        HttpStatus.REQUEST_TIMEOUT);
            }
        } else {
            throw new MemberNotFoundException("Member with memberId " + memberId + " not Found");
        }
    }

    private ResponseDto mapToResponse(Optional<CardHolder> cardHolderOpt, Optional<Member> memberOpt, String memberId) {
        String cardNumber = cardHolderOpt.map(CardHolder::getCardNumber).orElse(null);
        String name = memberOpt.map(Member::getName).orElse(null);
        return new ResponseDto(memberId, cardNumber, name);
    }
}
