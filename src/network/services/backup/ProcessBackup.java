package network.services.backup;

import network.Main;
import network.etc.FileHandler;
import network.etc.Logger;
import network.etc.MessageType;
import network.message.MessageBackup;
import network.message.MessageStored;
import network.message.MessageDoneBackup;
import network.node.InfoNode;
import network.server.com.SendMessage;

import java.io.IOException;

public class ProcessBackup implements Runnable {
    MessageBackup message;

    public ProcessBackup(MessageBackup messageBackup) {
        this.message = messageBackup;
    }

    @Override
    public void run() {
        try {
            Logger.ANY(this.getClass().getName(), "Received BACKUP");
            int desiredRepDeg = message.getDesiredRepDeg();
            String filePath = message.getFileName();

            saveFile(filePath, message);
            int actualRepDeg = message.getActualRepDeg() + 1;

            if (actualRepDeg == desiredRepDeg) {
                sendBackupDone(filePath);
            } else {
                sendToSuccessor();
                storedMessageOrigin(filePath);
            }
        }catch(Exception e){
            Logger.ERR(this.getClass().getName(), "Not possible to send backup message");
        }
    }

    public void saveFile(String filePath, MessageBackup message) {
        try {
            int port = Main.chordNode.getInfoNode().getPort();
            FileHandler.saveSerialize(port + "/backup/", "file.ser", message);
        }catch(Exception e) {
            Logger.ERR(this.getClass().getName(), "Not possible to save file " + filePath);
        }
    }

    public void sendToSuccessor() throws IOException {
        int desiredRepDeg = message.getDesiredRepDeg();
        byte[] bytesMessage = message.getBytes();
        String filePath = message.getFileName();
        int actualRepDeg = message.getActualRepDeg() + 1;

        InfoNode suc = Main.chordNode.getSuccessor();
        MessageBackup newMessage = new MessageBackup(message.getOriginNode(), filePath, bytesMessage, desiredRepDeg, actualRepDeg);
        Main.threadPool.submit(new SendMessage(suc.getIp(), suc.getPort(), newMessage));
    }

    public void storedMessageOrigin(String filePath) throws IOException {
        InfoNode currentNode = Main.chordNode.getInfoNode();
        MessageStored messageStored = new MessageStored(currentNode, MessageType.STORED, filePath);
        Main.threadPool.submit(new SendMessage(message.getIpOrigin(), message.getPortOrigin(), messageStored));
    }

    public void sendBackupDone(String filePath) throws IOException {
        int desiredRepDeg = message.getDesiredRepDeg();
        int actualRepDeg = message.getActualRepDeg() + 1;

        MessageDoneBackup messageDone = new MessageDoneBackup(message.getOriginNode(), MessageType.DONE_BACKUP, desiredRepDeg, actualRepDeg, filePath);
        Main.threadPool.submit(new SendMessage(message.getIpOrigin(), message.getPortOrigin(), messageDone));
    }
}
