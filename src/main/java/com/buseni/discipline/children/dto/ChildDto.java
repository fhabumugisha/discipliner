package com.buseni.discipline.children.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChildDto {

    private String id;

    @NotBlank(message = "{validation.child.name.required}")
    private String name;

    @NotNull(message = "{validation.child.age.required}")
    @Min(value = 0, message = "{validation.child.age.min}")
    private Integer age;

}