package com.example.multipledatasources.repository.member;

import com.example.multipledatasources.model.member.Member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
}
