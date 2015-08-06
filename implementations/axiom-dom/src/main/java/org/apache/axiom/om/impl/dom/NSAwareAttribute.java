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

import org.apache.axiom.dom.DOMNSAwareAttribute;
import org.apache.axiom.om.OMCloneOptions;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMAttributeEx;
import org.apache.axiom.om.impl.common.AxiomAttribute;
import org.apache.axiom.om.impl.common.AxiomText;

public final class NSAwareAttribute extends AttrImpl implements OMAttributeEx, AxiomAttribute, DOMNSAwareAttribute {
    // TODO: copy isId?
    NSAwareAttribute(String localName, OMNamespace namespace, String type, OMFactory factory) {
        super(null, factory);
        internalSetLocalName(localName);
        internalSetNamespace(namespace);
        coreSetType(type);
    }
    
    public NSAwareAttribute(DocumentImpl ownerDocument, String localName,
                    OMNamespace ns, String value, OMFactory factory) {
        super(ownerDocument, factory);
        internalSetLocalName(localName);
        if (value != null && value.length() != 0) {
            coreAppendChild((AxiomText)factory.createOMText(value), false);
        }
        coreSetType(OMConstants.XMLATTRTYPE_CDATA);
        internalSetNamespace(ns);
    }

    public String toString() {
        OMNamespace namespace = getNamespace();
        String localName = getLocalName();
        return (namespace == null) ? localName : namespace
                .getPrefix()
                + ":" + localName;
    }

    @Override
    final ParentNode shallowClone(OMCloneOptions options, ParentNode targetParent, boolean namespaceRepairing) {
        // Note: targetParent is always null here
        return new NSAwareAttribute(getLocalName(), getNamespace(), coreGetType(), getOMFactory());
    }
}
