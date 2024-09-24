package com.developer.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OnlineTerminalDTO {
    private Long userId;

    private List<Integer> terminals;
}
