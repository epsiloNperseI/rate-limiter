package dev.epsilon.ratelimiter.demo.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientKey;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private Instant blockedAt;

    @Column(nullable = false)
    private int limitValue;

    @Column(nullable = false)
    private int windowSeconds;
}