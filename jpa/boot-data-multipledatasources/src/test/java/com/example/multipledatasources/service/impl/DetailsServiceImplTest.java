package com.example.multipledatasources.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.exception.CustomServiceException;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetailsServiceImplTest {

    @Mock
    private CardHolderRepository cardHolderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Executor asyncExecutor;

    private DetailsServiceImpl detailsService;

    @BeforeEach
    void setUp() {
        detailsService = new DetailsServiceImpl(cardHolderRepository, memberRepository, asyncExecutor);
        // Configure executor to run tasks immediately in the same thread
        // Configure executor to run tasks immediately in the same thread
        doAnswer(invocation -> {
                    ((Runnable) invocation.getArgument(0)).run();
                    return null;
                })
                .when(asyncExecutor)
                .execute(any(Runnable.class));
    }

    @Test
    void getDetails_WhenBothEntitiesExist_ShouldReturnCompleteResponse() {
        // Arrange
        String memberId = "test-member-id";
        CardHolder cardHolder = new CardHolder();
        cardHolder.setCardNumber("1234-5678");
        Member member = new Member();
        member.setName("John Doe");
        member.setMemberId(memberId);

        given(cardHolderRepository.findByMemberId(eq(memberId))).willReturn(Optional.of(cardHolder));
        given(memberRepository.findByMemberId(eq(memberId))).willReturn(Optional.of(member));

        // Act
        ResponseDto result = detailsService.getDetails(memberId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.cardNumber()).isEqualTo("1234-5678");
        assertThat(result.memberName()).isEqualTo("John Doe");
        verify(asyncExecutor, times(2)).execute(any(Runnable.class));
    }

    @Test
    void getDetails_WhenEntitiesDoNotExist_ShouldReturnEmptyResponse() {
        // Arrange
        String memberId = "non-existent-id";
        given(cardHolderRepository.findByMemberId(eq(memberId))).willReturn(Optional.empty());
        given(memberRepository.findByMemberId(eq(memberId))).willReturn(Optional.empty());

        // Act
        ResponseDto result = detailsService.getDetails(memberId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.cardNumber()).isNull();
        assertThat(result.memberName()).isNull();
    }

    @Test
    void getDetails_WhenCardHolderMissing_ShouldReturnEmptyResponse() {
        // Arrange
        String memberId = "test-member-id";
        Member member = new Member();
        member.setName("John Doe");

        given(cardHolderRepository.findByMemberId(eq(memberId))).willReturn(Optional.empty());
        given(memberRepository.findByMemberId(eq(memberId))).willReturn(Optional.of(member));

        // Act
        ResponseDto result = detailsService.getDetails(memberId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.cardNumber()).isNull();
        assertThat(result.memberName()).isNull();
    }

    @Test
    void getDetails_WhenRepositoryThrowsException_ShouldPropagateError() {
        // Arrange
        String memberId = "test-member-id";
        RuntimeException expectedException = new RuntimeException("Database error");
        given(cardHolderRepository.findByMemberId(eq(memberId))).willThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> detailsService.getDetails(memberId))
                .isInstanceOf(CustomServiceException.class)
                .hasMessage("Failed to fetch details for member: test-member-id")
                .hasCause(expectedException);
    }
}
