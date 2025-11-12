package com.developer.message.service;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService extends BasicMessageService,InteractiveMessageService,CommonMessageService {

}
