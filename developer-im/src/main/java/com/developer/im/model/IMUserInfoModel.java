package com.developer.im.model;

import com.developer.framework.enums.IMTerminalTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户模型
 */
@Data
@AllArgsConstructor
public class IMUserInfoModel {


    private Long id;

    private IMTerminalTypeEnum terminal;

}
