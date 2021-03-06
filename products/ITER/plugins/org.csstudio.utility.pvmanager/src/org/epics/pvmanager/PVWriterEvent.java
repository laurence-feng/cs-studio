/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

/**
 * An event for the writer.
 * <p>
 * An event can be trigger by:
 * <ul>
 * <li>a connection change; the connection either connected or disconnected</li>
 * <li>an exception; an error either at the datasource or while preparing the write values</li>
 * <li>a write has concluded</li>
 * </ul>
 * One event can have multiple triggers. You can use the mask or the methods to check what condition applies.
 *
 * @param <T> the type of the writer
 * @author carcassi
 */
public class PVWriterEvent<T> {
    
    /**
     * Mask for connection event.
     */
    public static int CONNECTION_MASK      = Integer.parseInt("000001", 2);
    /**
     * Mask for error event.
     */
    public static int EXCEPTION_MASK       = Integer.parseInt("000010", 2);
    /**
     * Mask for a successful write result.
     */
    public static int WRITE_SUCCEEDED_MASK = Integer.parseInt("000100", 2);
    /**
     * Mask for a failed write result.
     */
    public static int WRITE_FAILED_MASK    = Integer.parseInt("001000", 2);
    
    private final int notificationMask;
    private final PVWriter<T> pvWriter;

    PVWriterEvent(int notificationMask, PVWriter<T> pvWriter) {
        this.notificationMask = notificationMask;
        this.pvWriter = pvWriter;
    }

    /**
     * The writer that generated the event.
     *
     * @return the pv writer
     */
    public PVWriter<T> getPvWriter() {
        return pvWriter;
    }

    /**
     * The mask for the event.
     *
     * @return event mask
     */
    public int getNotificationMask() {
        return notificationMask;
    }
    
    /**
     * Whether this event was generated by a connection change.
     *
     * @return true if the connection status changed from the last notification
     */
    public boolean isConnectionChanged() {
        return (notificationMask & CONNECTION_MASK) != 0;
    }
    
    /**
     * Whether this event was generated in response to a successful write operation.
     *
     * @return true if the write operation was concluded successfully
     */
    public boolean isWriteSucceeded() {
        return (notificationMask & WRITE_SUCCEEDED_MASK) != 0;
    }
    
    /**
     * Whether this event waas generated in response to a failed write operation.
     *
     * @return true if the write operation was concluded unsuccessfully
     */
    public boolean isWriteFailed() {
        return (notificationMask & WRITE_FAILED_MASK) != 0;
    }
    
    /**
     * Whether this event was generated in response to an error.
     *
     * @return true if a new exception is available
     */
    public boolean isExceptionChangesd() {
        return (notificationMask & EXCEPTION_MASK) != 0;
    }
    
    
}
