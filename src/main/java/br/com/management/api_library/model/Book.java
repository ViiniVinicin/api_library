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

    private String bookTitle;
    private String author;
    private String publisher;
    private String genre;
    private String description;
    private String language;
    private String isbn;
    private int pages;
    private int publicationYear;
    private int availableCopies;
    private boolean isAvailable;

}
