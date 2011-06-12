/*
 * @(#)Element.java	1.12 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.plugin.dom.core;

import org.w3c.dom.*;
import org.w3c.dom.TypeInfo;
import sun.plugin.dom.*;
import sun.plugin.dom.exception.*;
import sun.plugin.dom.html.HTMLConstants;
import sun.plugin.dom.html.HTMLDocument;

/**
 *  The <code>Element</code> interface represents an element in an HTML or XML 
 * document. Elements may have attributes associated with them; since the 
 * <code>Element</code> interface inherits from <code>Node</code> , the 
 * generic <code>Node</code> interface attribute <code>attributes</code> may 
 * be used to retrieve the set of all attributes for an element. There are 
 * methods on the <code>Element</code> interface to retrieve either an 
 * <code>Attr</code> object by name or an attribute value by name. In XML, 
 * where an attribute value may contain entity references, an 
 * <code>Attr</code> object should be retrieved to examine the possibly 
 * fairly complex sub-tree representing the attribute value. On the other 
 * hand, in HTML, where all attributes have simple string values, methods to 
 * directly access an attribute value can safely be used as a convenience. In 
 * DOM Level 2, the method <code>normalize</code> is inherited from the 
 * <code>Node</code> interface where it was moved.
 * <p>See also the <a href='http://www.w3.org/TR/2000/CR-DOM-Level-2-20000510'>Document Object Model (DOM) Level 2 Specification</a>.
 */
public abstract class Element extends sun.plugin.dom.core.Node 
			      implements org.w3c.dom.Element
{
    private static final String ATTR_TAGNAME = "tagName";

    /**
     * Construct a new Element object.
     */
    protected Element(DOMObject obj, 
		      org.w3c.dom.Document doc) {
	super(obj, doc);
    }

    /**
     *  The name of the element. For example, in: 
     * <pre>
     * &lt;elementExample id="demo"&gt; 
     *         ... 
     * &lt;/elementExample&gt; ,</pre>
     *  <code>tagName</code> has 
     * the value <code>"elementExample"</code> . Note that this is 
     * case-preserving in XML, as are all of the operations of the DOM. The 
     * HTML DOM returns the <code>tagName</code> of an HTML element in the 
     * canonical uppercase form, regardless of the case in the  source HTML 
     * document. 
     */
    public String getTagName() {
	return getAttribute(ATTR_TAGNAME);
    }

    /**
     *  Retrieves an attribute value by name.
     * @param name  The name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     */
    public String getAttribute(String name) {
	return DOMObjectHelper.getStringMemberNoEx(obj, name);
    }

    /**
     *  Adds a new attribute. If an attribute with that name is already 
     * present in the element, its value is changed to be that of the value 
     * parameter. This value is a simple string; it is not parsed as it is 
     * being set. So any markup (such as syntax to be recognized as an entity 
     * reference) is treated as literal text, and needs to be appropriately 
     * escaped by the implementation when it is written out. In order to 
     * assign an attribute value that contains entity references, the user 
     * must create an <code>Attr</code> node plus any <code>Text</code> and 
     * <code>EntityReference</code> nodes, build the appropriate subtree, and 
     * use <code>setAttributeNode</code> to assign it as the value of an 
     * attribute.
     * <br> To set an attribute with a qualified name and namespace URI, use 
     * the <code>setAttributeNS</code> method.
     * @param name  The name of the attribute to create or alter.
     * @param value  Value to set in string form.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void setAttribute(String name, 
                             String value)
                             throws DOMException {
	DOMObjectHelper.setStringMember(obj, name, value);
    }

    /**
     *  Removes an attribute by name. If the removed attribute is known to 
     * have a default value, an attribute immediately appears containing the 
     * default value as well as the corresponding namespace URI, local name, 
     * and prefix when applicable.
     * <br> To remove an attribute by local name and namespace URI, use the 
     * <code>removeAttributeNS</code> method.
     * @param name  The name of the attribute to remove.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void removeAttribute(String name)
                                throws DOMException {
	// Not implemented
	throw new PluginNotSupportedException("Element.removeAttribute() is not supported");
    }


    /**
     *  Retrieves an attribute node by name.
     * <br> To retrieve an attribute node by qualified name and namespace URI, 
     * use the <code>getAttributeNodeNS</code> method.
     * @param name  The name (<code>nodeName</code> ) of the attribute to 
     *   retrieve.
     * @return  The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code> ) or <code>null</code> if there is no such 
     *   attribute.
     */
    public org.w3c.dom.Attr getAttributeNode(String name) {
        return DOMObjectFactory.createAttr(obj.call(HTMLConstants.FUNC_GET_ATTRIBUTE_NODE,
                                                    new Object[] { name }),
                                           getOwnerDocument());
    }


    /**
     *  Adds a new attribute node. If an attribute with that name (
     * <code>nodeName</code> ) is already present in the element, it is 
     * replaced by the new one.
     * <br> To add a new attribute node with a qualified name and namespace 
     * URI, use the <code>setAttributeNodeNS</code> method.
     * @param newAttr  The <code>Attr</code> node to add to the attribute list.
     * @return  If the <code>newAttr</code> attribute replaces an existing 
     *   attribute, the replaced <code>Attr</code> node is returned, 
     *   otherwise <code>null</code> is returned.
     * @exception DOMException
     *    WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from 
     *   a different document than the one that created the element.
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br> INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already 
     *   an attribute of another <code>Element</code> object. The DOM user 
     *   must explicitly clone <code>Attr</code> nodes to re-use them in 
     *   other elements.
     */
    public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr newAttr)
					     throws DOMException {
        return DOMObjectFactory.createAttr(obj.call(HTMLConstants.FUNC_SET_ATTRIBUTE_NODE,
                                                    new Object[] { ((sun.plugin.dom.core.Attr) newAttr).getDOMObject() }),
                                           getOwnerDocument());
    }


    /**
     *  Removes the specified attribute node. If the removed <code>Attr</code> 
     * has a default value it is immediately replaced. The replacing 
     * attribute has the same namespace URI and local name, as well as the 
     * original prefix, when applicable.
     * @param oldAttr  The <code>Attr</code> node to remove from the attribute 
     *   list.
     * @return  The <code>Attr</code> node that was removed.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br> NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an 
     *   attribute of the element.
     */
    public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr oldAttr)
                                    throws DOMException
    {
        return DOMObjectFactory.createAttr(obj.call(HTMLConstants.FUNC_REMOVE_ATTRIBUTE_NODE,
                                                    new Object[] { ((sun.plugin.dom.core.Attr) oldAttr).getDOMObject() }),
                                           getOwnerDocument());
    }

    /**
     *  Returns a <code>NodeList</code> of all descendant <code>Elements</code>
     *  with a given tag name, in the order in which they are encountered in 
     * a preorder traversal of this <code>Element</code> tree.
     * @param name  The name of the tag to match on. The special value "*" 
     *   matches all tags.
     * @return  A list of matching <code>Element</code> nodes.
     */
    public org.w3c.dom.NodeList getElementsByTagName(String name) {
        return DOMObjectFactory.createNodeList(
            obj.call(HTMLConstants.FUNC_GET_ELEMENTS_BY_TAGNAME, new Object[]{name}),
            (HTMLDocument) getOwnerDocument()
        );
    }

    /**
     *  Retrieves an attribute value by local name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     * @since DOM Level 2
     */
    public String getAttributeNS(String namespaceURI, 
                                 String localName) {
        try {
            return (String) obj.call(HTMLConstants.FUNC_GET_ATTRIBUTE_NS,
                                     new Object[] { namespaceURI, localName });
        } catch (DOMException e) {
        }
        return null;
    }

    /**
     *  Adds a new attribute. If an attribute with the same local name and 
     * namespace URI is already present on the element, its prefix is changed 
     * to be the prefix part of the <code>qualifiedName</code> , and its 
     * value is changed to be the <code>value</code> parameter. This value is 
     * a simple string; it is not parsed as it is being set. So any markup 
     * (such as syntax to be recognized as an entity reference) is treated as 
     * literal text, and needs to be appropriately escaped by the 
     * implementation when it is written out. In order to assign an attribute 
     * value that contains entity references, the user must create an 
     * <code>Attr</code> node plus any <code>Text</code> and 
     * <code>EntityReference</code> nodes, build the appropriate subtree, and 
     * use <code>setAttributeNodeNS</code> or <code>setAttributeNode</code> to
     *  assign it as the value of an attribute.
     * <br> HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to create or 
     *   alter.
     * @param qualifiedName  The  qualified name of the attribute to create or 
     *   alter.
     * @param value  The value to set in string form.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code> or an empty string, 
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and the 
     *   <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/XML/1998/namespace ", or if the 
     *   <code>qualifiedName</code> is "xmlns" and the 
     *   <code>namespaceURI</code> is different from " 
     *   http://www.w3.org/2000/xmlns/ ".
     * @since DOM Level 2
     */
    public void setAttributeNS(String namespaceURI, 
                               String qualifiedName, 
                               String value)
                               throws DOMException {
        obj.call(HTMLConstants.FUNC_SET_ATTRIBUTE_NS,
                 new Object[] { namespaceURI, qualifiedName, value });
    }

    /**
     *  Removes an attribute by local name and namespace URI. If the removed 
     * attribute has a default value it is immediately replaced. The 
     * replacing attribute has the same namespace URI and local name, as well 
     * as the original prefix.
     * <br> HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to remove.
     * @param localName  The  local name of the attribute to remove.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     */
    public void removeAttributeNS(String namespaceURI, 
                                  String localName)
                                  throws DOMException {
        obj.call(HTMLConstants.FUNC_REMOVE_ATTRIBUTE_NS,
                 new Object[] { namespaceURI, localName });
    }

    /**
     *  Retrieves an <code>Attr</code> node by local name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> node with the specified attribute local 
     *   name and namespace URI or <code>null</code> if there is no such 
     *   attribute.
     * @since DOM Level 2
     */
    public org.w3c.dom.Attr getAttributeNodeNS(String namespaceURI, 
					       String localName) {
        return DOMObjectFactory.createAttr(obj.call(HTMLConstants.FUNC_GET_ATTRIBUTE_NODE_NS,
                                                    new Object[] { namespaceURI, localName }),
                                           getOwnerDocument());
    }

    /**
     *  Adds a new attribute. If an attribute with that local name and that 
     * namespace URI is already present in the element, it is replaced by the 
     * new one.
     * <br> HTML-only DOM implementations do not need to implement this method.
     * @param newAttr  The <code>Attr</code> node to add to the attribute list.
     * @return  If the <code>newAttr</code> attribute replaces an existing 
     *   attribute with the same  local name and  namespace URI , the 
     *   replaced <code>Attr</code> node is returned, otherwise 
     *   <code>null</code> is returned.
     * @exception DOMException
     *    WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from 
     *   a different document than the one that created the element.
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br> INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already 
     *   an attribute of another <code>Element</code> object. The DOM user 
     *   must explicitly clone <code>Attr</code> nodes to re-use them in 
     *   other elements.
     * @since DOM Level 2
     */
    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr)
                                   throws DOMException {
        return DOMObjectFactory.createAttr(obj.call(HTMLConstants.FUNC_SET_ATTRIBUTE_NODE_NS,
                                                    new Object[] { ((sun.plugin.dom.core.Attr) newAttr).getDOMObject() }),
                                           getOwnerDocument());
    }


    /**
     *  Returns a <code>NodeList</code> of all the descendant 
     * <code>Elements</code> with a given local name and namespace URI in the 
     * order in which they are encountered in a preorder traversal of this 
     * <code>Element</code> tree.
     * <br> HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the elements to match on. 
     *   The special value "*" matches all namespaces.
     * @param localName  The  local name of the elements to match on. The 
     *   special value "*" matches all local names.
     * @return  A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code> .
     * @since DOM Level 2
     */
    public org.w3c.dom.NodeList getElementsByTagNameNS(String namespaceURI, 
				                       String localName) {
        

        return DOMObjectFactory.createNodeList(
            obj.call(HTMLConstants.FUNC_GET_ELEMENTS_BY_TAGNAME_NS,
                     new Object[] { namespaceURI, localName }),
            (HTMLDocument) getOwnerDocument()
        );
    }


    /**
     *  Returns <code>true</code> when an attribute with a given name is 
     * specified on this element or has a default value, <code>false</code> 
     * otherwise.
     * @param name  The name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given name is 
     *   specified on this element or has a default value, <code>false</code> 
     *   otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttribute(String name) {
	return (getAttribute(name) != null);
    }

    /**
     *  Returns <code>true</code> when an attribute with a given local name 
     * and namespace URI is specified on this element or has a default value, 
     * <code>false</code> otherwise. HTML-only DOM implementations do not 
     * need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to look for.
     * @param localName  The  local name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given local name and 
     *   namespace URI is specified or has a default value on this element, 
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributeNS(String namespaceURI, 
                                  String localName) {
        try {
            return ((Boolean) obj.call(HTMLConstants.FUNC_HAS_ATTRIBUTE_NS,
                                       new Object[] { namespaceURI, localName })).booleanValue();
        } catch (Exception e) {
        }
        return false;
    }

    /**
     *  The value of this node, depending on its type; see the table above. 
     * When it is defined to be <code>null</code> , setting it has no effect.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *    DOMSTRING_SIZE_ERR: Raised when it would return more characters 
     *   than fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     */
    public String getNodeValue() throws DOMException {
	throw new PluginNotSupportedException("Element.getNodeValue() is not supported");
    }

    public void setNodeValue(String nodeValue)
        throws DOMException {
	// no-op
	throw new PluginNotSupportedException("Element.setNodeValue() is not supported");
    }

    // Start of dummy methods for DOM L3.
    public void setIdAttribute(String name,
                               boolean isId)
                               throws DOMException {
        throw new PluginNotSupportedException("Element.setIdAttribute() is not supported");
    }

    public void setIdAttributeNS(String namespaceURI,
                                 String localName,
                                 boolean isId)
                                 throws DOMException {
         throw new PluginNotSupportedException("Element.setIdAttributeNS() is not supported");
    }

    public void setIdAttributeNode(org.w3c.dom.Attr idAttr,
                                   boolean isId)
				   throws DOMException {
        throw new PluginNotSupportedException("Element.setIdAttributeNode() is not supported");

    }                   
    public TypeInfo getSchemaTypeInfo() {
        throw new PluginNotSupportedException("Element.getSchemaTypeInfo is not supported");
    }
    //End of dummy methods for DOM L3.

}

