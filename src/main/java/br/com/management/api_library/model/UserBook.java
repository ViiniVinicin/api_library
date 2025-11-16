package br.com.management.api_library.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false)
    private ReadingStatus readingStatus;
    private Double rating;
    private String review;
    private boolean isFavorite;
    private int currentPage;

}
