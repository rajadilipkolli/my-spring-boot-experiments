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
import com.example.multipledatasources.exception.MemberNotFoundException;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.example.multipledatasources.repository.member.MemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DetailsServiceImplTest {

    @Mock
    private CardHolderRepository cardHolderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Executor asyncExecutor;

    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;

    @InjectMocks
    private DetailsServiceImpl detailsService;

    private void configureExecutorForSuccess() {
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
        String validVisaCard = "4111111111111111"; // Raw digits format matching ESAPI pattern
        CardHolder cardHolder = new CardHolder();
        cardHolder.setCardNumber(validVisaCard);
        Member member = new Member();
        member.setName("John Doe");
        member.setMemberId(memberId);

        configureExecutorForSuccess();
        given(memberRepository.existsByMemberIdIgnoreCase(memberId)).willReturn(true);
        given(cardHolderRepository.findByMemberId(eq(memberId))).willReturn(Optional.of(cardHolder));
        given(memberRepository.findByMemberIdIgnoreCase(memberId)).willReturn(member);

        // Act
        ResponseDto result = detailsService.getDetails(memberId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.cardNumber()).isEqualTo(validVisaCard);
        assertThat(result.memberName()).isEqualTo("John Doe");
        verify(asyncExecutor, times(2)).execute(taskCaptor.capture());
        List<Runnable> capturedTasks = taskCaptor.getAllValues();
        assertThat(capturedTasks).hasSize(2);
    }

    @Test
    void getDetails_WhenEntitiesDoNotExist_ShouldReturnEmptyResponse() {
        // Arrange
        String memberId = "non-existent-id";
        given(memberRepository.existsByMemberIdIgnoreCase(memberId)).willReturn(false);
        // Act & Assert
        assertThatThrownBy(() -> detailsService.getDetails(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Member with memberId non-existent-id not Found")
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDetails_WhenCardHolderMissing_ShouldReturnEmptyResponse() {
        // Arrange
        String memberId = "test-member-id";
        Member member = new Member();
        member.setName("John Doe");
        member.setMemberId(memberId);

        configureExecutorForSuccess();

        given(memberRepository.existsByMemberIdIgnoreCase(memberId)).willReturn(true);
        given(memberRepository.findByMemberIdIgnoreCase(memberId)).willReturn(member);
        given(cardHolderRepository.findByMemberId(eq(memberId))).willReturn(Optional.empty());

        // Act
        ResponseDto result = detailsService.getDetails(memberId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.cardNumber()).isNull();
        assertThat(result.memberName()).isEqualTo("John Doe");
    }

    @Test
    void getDetails_WhenRepositoryThrowsException_ShouldPropagateError() {
        // Arrange
        String memberId = "test-member-id";
        given(memberRepository.existsByMemberIdIgnoreCase(memberId)).willReturn(true);
        RuntimeException expectedException = new RuntimeException("Database error");
        given(cardHolderRepository.findByMemberId(eq(memberId))).willThrow(expectedException);

        configureExecutorForSuccess();
        // Act & Assert
        assertThatThrownBy(() -> detailsService.getDetails(memberId))
                .isInstanceOf(CustomServiceException.class)
                .hasMessage("Failed to fetch details for member: test-member-id")
                .hasCause(expectedException)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getDetails_WhenOperationTimesOut_ShouldThrowException() {
        // Arrange
        String memberId = "test-member-id";
        given(memberRepository.existsByMemberIdIgnoreCase(memberId)).willReturn(true);
        doAnswer(invocation -> {
                    throw new CustomServiceException(
                            "Operation timed out while fetching details for member: " + memberId,
                            null,
                            HttpStatus.REQUEST_TIMEOUT);
                })
                .when(asyncExecutor)
                .execute(any(Runnable.class));

        // Act & Assert
        assertThatThrownBy(() -> detailsService.getDetails(memberId))
                .isInstanceOf(CustomServiceException.class)
                .hasMessageContaining("Operation timed out")
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT);
    }
}
