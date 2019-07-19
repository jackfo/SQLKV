package com.cfs.sqlkv.common.sanity;

import java.util.Map;

public class ThreadDump {


    public static String getStackDumpString() {
        StringBuffer sb = new StringBuffer();
        Map<Thread, StackTraceElement[]> st = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> e : st.entrySet()) {
            StackTraceElement[] lines = e.getValue();
            Thread t = e.getKey();
            sb.append("Thread name=" + t.getName() + " id=" + t.getId()
                    + " priority=" + t.getPriority() + " state=" + t.getState()
                    + " isdaemon=" + t.isDaemon() + "\n");
            for (int i = 0; i < lines.length; i++) {
                sb.append("\t" + lines[i] + "\n");

            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
