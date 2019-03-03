package ticketingsystem.test.utils;

import java.util.HashMap;

public class ValidationStatus {
    private HashMap<Integer, Integer> status;

    public ValidationStatus(HashMap<Integer, Integer> curPos) {
        status = new HashMap<>();
        for (int key: curPos.keySet()) {
            status.put(key, curPos.get(key));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationStatus that = (ValidationStatus) o;
        return status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }
}
