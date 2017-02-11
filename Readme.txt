Simplified Message Queueing (Part I)

A simple message queueing application involves implementation of servers and
clients that communicate with each other to pass messages
1. Exchange Server: responsible for taking messages from Client processes and
placing them in the appropriate queue(s), depending on the ExchangeType used
2. Queue Server: responsible for maintaining a message queue, and returns
a list or a message from the queue to the client
3. Client: reads in user input, communicates with the server to obtain results

Usage:
Create an exchange server: java MessageServer create exchange [exchange name]
Create a queue server: java MessageServer create queue [queue name]
Bind queue to exchange: in terminal running exchange server, type bind [queue name] [queue address] [queue port]
Create a MessageClient: java MessageClient [server address] [server port]
    If Client is connected to exchange:
        Put: put [exchange name] [queue name] [message]
            Note: wild card is supported at beginning, middle, or end of the queue name
            Example: *queue, qu*eue, and queue*
        Subscribe: subscribe [subscription name]
        Publish: publish [exchange name] [subscription name] [message]
    If Client is connected to queue:
        List: list [queue name]
        Get: get [queue name]
