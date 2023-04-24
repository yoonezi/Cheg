package com.likelion.cheg.domain.product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.likelion.cheg.domain.category.Category;
import com.likelion.cheg.web.dto.product.ProductUploadDto;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@ToString(exclude = {"cart","category"})
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonBackReference //순환참조 방지
    @ManyToOne
    @JoinColumn(name="category")
    private Category category; //상품 카테고리

    private String url; //상품_이미지 경로
    private String brand; //상품 브랜드
    private String name; //상품명
    private int price; //상품가격
    private String description; //상품설명
    private LocalDateTime createDate; //날짜

    @PrePersist //db에 insert되기 직전에 실행
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    //카테고리 변경
    public void changeCategory(Category category){
        //이미 카테고리가 있을 경우 관계 제거
        if(this.category != null){
            this.category.getProducts().remove(this);
        }
        this.category = category;
        if(category != null){
            category.getProducts().add(this);
        }
    }

    //상품 생성 메서드
    public static Product createProduct(Category category, String brand, String name, String description, int price, String url){
        Product product = Product.builder()
                .category(category)
                .brand(brand)
                .name(name)
                .description(description)
                .price(price)
                .url(url)
                .build();
        return product;
    }
}
