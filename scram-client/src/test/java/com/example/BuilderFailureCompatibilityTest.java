/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ongres.scram.client.ChannelBindingPolicy;
import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.util.TlsServerEndpoint;
import org.junit.jupiter.api.Test;

/**
 * {@code build()} failures that 3.2 reported as {@link IllegalArgumentException} keep that type and
 * message, so a caller that catches {@code IllegalArgumentException} around {@code build()} still
 * catches them.
 */
class BuilderFailureCompatibilityTest {

  private static final byte[] CBIND_DATA = {1, 2, 3};

  @Test
  void noKnownMechanismAdvertised() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> builder(Collections.singletonList("SCRAM-UNKNOWN")).build());
    assertEquals("Either a bare or plus mechanism must be present", ex.getMessage());
  }

  @Test
  void onlyPlusAdvertisedAndNoChannelBindingData() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> builder(Collections.singletonList("SCRAM-SHA-256-PLUS")).build());
    assertEquals("A non-PLUS mechanism was not advertised", ex.getMessage());
  }

  @Test
  void onlyPlusAdvertisedAndChannelBindingDisabled() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> builder(Arrays.asList("SCRAM-SHA-256-PLUS"))
            .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, CBIND_DATA)
            .channelBindingPolicy(ChannelBindingPolicy.DISABLE)
            .build());
    assertEquals("A non-PLUS mechanism was not advertised", ex.getMessage());
  }

  private static ScramClient.FinalBuildStage builder(List<String> mechanisms) {
    return ScramClient.builder()
        .advertisedMechanisms(mechanisms)
        .username("user")
        .password("pencil".toCharArray());
  }
}
