package com.developer.im.model;

import com.developer.im.enums.IMTerminalType;
import lombok.Data;

/**
 * 用户模型
 */
@Data
public class IMUserInfoModel {


    private Long id;

    private IMTerminalType terminal;

}
