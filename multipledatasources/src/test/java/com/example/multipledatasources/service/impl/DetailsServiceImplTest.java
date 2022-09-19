package com.example.multipledatasources.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.model.cardholder.CardHolder;
import com.example.multipledatasources.model.member.Member;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetailsServiceImplTest {

    @Mock private CardHolderRepository cardHolderRepository;

    @Mock private MemberRepository memberRepository;

    @InjectMocks private DetailsServiceImpl detailsService;

    @Test
    void getDetails() {
        // given
        Member member = new Member();
        member.setName("junit");
        given(memberRepository.findByMemberId("1")).willReturn(Optional.of(member));

        given(cardHolderRepository.findByMemberId("1")).willReturn(Optional.of(new CardHolder()));
        // when
        ResponseDto responseDto = detailsService.getDetails("1");

        // then
        assertThat(responseDto.memberId()).isEqualTo("1");
        assertThat(responseDto.cardNumber()).isNull();
        assertThat(responseDto.memberName()).isEqualTo("junit");
    }
}
