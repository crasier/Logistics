package com.sdeport.logistics.driver.tools;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.LinkedHashMap;
import java.util.LinkedList;


public class CD {

    private static final int TICK = 1;

    private LinkedHashMap<String, Action> actions;

    private Handler mHandler;

    private Thread timerThread;

    public static class LazyHolder {
        public static CD INSTANCE = new CD();
    }

    public static CD getInstance() {
        return LazyHolder.INSTANCE;
    }

    private CD() {

        actions = new LinkedHashMap<>();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TICK:
                        LinkedList<String> removeable = new LinkedList<>();
                        for (Action ac : actions.values()) {
                            ac.duration -= 1000;
                            if (!ac.usable) {
                                continue;
                            }
                            if (ac.duration <= 0) {
                                ac.onStop();
                                removeable.add(ac.tag);
                            }else {
                                ac.onStep(ac.duration);
                            }
                        }
                        for (String tag : removeable) {
                            actions.remove(tag);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(TICK);
                    }
                    SystemClock.sleep(1000);
                }
            }
        });

        timerThread.start();
    }

    public void stop() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }

    public abstract static class Action {
        public long duration;
        public long step = 1000;
        public String tag;
        public boolean usable = true;

        public Action() {

        }

        public Action(String tag, long duration) {
            this.duration = duration;
            this.tag = tag;
        }

        public void dispose() {
            this.usable = false;
        }

        public abstract void onStep(long step);
        public abstract void onStop();
    }

    public void addAction(Action action) {
        if (actions.containsKey(action.tag)) {
            action.duration = actions.get(action.tag).duration;
            actions.put(action.tag, action);
        }else {
            actions.put(action.tag, action);
        }

        action.onStep(action.duration);
    }
}
