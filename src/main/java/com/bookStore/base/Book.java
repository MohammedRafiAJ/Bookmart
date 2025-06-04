package com.bookStore.base;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("author")
    private String author;
    @JsonProperty("published_year")
    private int published_year;
    @JsonProperty("book_summary")
    private String book_summary;
    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    public Book(String name, String author, int published_year, String book_summary, String coverImageUrl) {
        this.id = (int)(System.currentTimeMillis() % 100000); // Generate unique ID
        this.name = name;
        this.author = author;
        this.published_year = published_year;
        this.book_summary = book_summary;
        this.coverImageUrl = coverImageUrl;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", published_year=" + published_year +
                ", book_summary='" + book_summary + '\'' +
                ", cover_image_url='" + coverImageUrl + '\'' +
                '}';
    }
}
