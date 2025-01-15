package com.example.multipledatasources.repository.member;

import com.example.multipledatasources.entities.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByMemberIdIgnoreCase(String memberId);

    boolean existsByMemberIdIgnoreCase(String memberId);
}
