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
package org.apache.axiom.om.impl.common.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.xml.stream.XMLStreamConstants;

import org.apache.axiom.core.Builder;
import org.apache.axiom.core.CoreNode;
import org.apache.axiom.core.CoreParentNode;
import org.apache.axiom.core.NodeFactory;
import org.apache.axiom.core.stream.XmlHandler;
import org.apache.axiom.om.impl.intf.AxiomDocument;
import org.apache.axiom.om.impl.intf.AxiomSourcedElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class BuilderHandler implements XmlHandler {
    private static final Log log = LogFactory.getLog(BuilderHandler.class);
    
    final NodeFactory nodeFactory;
    final Model model;
    final AxiomSourcedElement root;
    final Builder builder;
    final OMNamespaceCache nsCache = new OMNamespaceCache();
    public Context context;
    // returns the state of completion
    public boolean done;
    // keeps the state of the cache
    public boolean cache = true;
    public AxiomDocument document;
    
    /**
     * Tracks the depth of the node identified by {@link #target}. By definition, the document has
     * depth 0. Note that if caching is disabled, then this depth may be different from the actual
     * depth reached by the underlying parser.
     */
    public int depth;
    
    /**
     * Stores the stack trace of the code that caused a node to be discarded or consumed. This is
     * only used if debug logging was enabled when builder was created.
     */
    public Map<CoreParentNode,Throwable> discardTracker = log.isDebugEnabled() ? new LinkedHashMap<CoreParentNode,Throwable>() : null;
    
    private ArrayList<BuilderListener> listeners;
    private Queue<Runnable> deferredListenerActions;

    public BuilderHandler(NodeFactory nodeFactory, Model model, AxiomSourcedElement root, Builder builder) {
        this.nodeFactory = nodeFactory;
        this.model = model;
        this.root = root;
        this.builder = builder;
        context = new Context(this, null, 0);
    }

    public void addListener(BuilderListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<BuilderListener>();
        }
        listeners.add(listener);
    }
    
    void nodeAdded(CoreNode node) {
        if (listeners != null) {
            for (int i=0, size=listeners.size(); i<size; i++) {
                Runnable action = listeners.get(i).nodeAdded(node, depth);
                if (action != null) {
                    if (deferredListenerActions == null) {
                        deferredListenerActions = new LinkedList<Runnable>();
                    }
                    deferredListenerActions.add(action);
                }
            }
        }
    }

    void executeDeferredListenerActions() {
        if (deferredListenerActions != null) {
            Runnable action;
            while ((action = deferredListenerActions.poll()) != null) {
                action.run();
            }
        }
    }
    
    public boolean isCompleted() {
        return done;
    }
    
    public AxiomDocument getDocument() {
        if (root != null) {
            throw new UnsupportedOperationException("There is no document linked to this builder");
        } else {
            return document;
        }
    }
    
    public void startDocument(String inputEncoding, String xmlVersion, String xmlEncoding, boolean standalone) {
        if (root == null) {
            document = nodeFactory.createNode(model.getDocumentType());
            document.coreSetInputEncoding(inputEncoding);
            document.coreSetXmlVersion(xmlVersion);
            document.coreSetXmlEncoding(xmlEncoding);
            document.coreSetStandalone(standalone);
            document.coreSetInputContext(context);
            nodeAdded(document);
            context.target = document;
        }
    }
    
    public void processDocumentTypeDeclaration(String rootName, String publicId, String systemId,
            String internalSubset) {
        model.validateEventType(XMLStreamConstants.DTD);
        context.processDocumentTypeDeclaration(rootName, publicId, systemId, internalSubset);
    }
    
    public void startElement(String namespaceURI, String localName, String prefix) {
        depth++;
        context = context.startElement(namespaceURI, localName, prefix);
    }
    
    public void endElement() {
        context = context.endElement();
        depth--;
    }

    public void processAttribute(String namespaceURI, String localName, String prefix, String value, String type, boolean specified) {
        context.processAttribute(namespaceURI, localName, prefix, value, type, specified);
    }
    
    public void processNamespaceDeclaration(String prefix, String namespaceURI) {
        context.processNamespaceDeclaration(prefix, namespaceURI);
    }
    
    public void attributesCompleted() {
        context.attributesCompleted();
    }
    
    public void processCharacterData(Object data, boolean ignorable) {
        context.processCharacterData(data, ignorable);
    }
    
    public void processProcessingInstruction(String piTarget, String piData) {
        model.validateEventType(XMLStreamConstants.PROCESSING_INSTRUCTION);
        context.processProcessingInstruction(piTarget, piData);
    }

    public void processComment(String content) {
        model.validateEventType(XMLStreamConstants.COMMENT);
        context.processComment(content);
    }
    
    public void processCDATASection(String content) {
        model.validateEventType(XMLStreamConstants.CDATA);
        context.processCDATASection(content);
    }
    
    public void processEntityReference(String name, String replacementText) {
        model.validateEventType(XMLStreamConstants.ENTITY_REFERENCE);
        context.processEntityReference(name, replacementText);
    }
    
    public void endDocument() {
        if (depth != 0) {
            throw new IllegalStateException();
        }
        context.endDocument();
        context = null;
        done = true;
    }
}
