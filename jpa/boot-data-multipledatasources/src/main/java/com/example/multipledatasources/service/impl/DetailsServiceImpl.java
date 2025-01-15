package com.example.multipledatasources.service.impl;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.exception.CustomServiceException;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import com.example.multipledatasources.service.DetailsService;
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
            @Qualifier("applicationTaskExecutor") Executor asyncExecutor) {
        this.cardHolderRepository = cardHolderRepository;
        this.memberRepository = memberRepository;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public ResponseDto getDetails(String memberId) throws CustomServiceException {
        CompletableFuture<ResponseDto> responseFuture = CompletableFuture.supplyAsync(
                        () -> cardHolderRepository.findByMemberId(memberId), asyncExecutor)
                .thenCombineAsync(
                        CompletableFuture.supplyAsync(() -> memberRepository.findByMemberId(memberId), asyncExecutor),
                        (cardHolderOpt, memberOpt) -> {
                            if (cardHolderOpt.isPresent() && memberOpt.isPresent()) {
                                return new ResponseDto(
                                        memberId,
                                        cardHolderOpt.get().getCardNumber(),
                                        memberOpt.get().getName());
                            } else {
                                return new ResponseDto(memberId, null, null);
                            }
                        },
                        asyncExecutor);

        try {
            return responseFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomServiceException("Thread was interrupted while fetching details", e.getCause());
        } catch (ExecutionException e) {
            throw new CustomServiceException("Execution exception occurred while fetching details", e.getCause());
        }
    }
}
