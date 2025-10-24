package com.example.multipledatasources.configuration;

import com.example.multipledatasources.entities.member.Member;
import com.example.multipledatasources.repository.member.MemberRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = Member.class)
@EnableJpaRepositories(basePackageClasses = MemberRepository.class)
class MemberDataSourceConfiguration {}
