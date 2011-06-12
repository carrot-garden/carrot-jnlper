/*
 * @(#)CSSUnknownRule.java	1.7 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package sun.plugin.dom.css;

import org.w3c.dom.*;
import org.w3c.dom.css.*;
import sun.plugin.dom.*;

/**
 *  The <code>CSSUnknownRule</code> interface represents an at-rule not 
 * supported by this user agent. 
 * <p>See also the <a href='http://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113'>Document Object Model (DOM) Level 2 Style Specification</a>.
 * @since DOM Level 2
 */
public final class CSSUnknownRule extends sun.plugin.dom.css.CSSRule 
			    implements org.w3c.dom.css.CSSUnknownRule
{
    /**
     * Construct a CSSRuleList object.
     */
    public CSSUnknownRule(DOMObject obj,
			  org.w3c.dom.Document document){
	super(obj, document);
    }   
}
