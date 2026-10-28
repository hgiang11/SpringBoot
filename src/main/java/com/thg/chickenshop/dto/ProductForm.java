package com.thg.chickenshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;   // MỚI

import java.math.BigDecimal;

@Getter @Setter
public class ProductForm {

    private Long id;   // null = thêm mới, có giá trị = sửa

    @NotBlank(message = "Tên món không được để trống")
    @Size(max = 100, message = "Tên món tối đa 100 ký tự")
    private String name;

    @Size(max = 100, message = "Tên tiếng Anh tối đa 100 ký tự")
    private String nameEn;

    @Size(max = 1000, message = "Mô tả tiếng Anh tối đa 1000 ký tự")
    private String descriptionEn;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "1000", message = "Giá tối thiểu là 1.000 đ")
    private BigDecimal price;

    private String imageUrl;
    private MultipartFile image;   // MỚI: file ảnh người dùng chọn (có thể để trống)

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private boolean available = true;
}