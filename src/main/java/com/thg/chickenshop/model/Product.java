package com.thg.chickenshop.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    private String imageUrl;

    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    public Product(String name, String description, BigDecimal price,
                   String imageUrl, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }
    @Column(length = 100)
    private String nameEn;

    @Column(length = 1000)
    private String descriptionEn;

    @Column(columnDefinition = "int not null default 100")
    private int stock = 100;

    // Trả về tên theo ngôn ngữ; chưa có tên tiếng Anh thì dùng tên tiếng Việt
    public String localizedName(String lang) {
        return "en".equals(lang) && nameEn != null && !nameEn.isBlank() ? nameEn : name;
    }

    public String localizedDescription(String lang) {
        return "en".equals(lang) && descriptionEn != null && !descriptionEn.isBlank() ? descriptionEn : description;
    }

}