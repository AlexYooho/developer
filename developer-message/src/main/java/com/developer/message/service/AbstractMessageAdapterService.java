package com.developer.message.service;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.*;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMessageAdapterService implements MessageService{

    @Override
    public MessageConversationTypeEnum messageMainType() {
        return null;
    }

    @Override
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> withdrawMessage(WithdrawMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<List<QueryHistoryMessageResponseDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        return null;
    }

    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> friendApplyAcceptMessage(Long receiverId) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> friendApplyRejectMessage(Long receiverId,String rejectReason) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        return null;
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return null;
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> sendJoinGroupInviteMessage(List<Long> memberIds,String groupName,String inviterName,String groupAvatar) {
        return null;
    }

    /*
    发起支付
     */
    public DeveloperResult<Boolean> invokePay(Long messageId, RpcClient rpcClient,SendMessageRequestDTO req){
        if (!req.getMessageContentType().equals(MessageContentTypeEnum.TRANSFER) && !req.getMessageContentType().equals(MessageContentTypeEnum.RED_PACKETS)) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo());
        }
        InvokeRedPacketsTransferRequestRpcDTO paymentDto = buildPacketsTransferRequestRpcDTO(req, messageId);
        DeveloperResult<Boolean> execute = RpcExecutor.execute(() -> rpcClient.paymentRpcService.invokeRedPacketsTransfer(paymentDto));
        if (!execute.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), execute.getMsg());
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    支付rpc参数DTO
     */
    private static InvokeRedPacketsTransferRequestRpcDTO buildPacketsTransferRequestRpcDTO(SendMessageRequestDTO req, Long messageId) {
        InvokeRedPacketsTransferRequestRpcDTO paymentDto = new InvokeRedPacketsTransferRequestRpcDTO();
        paymentDto.setPaymentType(req.getPaymentInfoDTO().getPaymentType());
        paymentDto.setPaymentAmount(req.getPaymentInfoDTO().getPaymentAmount());
        paymentDto.setTargetId(req.getTargetId());
        paymentDto.setRedPacketsTotalCount(req.getPaymentInfoDTO().getRedPacketsTotalCount());
        paymentDto.setRedPacketsType(req.getPaymentInfoDTO().getRedPacketsType());
        paymentDto.setMessageId(messageId);
        paymentDto.setPaymentChannel(PaymentChannelEnum.FRIEND);
        return paymentDto;
    }
}
