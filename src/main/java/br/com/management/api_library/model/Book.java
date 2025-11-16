package br.com.management.api_library.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String author;
    private String publisher;
    private String genre;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String language;

    @Column(unique = true)
    private String isbn;

    private int pages;
    private String imageUrl;

}
