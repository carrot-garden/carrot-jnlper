/*
 * @(#)DOMImplementation.java	1.10 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.plugin.dom;

import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.w3c.dom.css.*;
import sun.plugin.dom.exception.*;


/**
 *  The <code>DOMImplementation</code> interface provides a number of methods 
 * for performing operations that are independent of any particular instance 
 * of the document object model.
 * <p>See also the <a href='http://www.w3.org/TR/2000/CR-DOM-Level-2-20000510'>Document Object Model (DOM) Level 2 Specification</a>.
 */
public class DOMImplementation implements org.w3c.dom.DOMImplementation, 
				          org.w3c.dom.html.HTMLDOMImplementation,
					  org.w3c.dom.css.DOMImplementationCSS
{
    private DOMObject obj;

    public DOMImplementation(DOMObject obj) {
        this.obj = obj;
    }

    /**
     *  Test if the DOM implementation implements a specific feature.
     * @param feature  The name of the feature to test (case-insensitive). The 
     *   values used by DOM features are defined throughout this 
     *   specification and listed in the  section. The name must be an  XML 
     *   name . To avoid possible conflicts, as a convention, names referring 
     *   to features defined outside the DOM specification should be made 
     *   unique by reversing the name of the Internet domain name of the 
     *   person (or the organization that the person belongs to) who defines 
     *   the feature, component by component, and using this as a prefix. For 
     *   instance, the W3C SYMM Working Group defines the feature 
     *   "org.w3c.dom.smil".
     * @param version  This is the version number of the feature to test. In 
     *   Level 2, this is the string "2.0". If the version is not specified, 
     *   supporting any version of the feature causes the method to return 
     *   <code>true</code> .
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
    public boolean hasFeature(String feature, 
                              String version) {
        try {
            return ((Boolean) obj.call("hasFeature", new Object[] { feature, version })).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *  Creates an empty <code>DocumentType</code> node. Entity declarations 
     * and notations are not made available. Entity reference expansions and 
     * default attribute additions do not occur. It is expected that a future 
     * version of the DOM will provide a way for populating a 
     * <code>DocumentType</code> .
     * <br> HTML-only DOM implementations do not need to implement this method.
     * @param qualifiedName  The  qualified name of the document type to be 
     *   created. 
     * @param publicId  The external subset public identifier.
     * @param systemId  The external subset system identifier.
     * @return  A new <code>DocumentType</code> node with 
     *   <code>Node.ownerDocument</code> set to <code>null</code> .
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed.
     * @since DOM Level 2
     */
    public DocumentType createDocumentType(String qualifiedName, 
                                           String publicId, 
                                           String systemId)
                                           throws DOMException
    {
        // It's unlikely that the browser, being HTML-only, will support this
	throw new PluginNotSupportedException("DOMImplementation.createDocumentType() is not supported");
    }

    /**
     *  Creates an XML <code>Document</code> object of the specified type with 
     * its document element. HTML-only DOM implementations do not need to 
     * implement this method.
     * @param namespaceURI  The  namespace URI of the document element to 
     *   create.
     * @param qualifiedName  The  qualified name of the document element to be 
     *   created.
     * @param doctype  The type of document to be created or <code>null</code> 
     *   . When <code>doctype</code> is not <code>null</code> , its 
     *   <code>Node.ownerDocument</code> attribute is set to the document 
     *   being created.
     * @return  A new <code>Document</code> object.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code> or an empty string, 
     *   or if the <code>qualifiedName</code> has a prefix that is "xml" and 
     *   the <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/XML/1998/namespace "  .
     *   <br> WRONG_DOCUMENT_ERR: Raised if <code>doctype</code> has already 
     *   been used with a different document or was created from a different 
     *   implementation.
     * @since DOM Level 2
     */
    public Document createDocument(String namespaceURI, 
                                   String qualifiedName, 
                                   DocumentType doctype)
                                   throws DOMException
    {
        // It's unlikely that the browser, being HTML-only, will support this
	throw new PluginNotSupportedException("DOMImplementation.createDocument() is not supported");
    }

    /**
     *  Creates an <code>HTMLDocument</code> object with the minimal tree made 
     * of the following elements: <code>HTML</code> , <code>HEAD</code> , 
     * <code>TITLE</code> , and <code>BODY</code> .
     * @param title  The title of the document to be set as the content of the 
     *   <code>TITLE</code> element, through a child <code>Text</code> node.
     * @return  A new <code>HTMLDocument</code> object.
     */
    public HTMLDocument createHTMLDocument(String title)
    {
        // It doesn't look like there is a JavaScript API for this
        // call -- it's also not documented in the current DOM Level 2
        // specs on w3c.org. We could consider implementing it
        // manually on top of createDocument(), above, at some point
        // if it turns out the browsers have support for that API
	throw new PluginNotSupportedException("HTMLDOMImplementation.createHTMLDocument() is not supported");
    }

    /**
     * Creates a new <code>CSSStyleSheet</code>.
     * @param title The advisory title. See also the  section. 
     * @param media The comma-separated list of media associated with the new 
     *   style sheet. See also the  section. 
     * @return A new CSS style sheet.
     * @exception DOMException
     *    SYNTAX_ERR: Raised if the specified media string value has a syntax 
     *   error and is unparsable. 
     */
    public CSSStyleSheet createCSSStyleSheet(String title, 
                                             String media)
                                             throws DOMException
    {
	throw new PluginNotSupportedException("DOMImplementationCSS.createCSSStyleSheet() is not supported");
    }

    // Start DOM Level 3 Stub methods
    /**
     *  This method returns a specialized object which implements the
     * specialized APIs of the specified feature and version, as specified
     * in . The specialized object may also be obtained by using
     * binding-specific casting methods but is not necessarily expected to,
     * as discussed in . This method also allow the implementation to
     * provide specialized objects which do not support the
     * <code>DOMImplementation</code> interface.
     * @param feature  The name of the feature requested. Note that any plus
     *   sign "+" prepended to the name of the feature will be ignored since
     *   it is not significant in the context of this method.
     * @param version  This is the version number of the feature to test.
     * @return  Returns an object which implements the specialized APIs of
     *   the specified feature and version, if any, or <code>null</code> if
     *   there is no object which implements interfaces associated with that
     *   feature. If the <code>DOMObject</code> returned by this method
     *   implements the <code>DOMImplementation</code> interface, it must
     *   delegate to the primary core <code>DOMImplementation</code> and not
     *   return results inconsistent with the primary core
     *   <code>DOMImplementation</code> such as <code>hasFeature</code>,
     *   <code>getFeature</code>, etc.
     * @since DOM Level 3
     */
    public Object getFeature(String feature, String version) {
        // FIXME
        throw new PluginNotSupportedException("DOMImplementation.getFeature() is not supported.");
    }
}
