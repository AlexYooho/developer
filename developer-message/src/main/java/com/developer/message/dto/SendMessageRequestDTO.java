package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.payment.PaymentTypeEnum;
import com.developer.framework.enums.payment.RedPacketsTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageRequestDTO {

    /*
    发消息目标对象id
     */
    @JsonProperty("target_id")
    private Long targetId;

    /*
    消息会话类型
     */
    @JsonProperty("message_main_type")
    private MessageConversationTypeEnum messageMainType;

    /*
    消息内容类型
     */
    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    /*
    发送终端类型
     */
    @JsonProperty("terminal_type")
    private TerminalTypeEnum terminalType;

    /*
    消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /**
     * @ 用户id
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;

    /**
     * 引用消息id
     */
    @JsonProperty("reference_id")
    private Long referenceId;

    /*
    客户端消息id
     */
    @JsonProperty("client_msg_id")
    private String clientMsgId;

    /*
    支付信息
     */
    @JsonProperty("payment_info")
    private PaymentInfoDTO paymentInfoDTO;

    @Data
    public static class PaymentInfoDTO{

        @JsonProperty("payment_type")
        private PaymentTypeEnum paymentType;

        @JsonProperty("payment_amount")
        private BigDecimal paymentAmount;

        @JsonProperty("red_packets_total_count")
        private Integer redPacketsTotalCount;

        @JsonProperty("red_packets_type")
        private RedPacketsTypeEnum redPacketsType;

    }

}
