package org.example.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.validationGroup.Create;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Name must be filled")
    private String name;
    @NotBlank(groups = Create.class, message = "Description must be filled")
    private String description;
    @NotNull(groups = Create.class, message = "Available must be filled")
    private Boolean available;
    @JsonIgnore
    private Long owner;
    private Long requestId;
    private List<CommentDto> comments;
}
