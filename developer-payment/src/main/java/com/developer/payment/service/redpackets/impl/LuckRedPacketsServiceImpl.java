package com.developer.payment.service.redpackets.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.service.redpackets.RedPacketsService;
import org.springframework.stereotype.Service;

@Service
public class LuckRedPacketsServiceImpl implements RedPacketsService {
    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        return null;
    }
}
