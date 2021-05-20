package network.server.fixFingers;

import network.Main;
import network.etc.MessageType;
import network.etc.Singleton;
import network.message.MessageLookup;
import network.node.InfoNode;
import network.server.com.SendMessage;

import java.math.BigInteger;

public class FixFingers implements Runnable{

    @Override
    public void run() {
        Integer next = Main.chordNode.getNext();
        next += 1;

        Main.chordNode.setNext(next);
        if (next > Singleton.m)
            next = 1;


        BigInteger currentId = Main.chordNode.getId();
        BigInteger nextId = new BigInteger(String.valueOf((long)Math.pow(2, next-1)));
        BigInteger targetId = currentId.add(nextId);

        MessageLookup messageLookup = new MessageLookup(Main.chordNode.getInfoNode(), targetId, MessageType.FIX_FINGERS);
        InfoNode successor = Main.chordNode.getSuccessor();
        new SendMessage(successor.getIp(), successor.getPort(), messageLookup).start();
    }
}
