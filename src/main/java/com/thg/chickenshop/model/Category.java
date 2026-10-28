package com.thg.chickenshop.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    @Column(length = 50)
    private String nameEn;

    public String localizedName(String lang) {
        return "en".equals(lang) && nameEn != null && !nameEn.isBlank() ? nameEn : name;
    }

}
