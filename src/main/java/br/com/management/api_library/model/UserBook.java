package br.com.management.api_library.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;

    @Enumerated(EnumType.STRING)
    private ReadingStatus readingStatus; // Sugest: Usar Enum para valores como: WANT_TO_READ, READING, READ, DROPPED
    private Double rating; // Sugest: Valor de 1 a 5
    private String review;
    private boolean isFavorite;
    private int currentPage;

}
