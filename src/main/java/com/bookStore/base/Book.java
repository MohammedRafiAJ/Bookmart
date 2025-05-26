package com.bookStore.base;

public class Book {
    private int id;
    private String name;
    private String author;
    private int published_year;
    private String book_summary;

    public Book(String name, String author, int published_year, String book_summary) {
        this.id = (int)(System.currentTimeMillis() % 100000); // Generate unique ID
        this.name = name;
        this.author = author;
        this.published_year = published_year;
        this.book_summary = book_summary;
    }

    // Setter method for ID
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublishedYear() {
        return published_year;
    }

    public String getBookSummary() {
        return book_summary;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", published_year=" + published_year +
                ", book_summary='" + book_summary + '\'' +
                '}';
    }
}
