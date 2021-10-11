package com.example.graphql.querydsl;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQueryDSLApplicationTests extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    assertThat(POSTGRE_SQL_CONTAINER.isRunning()).isTrue();
  }
}
