package com.symphony.connectbot;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;

import com.symphony.bdk.core.service.connection.ConnectionService;
import com.symphony.bdk.core.service.stream.*;
import com.symphony.bdk.core.service.user.UserService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.gen.api.model.V4ConnectionAccepted;
import com.symphony.bdk.gen.api.model.V4ConnectionRequested;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.symphony.bdk.spring.annotation.Slash;
import com.symphony.bdk.spring.events.RealTimeEvent;
import com.symphony.bdk.template.api.Template;
import com.symphony.bdk.gen.api.model.MemberInfo;
import com.symphony.bdk.gen.api.model.Stream;

@Component
public class ConnectHandler {
    private final MessageService messageService;
    private final StreamService streamService;
    private final ConnectionService connectionService;
    private final Template templateWelcome;
    private final Template templateHelp;

    public ConnectHandler(MessageService messageService, StreamService streamService, ConnectionService connectionService){
        this.messageService = messageService;
        this.templateWelcome = messageService.templates().newTemplateFromClasspath("/templates/welcome.ftl");
        this.templateHelp = messageService.templates().newTemplateFromClasspath("/templates/helpcommands.ftl");
        
        this.streamService = streamService;
        this.connectionService = connectionService;
    }
    @Slash(value = "/help",mentionBot =  false)
    public void onConnect(CommandContext context){

        final Message messageHelp = Message.builder().template(this.templateHelp).build();
        this.messageService.send(context.getStreamId(), messageHelp);

    }


    @EventListener
    public void onUserConnect(RealTimeEvent<V4ConnectionRequested> event)
    {
        final Long userId = event.getInitiator().getUser().getUserId();
        this.connectionService.acceptConnection(userId);
        Stream streamId = this.streamService.create(userId);
        List<MemberInfo> members = this.streamService.listRoomMembers(streamId.getId());
        
        Long botUserId = (members.get(0).getId()!= userId?members.get(0).getId():members.get(1).getId());
        HashMap<String, String> templateValues = new HashMap<String, String>();
        templateValues.put("userid", Long.toString(userId));
        templateValues.put("botuserid", Long.toString(botUserId));
        templateValues.put("supportemail", "thibault.chays@symphony.com");
        templateValues.put("salesemail", "dimiter.georgiev@symphony.com");
        
        templateValues.put("company", "Awesome Company, LLC");

        final Message messageWelcome = Message.builder().template(this.templateWelcome, templateValues).build();
        final Message messageHelp = Message.builder().template(this.templateHelp).build();

        this.messageService.send(streamId.getId(), messageWelcome);
        this.messageService.send(streamId.getId(), messageHelp);
    }

}
