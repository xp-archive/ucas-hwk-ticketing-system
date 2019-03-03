package ticketingsystem.record;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Log {
    private volatile ConcurrentHashMap<Integer, LinkedList<Record>> records;

    public int routeNum, coachNum, seatNum, stationNum;

    public Log(int routeNum, int coachNum, int seatNum, int stationNum) {
        this.records = new ConcurrentHashMap<>();
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
    }

    public void record(Record record) {
        int threadID = ThreadID.get();
        LinkedList<Record> recordList = records.get(threadID);
        if (recordList == null) {
            recordList = new LinkedList<>();
            records.put(threadID, recordList);
        }
        int size = recordList.size();
        record.recordID = size + 1;
        recordList.addLast(record);
    }

    int[] getThreadIDs() {
        ArrayList<Integer> list = Collections.list(records.keys());
        int[] keys = new int[list.size()];
        for (int i = 0; i != list.size(); ++i) {
            keys[i] = list.get(i);
        }
        return keys;
    }

    Record[] getThreadRecords(int threadID) {
        LinkedList<Record> threadRecords = records.get(threadID);
        if (threadRecords != null) {
            return threadRecords.toArray(new Record[threadRecords.size()]);
        } else {
            return null;
        }
    }

    public HashMap<Integer, Record[]> getRecords() {
        HashMap<Integer, Record[]> ret = new HashMap<>();
        int[] keys = getThreadIDs();
        for (int key: keys) {
            ret.put(key, getThreadRecords(key));
        }
        return ret;
    }
}
