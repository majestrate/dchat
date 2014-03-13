This document defines the dchat protocol

All dchat messages are well formed json objects containing 3 entries, "VERSION", "DATA" and "TYPE". They are sent as repliable i2p datagrams over i2p.

Version 1:

Defined Message Types:

BROADCAST_CHAT 
 * This message was broadcast to everyone
 * "DATA" contains chat data as string

UNICAST_CHAT 
 * This message was sent directly to you
 * "DATA" contains chat data as string

NICK
 * Requesting Nickname
 * "DATA" is an empty string and NOT null

IDENT
 * Response to NICK message
 * "DATA" contains peer's nickname

PEERS 
 * "DATA" contains a json array of i2p destinations that this peer knows of
 * If "DATA" is and empty json array reply to sender with known peers


On startup client talks to a bootstrap node and requests more peers with a PEERS message. 
