package big.engine.util.timer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class AutoList<T>{
    public ConcurrentHashMap<T, Long> list;
    private int tries = 0;

    public AutoList() {
        list = new  ConcurrentHashMap<>();
    }

    public void add(T t) {
        list.put(t, System.currentTimeMillis());
    }
    public void add(T t,int c) {
        list.put(t,System.currentTimeMillis()-c);
    }
    public void remove(T t) {
        list.remove(t);
    }

    public boolean contains(T t) {
        return list.containsKey(t);
    }

    public ArrayList<T> getList() {
        ArrayList<T> list = new ArrayList<>();
        list.addAll(this.list.keySet());
        list.sort(Comparator.comparingLong(s->this.list.get(s)));
        return list;
    }

    public Set<T> getSet() {
        return list.keySet();
    }

    public void clear() {
        list.clear();
    }

    public AutoList<T> update(long ms) {
        try {
            List<T> toRemove = new ArrayList<>();
            for (T t : list.keySet()) {
                if (System.currentTimeMillis() - list.get(t) > ms) {
                    toRemove.add(t);
                }
            }
            for (T t : toRemove) {
                onRemove(t);
                list.remove(t);
            }
        } catch (Exception e) {
            tries++;
            if (tries > 5) {
                tries = 0;
                return this;
            }
            update(ms);
        }
        tries = 0;
        return this;
    }

    public AutoList<T> update(Predicate<T> predicate) {
        try {
            List<T> toRemove = new ArrayList<>();
            for (T t : list.keySet()) {
                if (predicate.test(t)) {
                    toRemove.add(t);
                }
            }
            for (T t : toRemove) {
                onRemove(t);
                list.remove(t);
            }
        } catch (Exception e) {
            tries++;
            if (tries > 5) {
                tries = 0;
                return this;
            }
            update(predicate);
        }
        tries = 0;
        return this;
    }

    public void onRemove(T t) {

    }
}
