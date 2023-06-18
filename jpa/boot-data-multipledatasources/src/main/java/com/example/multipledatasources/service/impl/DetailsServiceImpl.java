package com.example.multipledatasources.service.impl;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.model.cardholder.CardHolder;
import com.example.multipledatasources.model.member.Member;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import com.example.multipledatasources.service.DetailsService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetailsServiceImpl implements DetailsService {

    private final CardHolderRepository cardHolderRepository;
    private final MemberRepository memberRepository;

    @Override
    public ResponseDto getDetails(String memberId) {
        Optional<CardHolder> cardHolderByMemberId =
                this.cardHolderRepository.findByMemberId(memberId);
        Optional<Member> memberByMemberId = this.memberRepository.findByMemberId(memberId);
        if (cardHolderByMemberId.isPresent() && memberByMemberId.isPresent()) {
            return new ResponseDto(
                    memberId,
                    cardHolderByMemberId.get().getCardNumber(),
                    memberByMemberId.get().getName());
        }
        return new ResponseDto(null, null, null);
    }
}
