package konkuk.thip.test;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_table")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL 시퀀스 자동증가
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자
    public TestEntity() {}

    public TestEntity(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getter / Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 생성 직후 자동으로 날짜 세팅하려면 아래와 같이 해도 됨
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
