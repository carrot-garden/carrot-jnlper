/*
 * @(#)FormArray.java	1.9 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.plugin.javascript.navig;

import netscape.javascript.JSObject;
import netscape.javascript.JSException;


/** 
 * <p> Emulate the form Array object in the JavaScript Document Object Model.
 * </p>
 */
class FormArray extends sun.plugin.javascript.navig.Array {

    /**
     * <p> Construct a new FormArray object. 
     * </p>
     * 
     * @param instance Native plugin instance.
     * @param context Evaluation context.
     * @param length Length of the array.
     */
    FormArray(int instance, String context, int length) {
	super(instance, context, length);
    }


    /** 
     * <p> Create  an array element.
     * </p>
     *
     * @param String Array element context.
     * @return New array element.
     */
    protected Object createObject(String context) throws JSException {
	return resolveObject(JSType.Form, context);
    }
}
