package com.example.Marketten.dto.list;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TitleListDTO {
    private Long titleId;
    private Long tempPostId; // TempPost FK
    private String titleName;
}
