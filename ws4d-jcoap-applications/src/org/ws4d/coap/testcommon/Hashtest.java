
package org.ws4d.coap.testcommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hashtest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Hashtest hashtest = new Hashtest();
        hashtest.runTest();
    }

    public class ReceivedCONMessage {
        public int msgID;
        public int sourceAddr;

        public ReceivedCONMessage(int msgID, int addr) {
            this.msgID = msgID;
            this.sourceAddr = addr;

        }
    }

    public class DublicateBuffer {
        private HashMap<Integer, List<ReceivedCONMessage>> table;

        public DublicateBuffer() {
            table = new HashMap<Integer, List<ReceivedCONMessage>>();
        }

        private void add(ReceivedCONMessage m) {
            if (!table.containsKey(m.msgID)) {
                table.put(m.msgID, new ArrayList<ReceivedCONMessage>());
            }
            List<ReceivedCONMessage> l = table.get(m.msgID);
            l.add(m);
        }

        private ReceivedCONMessage get(int msgID, int addr) {
            if (table.containsKey(msgID)) {
                List<ReceivedCONMessage> l = table.get(msgID);
                for (ReceivedCONMessage el : l) {
                    if (el.sourceAddr == addr) {
                        return el;
                    }
                }
            }
            return null;
        }

        private void remove(ReceivedCONMessage m) {
            if (table.containsKey(m.msgID)) {
                List<ReceivedCONMessage> l = table.get(m.msgID);
                l.remove(m);
                System.out.println("Removed " + m.sourceAddr);
                if (l.isEmpty()) {
                    table.remove(m.msgID);
                    System.out.println("Removed List");
                }
            }
        }

        private void printEntry(Integer i) {
            List<ReceivedCONMessage> l = table.get(i);
            if (l != null) {
                for (ReceivedCONMessage el : l) {
                    System.out.println(el.sourceAddr);
                }
            }
        }
    }

    private void runTest() {
        DublicateBuffer db = new DublicateBuffer();
        System.out.println("Start Test...");

        ReceivedCONMessage m1 = new ReceivedCONMessage(10, 100);
        ReceivedCONMessage m2 = new ReceivedCONMessage(10, 300);
        ReceivedCONMessage m3;
        db.add(m1);
        db.add(m2);
        m3 = db.get(10, 300);
        if (m3 == null) {
            System.out.println("NULL");
        } else {
            db.remove(m3);
        }
        db.printEntry(10);
    }

}
