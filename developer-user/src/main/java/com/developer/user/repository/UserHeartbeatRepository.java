package com.developer.user.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.user.mappers.UserHeartbeatMapper;
import com.developer.user.pojo.UserHeartbeatPO;
import org.springframework.stereotype.Repository;

@Repository
public class UserHeartbeatRepository extends ServiceImpl<UserHeartbeatMapper, UserHeartbeatPO> {
}
