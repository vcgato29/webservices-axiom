/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class LeafNode extends NodeImpl {
    private ParentNode ownerNode;
    
    private NodeImpl previousSibling;

    private NodeImpl nextSibling;

    public LeafNode(OMFactory factory) {
        super(factory);
    }

    final ParentNode internalGetOwnerNode() {
        return ownerNode;
    }

    final void internalSetOwnerNode(ParentNode ownerNode) {
        this.ownerNode = ownerNode;
    }

    final NodeImpl internalGetPreviousSibling() {
        return previousSibling;
    }
    
    final NodeImpl internalGetNextSibling() {
        return nextSibling;
    }
    
    final void internalSetPreviousSibling(NodeImpl previousSibling) {
        this.previousSibling = previousSibling;
    }
    
    final void internalSetNextSibling(NodeImpl nextSibling) {
        this.nextSibling = nextSibling;
    }
    
    public final NodeList getChildNodes() {
        return EmptyNodeList.INSTANCE;
    }

    public final Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        DOMException.HIERARCHY_REQUEST_ERR, null));
    }

    public final Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_FOUND_ERR, DOMMessageFormatter
                .formatMessage(DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR,
                               null));
    }

    public final Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                               DOMMessageFormatter.formatMessage(
                                       DOMMessageFormatter.DOM_DOMAIN,
                                       DOMException.HIERARCHY_REQUEST_ERR, null));
    }

    public final Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                               DOMMessageFormatter.formatMessage(
                                       DOMMessageFormatter.DOM_DOMAIN,
                                       DOMException.HIERARCHY_REQUEST_ERR, null));
    }

    public Node cloneNode(boolean deep) {
        LeafNode newnode = (LeafNode)super.cloneNode(deep);
        newnode.previousSibling = null;
        newnode.nextSibling = null;
        return newnode;
    }

    public final OMXMLParserWrapper getBuilder() {
        return null;
    }

    public final boolean isComplete() {
        return true;
    }

    public final void setComplete(boolean state) {
        if (state != true) {
            throw new IllegalStateException();
        }
    }

    public final void discard() throws OMException {
        detach();
    }
}
