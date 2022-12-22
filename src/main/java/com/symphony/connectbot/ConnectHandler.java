package com.symphony.connectbot;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;

import com.symphony.bdk.core.service.connection.ConnectionService;
import com.symphony.bdk.core.service.connection.constant.ConnectionStatus;
import com.symphony.bdk.core.service.stream.*;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.core.service.session.SessionService;
import com.symphony.bdk.gen.api.model.V4ConnectionRequested;

import java.util.HashMap;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.symphony.bdk.spring.annotation.Slash;
import com.symphony.bdk.spring.events.RealTimeEvent;
import com.symphony.bdk.template.api.Template;

import com.symphony.bdk.gen.api.model.Stream;
import com.symphony.bdk.gen.api.model.UserConnection;

@Component
public class ConnectHandler {
    private final MessageService messageService;
    private final StreamService streamService;
    private final ConnectionService connectionService;
    private final SessionService sessionService;
    private final Template templateWelcome;
    private final Template templateHelp;

    public ConnectHandler(MessageService messageService, StreamService streamService, ConnectionService connectionService, SessionService sessionService){
        this.messageService = messageService;
        this.templateWelcome = messageService.templates().newTemplateFromClasspath("/templates/welcome.ftl");
        this.templateHelp = messageService.templates().newTemplateFromClasspath("/templates/helpcommands.ftl");
        
        this.streamService = streamService;
        this.connectionService = connectionService;
        this.sessionService = sessionService;
        
        // handle potential connection requests received while the bot was offline (at bootstrap)
        bootstrapUserConnect();
    }
    @Slash(value = "/help",mentionBot =  false)
    public void onHelp(CommandContext context){
        final Message messageHelp = Message.builder().template(this.templateHelp).build();
        this.messageService.send(context.getStreamId(), messageHelp);
    }

    /**
     * Event listener of incoming connection requests
     * @param event datafeed event context object
     */
    @EventListener
    public void onUserConnect(RealTimeEvent<V4ConnectionRequested> event)
    {
        final Long userId = event.getInitiator().getUser().getUserId();
        handleUserConnect(userId);
    }

    /**
     * Handle connection requests received while the bot was offline (connection event not received from the datafeed)
     */
    public void bootstrapUserConnect(){
        List<UserConnection> list = connectionService.listConnections(ConnectionStatus.PENDING_INCOMING, null);
        for (UserConnection userConnection : list) {
            handleUserConnect(userConnection.getUserId());
        }
    }

    /**
     * Handle a connection request: Accept the connection then send the welcome message.
     * @param userId User id of the user who sent the connection request
     */
    public void handleUserConnect(Long userId){
        this.connectionService.acceptConnection(userId);
        Stream stream = this.streamService.create(userId);
        sendWelcomeMessage(userId, stream);
    }

    /**
     * Send a welcome message to the user based on a template.
     * @param userId User id of the user who sent the connection request
     * @param stream Chat conversation with this user
     */
    public void sendWelcomeMessage(Long userId, Stream stream){
        Long botUserId = this.sessionService.getSession().getId();
        
        HashMap<String, String> templateValues = new HashMap<String, String>();
        templateValues.put("userid", Long.toString(userId));
        templateValues.put("botuserid", Long.toString(botUserId));
        templateValues.put("supportemail", "thibault.chays@symphony.com");
        templateValues.put("salesemail", "dimiter.georgiev@symphony.com");
        templateValues.put("company", "Awesome Company, LLC");
        
        final Message messageWelcome = Message.builder().template(this.templateWelcome, templateValues).build();
        final Message messageHelp = Message.builder().template(this.templateHelp).build();

        this.messageService.send(stream.getId(), messageWelcome);
        this.messageService.send(stream.getId(), messageHelp);
    }
}
