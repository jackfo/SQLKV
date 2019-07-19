package com.cfs.sqlkv.common.sanity;


import java.util.Hashtable;

public class SanityManager {

    public static final boolean ASSERT = SanityState.ASSERT; // code should use DEBUG
    public static final boolean DEBUG = SanityState.DEBUG;

    public static final String DEBUGDEBUG = "DumpSanityDebug";


    static private java.io.PrintWriter debugStream = new java.io.PrintWriter(System.err);

    static private Hashtable<String, Boolean> DebugFlags = new Hashtable<String, Boolean>();

    static private boolean AllDebugOn = false;
    static private boolean AllDebugOff = false;

    public static final void ASSERT(boolean mustBeTrue) {
        if (DEBUG)
            if (!mustBeTrue) {
                if (DEBUG) {
                    AssertFailure af = new AssertFailure("ASSERT FAILED");
                    if (DEBUG_ON("AssertFailureTrace")) {
                        showTrace(af);
                    }
                    throw af;
                } else
                    throw new AssertFailure("ASSERT FAILED");
            }
    }

    public static final void ASSERT(boolean mustBeTrue, String msgIfFail) {
        if (DEBUG)
            if (!mustBeTrue) {
                if (DEBUG) {
                    AssertFailure af = new AssertFailure("ASSERT FAILED " + msgIfFail);
                    if (DEBUG_ON("AssertFailureTrace")) {
                        showTrace(af);
                    }
                    throw af;
                } else
                    throw new AssertFailure("ASSERT FAILED " + msgIfFail);
            }
    }

    public static final void THROWASSERT(String msgIfFail) {
        // XXX (nat) Hmm, should we check ASSERT here?  The caller is
        // not expecting this function to return, whether assertions
        // are compiled in or not.
        THROWASSERT(msgIfFail, null);
    }

    public static final void THROWASSERT(String msg, Throwable t) {
        AssertFailure af = new AssertFailure("ASSERT FAILED " + msg, t);
        if (DEBUG) {
            if (DEBUG_ON("AssertFailureTrace")) {
                showTrace(af);
            }
        }
        if (t != null) {
            showTrace(t);
        }
        throw af;
    }


    public static final void THROWASSERT(Throwable t) {
        THROWASSERT(t.toString(), t);
    }


    public static final void DEBUG(String flag, String message) {
        if (DEBUG) {
            if (DEBUG_ON(flag)) {
                DEBUG_PRINT(flag, message);
            }
        }
    }

    public static final boolean DEBUG_ON(String flag) {
        if (DEBUG) {
            if (AllDebugOn) return true;
            else if (AllDebugOff) return false;
            else {
                Boolean flagValue = DebugFlags.get(flag);
                if (!DEBUGDEBUG.equals(flag)) {
                    if (DEBUG_ON(DEBUGDEBUG)) {
                        DEBUG_PRINT(DEBUGDEBUG, "DEBUG_ON: Debug flag " + flag + " = " + flagValue);
                    }
                }
                if (flagValue == null) return false;
                else return flagValue.booleanValue();
            }
        } else return false;
    }

    public static final void DEBUG_SET(String flag) {
        if (DEBUG) {
            if (!DEBUGDEBUG.equals(flag)) {
                if (DEBUG_ON(DEBUGDEBUG))
                    DEBUG_PRINT(DEBUGDEBUG, "DEBUG_SET: Debug flag " + flag);
            }

            DebugFlags.put(flag, Boolean.TRUE);
        }
    }

    public static final void DEBUG_CLEAR(String flag) {
        if (DEBUG) {
            if (!DEBUGDEBUG.equals(flag)) {
                if (DEBUG_ON(DEBUGDEBUG))
                    DEBUG_PRINT(DEBUGDEBUG, "DEBUG_CLEAR: Debug flag " + flag);
            }

            DebugFlags.put(flag, Boolean.FALSE);
        }
    }

    public static final void DEBUG_ALL_ON() {
        if (DEBUG) {
            AllDebugOn = true;
            AllDebugOff = false;
        }
    }


    public static final void DEBUG_ALL_OFF() {
        if (DEBUG) {
            AllDebugOff = true;
            AllDebugOn = false;
        }
    }


    static public void SET_DEBUG_STREAM(java.io.PrintWriter pw) {
        debugStream = pw;
    }

    static public java.io.PrintWriter GET_DEBUG_STREAM() {
        return debugStream;
    }

    static private void showTrace(AssertFailure af) {
        af.printStackTrace();
        java.io.PrintWriter assertStream = GET_DEBUG_STREAM();

        assertStream.println("Assertion trace:");
        af.printStackTrace(assertStream);
        assertStream.flush();
    }

    static public void showTrace(Throwable t) {
        java.io.PrintWriter assertStream = GET_DEBUG_STREAM();
        assertStream.println("Exception trace: ");
        t.printStackTrace(assertStream);
    }

    static public void DEBUG_PRINT(String flag, String message) {
        java.io.PrintWriter debugStream = GET_DEBUG_STREAM();
        debugStream.println("DEBUG " + flag + " OUTPUT: " + message);
        debugStream.flush();
    }

    public static void NOTREACHED() {
        THROWASSERT("code should not be reached");
    }
}

