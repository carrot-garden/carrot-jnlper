/*
 * @(#)JSException.java	1.10 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package netscape.javascript;


/** 
 * <p> Thrown when an exception is raised in the JavaScript engine.
 * </p>
 *
 * <p> Much of the functionality in this class is deprecated as it is
 * not portable between web browsers. The only functionality that
 * should be relied upon is the throwing of this exception and calls
 * to <CODE>printStackTrace()</CODE>. </p>
 */
public class JSException extends RuntimeException {

    // Exception type supported by JavaScript 1.4 in Navigator 5.0.
    //
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_EMPTY = -1;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_VOID = 0;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_OBJECT = 1;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_FUNCTION = 2;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_STRING = 3;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_NUMBER = 4;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_BOOLEAN = 5;
    /** @deprecated  Not portable between web browsers. */
    public static final int EXCEPTION_TYPE_ERROR = 6;

    /** 
     * <p> Constructs a JSException object. 
     * </p>
     */
    public JSException() {
	this(null);
    }

    /** 
     * <p> Construct a JSException object with a detail message. 
     * </p>
     *
     * @param s The detail message
     */
     public JSException(String s)  {
	this(s, null, -1, null, -1);
    }

    
    /** 
     * <p> Construct a JSException object. This constructor is
     * deprecated as it involves non-portable functionality. </p>
     *
     * @param s The detail message.
     * @param filename The URL of the file where the error occurred, if possible.
     * @param lineno The line number if the file, if possible.
     * @param source The string containing the JavaScript code being evaluated.
     * @param tokenIndex The index into the source string where the error occurred.
     * @deprecated  Not portable between web browsers.
     */
    public JSException(String s, String filename, int lineno, String source, 
		       int tokenIndex)  {
	super(s);
	this.message = s;
	this.filename = filename;
	this.lineno = lineno;
	this.source = source;
	this.tokenIndex = tokenIndex;	
	this.wrappedExceptionType = EXCEPTION_TYPE_EMPTY;
    }

    /**
     * <p> Construct a JSException object. This constructor is
     * deprecated as it involves non-portable functionality. </p>
     * 
     * @param wrappedExceptionType Type of the wrapped JavaScript exception.
     * @param wrappedException JavaScript exception wrapper.
     * @deprecated  Not portable between web browsers.
     */
    public JSException(int wrappedExceptionType, Object wrappedException) {
	this();
	this.wrappedExceptionType = wrappedExceptionType;
	this.wrappedException = wrappedException;
    }

    /** 
     * <p> The detail message. </p> 
     * @deprecated  Not portable between web browsers.
     */
    protected String message = null;

    /**
     * <p> The URL of the file where the error occurred, if possible. </p>
     * @deprecated  Not portable between web browsers.
     */
    protected String filename = null;

    /** 
     * <p> The line number if the file, if possible. </p>
     * @deprecated  Not portable between web browsers.
     */
    protected int lineno = -1;

    /** 
     * <p> The string containing the JavaScript code being evaluated. </p>
     * @deprecated  Not portable between web browsers.
     */
    protected String source = null;

    /** 
     * <p> The index into the source string where the error occurred. </p>
     * @deprecated  Not portable between web browsers.
     */
    protected int tokenIndex = -1;

    /**
     * <p> Type of the wrapped JavaScript exception. </p>
     * @deprecated  Not portable between web browsers.
     */
    private int wrappedExceptionType = -1;

    /**
     * <p> JavaScript exception wrapper. </p>
     * @deprecated  Not portable between web browsers.
     */
    private Object wrappedException = null;

    /**
     * <P> getWrappedExceptionType returns the int mapping of the type
     * of the wrappedException Object. This method is deprecated as it
     * involves non-portable functionality.  </P>
     * 
     * @return int JavaScript exception type.
     * @deprecated  Not portable between web browsers.
     */
    public int getWrappedExceptionType() {
	return wrappedExceptionType;
    }

    /**
     * <P> Returns the wrapped JavaScript exception. This method is
     * deprecated as it involves non-portable functionality.  </P>
     *
     * @return Object JavaScript exception wrapper.
     * @deprecated  Not portable between web browsers.
     */
    public Object getWrappedException() {
	return wrappedException;
    }
}
