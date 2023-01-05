# Marketplace Connect Flow Example
## Introduction
The objective of this code sample is to help partners to make their Symphony bot compliant with the requirements of the **Marketplace Connect flow**.

It is based on the Java BDK. If you are not familiar with the Symphony Java BDK, please have a look here: https://github.com/finos/symphony-bdk-java

## Requirements for the Connect flow
The requirements of the Connect flow are to provide a good user experience to Symphony users who will connect to your bot from the Marketplace.
This means:
- Automatically accept incoming connection requests.
- And automatically send an introduction message when a new user connects to the bot.
  - This message could contain a thank you message & a user mention to drag attention. It would also inform about the next steps required to get access to the service.
  - Optionally, it gives access to a subset of the service or offers a limited time access. This part is not included in the code sample. In that case it also contains info on how to get support as well as how to interact with the bot.

## Get Started
```
git clone
```
Add RSA private key in /rsa/privatekey.pem

Edit application.yaml in /src/main/resources/
- Update host with your pod url
- Update username with your bot username
- Set path to rsa key (default /rsa/privatekey.pem)

Edit source code in ConnectHandler.java with your own parameters for the support email address, sales email address and company name. See below:
```
templateValues.put("supportemail", "thibault.chays@symphony.com");
templateValues.put("salesemail", "dimiter.georgiev@symphony.com");
templateValues.put("company", "Awesome Company, LLC");
```

You may also want to add steps to grant the user a limited time access to the service, or a freemium access, in the handleUserConnect() function, see below:
```
  public void handleUserConnect(Long userId){
        // Accept incoming connection request
        this.connectionService.acceptConnection(userId);
        
        // Create 1-1 chat with the user
        Stream stream = this.streamService.create(userId);
        
        // Send the welcome message
        sendWelcomeMessage(userId, stream);

        // Add here steps to give a limited time access to the service if applicable
        // grantLimitedAccess(userId);
    }
```

Regarding build & dependencies, please look at https://github.com/finos/symphony-bdk-java

## Disclaimer
This code sample is only an example. It is not meant to be directly used in a production environment. 

## Start the bot
Once your bot is running (main class: BotApplication.java), send a connection request from an external user to your bot. 

The bot will accept the connection automatically and send the introduction message, as well as the list of available commands. 

If you don't have access to an external pod or a test pod, it is possible to get a development access to our develop & develop2 pods to test usage in external rooms.

