package ticketingsystem.test;

import java.util.*;

import ticketingsystem.Console;
import ticketingsystem.WrappedTDS;
import ticketingsystem.record.Log;
import ticketingsystem.record.Operation;
import ticketingsystem.record.Record;
import ticketingsystem.record.SystemStatus;
import ticketingsystem.test.utils.Execution;
import ticketingsystem.test.utils.OperationGenerator;
import ticketingsystem.test.utils.SoldTickets;
import ticketingsystem.test.utils.ValidationStatus;

public class CorrectnessTest {
    public static final boolean singleThreadEnable = Config.Correctness.SingleThread.enable;
    public static final boolean multiThreadEnable = Config.Correctness.MultiThread.enable;

    public static boolean singleThread() {
        final int routeNum = Config.Correctness.SingleThread.routeNum;
        final int coachNum = Config.Correctness.SingleThread.coachNum;
        final int seatNum = Config.Correctness.SingleThread.seatNum;
        final int stationNum = Config.Correctness.SingleThread.stationNum;
        Console.log("Single Thread Correctness Validation");
        final WrappedTDS tds = new WrappedTDS(routeNum, coachNum, seatNum, stationNum, 1);
        boolean result = tdsSingleThread(tds);
        Console.log((result ? "    - passed." : "    - failed."));
        return result;
    }

    public static boolean multiThread() throws InterruptedException {
        final int routeNum = Config.Correctness.MultiThread.routeNum;
        final int coachNum = Config.Correctness.MultiThread.coachNum;
        final int seatNum = Config.Correctness.MultiThread.seatNum;
        final int stationNum = Config.Correctness.MultiThread.stationNum;
        final int threadNum = Config.Correctness.MultiThread.threadNum;
        Console.log("Multi-Thread Correctness Validation");
        final WrappedTDS tds = new WrappedTDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        boolean result = tdsMultiThread(tds);
        Console.log((result ? "    - passed." : "    - failed."));
        return result;
    }

    public static boolean tdsSingleThread(WrappedTDS tds) {
        int routeNum = tds.getRouteNum();
        int coachNum = tds.getCoachNum();
        int seatNum = tds.getSeatNum();
        int stationNum = tds.getStationNum();

        final int operationNum = Config.Correctness.SingleThread.operationNum;
        final OperationGenerator operationGenerator = new OperationGenerator(
                Config.Correctness.SingleThread.operationWeight.buy,
                Config.Correctness.SingleThread.operationWeight.inquiry,
                Config.Correctness.SingleThread.operationWeight.refund
        );

        SystemStatus status = new SystemStatus(routeNum, coachNum, seatNum, stationNum);
        SoldTickets soldTickets = new SoldTickets();
        Log log = new Log(routeNum, coachNum, seatNum, stationNum);
        Record.resetRecordID();

        boolean ok = true;
        for (int i = 0; ok && i != operationNum; ++i) {
            SystemStatus before = tds.getStatus();
            Record record = Execution.oneStepRecorded(tds, operationGenerator, soldTickets);
            SystemStatus after = tds.getStatus();

            log.record(record);

            if (!status.equals(before) || !status.transition(record)) {
                ok = false;
            }
            if (!status.equals(after)) {
                ok = false;
            }
        }
        return ok;
    }

    public static boolean tdsMultiThread(final WrappedTDS tds) throws InterruptedException {
        int routeNum = tds.getRouteNum();
        int coachNum = tds.getCoachNum();
        int seatNum = tds.getSeatNum();
        int stationNum = tds.getStationNum();
        int threadNum = tds.getThreadNum();
        final int operationNum = Config.Correctness.MultiThread.operationNum;
        final OperationGenerator operationGenerator = new OperationGenerator(
                Config.Correctness.MultiThread.operationWeight.buy,
                Config.Correctness.MultiThread.operationWeight.inquiry,
                Config.Correctness.MultiThread.operationWeight.refund
        );

        final Log log = new Log(routeNum, coachNum, seatNum, stationNum);

        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i != threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                SoldTickets soldTickets = new SoldTickets();

                @Override
                public void run() {
                    for (int t = 0; t != operationNum; ++t) {
                        Record record = Execution.oneStepRecorded(tds, operationGenerator, soldTickets);
                        log.record(record);
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i != threadNum; ++i) {
            threads[i].join();
        }

        SystemStatus initialStatus = new SystemStatus(routeNum, coachNum, seatNum, stationNum);
        HashMap<Integer, Integer> initialPos = new HashMap<>();
        HashMap<Integer, Record[]> records = log.getRecords();

        for (int threadID: records.keySet()) {
            initialPos.put(threadID, 0);
        }

        return recordIsValid(initialStatus, initialPos, records);
    }

    private static boolean recordIsValid(SystemStatus curStatus,
                                         HashMap<Integer, Integer> curPos,
                                         HashMap<Integer, Record[]> records) {
        HashSet<ValidationStatus> mark = new HashSet<>();
        return recordIsValid(curStatus, curPos, records, 1, mark);
    }

    private static boolean recordIsValid(SystemStatus curStatus,
                                         HashMap<Integer, Integer> curPos,
                                         HashMap<Integer, Record[]> records,
                                         int depth,
                                         HashSet<ValidationStatus> mark) {

        ValidationStatus check = new ValidationStatus(curPos);
        mark.add(check);
//        if (mark.size() % 1000 == 0) {
//            System.out.println(mark.size());
//        }


        // 清除所有的查询操作
        for (int threadID: curPos.keySet()) {
            Record[] threadRecords = records.get(threadID);
            int threadPos = curPos.get(threadID);
            while (threadPos < threadRecords.length && threadRecords[threadPos].operation == Operation.INQUIRY) {
                ++threadPos;
            }
            curPos.put(threadID, threadPos);
        }

        // 当所有线程的记录均为空时，表示已经找到了一个可行的线性化序列
        boolean isEmpty = true;
        for (int threadID: curPos.keySet()) {
            Record[] threadRecords = records.get(threadID);
            int threadPos = curPos.get(threadID);
            if (threadPos < threadRecords.length) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            return true;
        }

        // 通过回溯尝试找到一个可行的线性化序列
        for (int threadID: curPos.keySet()) {
            Record[] threadRecords = records.get(threadID);
            int threadPos = curPos.get(threadID);

            // 尝试当前线程当前的记录是否能够使状态合法地转移
            if (threadPos < threadRecords.length) {
                Record curRecord = threadRecords[threadPos];
                int pos = curPos.get(threadID);
                curPos.put(threadID, pos+1);
                if (!mark.contains(new ValidationStatus(curPos))) {
                    if (curStatus.transition(curRecord)) {
                        // 如果可行，进入到下一个记录
//                        System.out.println("("+depth+")for: " + curPos + " - " + threadID);
//                        System.out.println("              - target: " + curPos);
                        if (recordIsValid(curStatus, curPos, records, depth + 1, mark)) {
                            return true;
                        }
                        curStatus.recoverFrom(curRecord);
                    }
                }
                curPos.put(threadID, pos);
            }
        }

//        System.out.println("("+depth+")failed: " + curPos);

        return false;
    }

    public static void main(String[] args) throws Exception {
        singleThread();
        multiThread();
    }
}
