package com.developer.framework.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.developer.framework.exception.EmailException;
import javax.mail.internet.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

@Service
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void send(String name, String form, String to, String subject, String content, Boolean isHtml, String cc, String bcc, List<File> files) {

        if (StringUtils.isAnyBlank(form, to, subject, content)) {
            throw new EmailException(-1,"发送人,接收人,主题,内容均不可为空");
        }
        try {
            //true表示支持复杂类型
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
            //邮件发信人
            messageHelper.setFrom(String.valueOf(new InternetAddress(name + "<" + form + ">")));
            //邮件收信人
            messageHelper.setTo(to.split(","));
            //邮件主题
            messageHelper.setSubject(subject);
            //邮件内容
            messageHelper.setText(content, isHtml);
            //抄送
            if (!StringUtils.isEmpty(cc)) {
                messageHelper.setCc(cc.split(","));
            }
            //密送
            if (!StringUtils.isEmpty(bcc)) {
                messageHelper.setCc(bcc.split(","));
            }
            //添加邮件附件
            if (CollectionUtil.isNotEmpty(files)) {
                for (File file : files) {
                    messageHelper.addAttachment(file.getName(), file);
                }
            }
            // 邮件发送时间
            messageHelper.setSentDate(new Date());
            //正式发送邮件
            mailSender.send(messageHelper.getMimeMessage());
        } catch (Exception e) {
            throw new EmailException(-1,"邮件发送失败:"+e);
        }
    }

    public void sendText(String to, String subject, String content) {
        this.send("DeveloperChat", "1758490525@qq.com", to, subject, content, false, null, null, null);
    }

    public void sendHtml(String to, String subject, String content) {
        this.send("DeveloperChat", "1758490525@qq.com", to, subject, content, true, null, null, null);
    }

    /**
     * 发送验证码
     */
    public Integer sendAuthorizationCode(){
        Context context = new Context();
        context.setVariable("nickname", "水友");

        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        context.setVariable("authCode", code);
        String content = templateEngine.process("commentTemplate.html", context);
        sendHtml("17608478306@163.com", "Verify your identity", content);
        return code;
    }
}