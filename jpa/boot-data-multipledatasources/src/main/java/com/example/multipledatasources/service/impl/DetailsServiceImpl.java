package com.example.multipledatasources.service.impl;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.exception.CustomServiceException;
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
        var cardHolderFuture =
                CompletableFuture.supplyAsync(() -> cardHolderRepository.findByMemberId(memberId), asyncExecutor);
        var memberFuture =
                CompletableFuture.supplyAsync(() -> memberRepository.findByMemberId(memberId), asyncExecutor);

        CompletableFuture<ResponseDto> responseFuture = cardHolderFuture.thenCombine(
                memberFuture, (cardHolder, member) -> mapToResponse(cardHolder, member, memberId));

        try {
            return responseFuture.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomServiceException(
                    "Operation interrupted while fetching details for member: " + memberId,
                    e,
                    HttpStatus.REQUEST_TIMEOUT);
        } catch (ExecutionException e) {
            throw new CustomServiceException(
                    "Failed to fetch details for member: " + memberId, e.getCause(), HttpStatus.REQUEST_TIMEOUT);
        } catch (TimeoutException e) {
            throw new CustomServiceException(
                    "Operation timed out while fetching details for member: " + memberId,
                    e,
                    HttpStatus.REQUEST_TIMEOUT);
        }
    }

    private ResponseDto mapToResponse(Optional<CardHolder> cardHolderOpt, Optional<Member> memberOpt, String memberId) {
        if (cardHolderOpt.isPresent() && memberOpt.isPresent()) {
            CardHolder cardHolder = cardHolderOpt.get();
            Member member = memberOpt.get();
            return new ResponseDto(member.getMemberId(), cardHolder.getCardNumber(), member.getName());
        }
        return new ResponseDto(memberId, null, null);
    }
}
