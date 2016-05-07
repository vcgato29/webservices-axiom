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
package org.apache.axiom.om.impl.mixin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.axiom.core.Axis;
import org.apache.axiom.core.Builder;
import org.apache.axiom.core.CoreChildNode;
import org.apache.axiom.core.CoreElement;
import org.apache.axiom.core.CoreModelException;
import org.apache.axiom.core.ElementMatcher;
import org.apache.axiom.core.Mappers;
import org.apache.axiom.core.impl.builder.BuilderImpl;
import org.apache.axiom.core.stream.NamespaceRepairingFilterHandler;
import org.apache.axiom.core.stream.NamespaceURIInterningFilterHandler;
import org.apache.axiom.core.stream.StreamException;
import org.apache.axiom.core.stream.XmlHandler;
import org.apache.axiom.core.stream.sax.XmlHandlerContentHandler;
import org.apache.axiom.core.stream.serializer.SerializerXmlHandler;
import org.apache.axiom.core.stream.stax.StAXPivot;
import org.apache.axiom.core.stream.stax.XMLStreamWriterNamespaceContextProvider;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMSerializable;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.OMXMLStreamReaderConfiguration;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.common.AxiomExceptionTranslator;
import org.apache.axiom.om.impl.common.AxiomSemantics;
import org.apache.axiom.om.impl.common.SAXResultContentHandler;
import org.apache.axiom.om.impl.common.builder.OMXMLParserWrapperImpl;
import org.apache.axiom.om.impl.common.serializer.push.NamespaceContextPreservationFilterHandler;
import org.apache.axiom.om.impl.common.serializer.push.XmlDeclarationRewriterHandler;
import org.apache.axiom.om.impl.common.serializer.push.XsiTypeFilterHandler;
import org.apache.axiom.om.impl.common.serializer.push.sax.XMLReaderImpl;
import org.apache.axiom.om.impl.common.serializer.push.stax.StAXSerializer;
import org.apache.axiom.om.impl.intf.AxiomChildNode;
import org.apache.axiom.om.impl.intf.AxiomContainer;
import org.apache.axiom.om.impl.intf.AxiomElement;
import org.apache.axiom.om.impl.intf.AxiomSerializable;
import org.apache.axiom.om.impl.intf.OMFactoryEx;
import org.apache.axiom.om.impl.stream.stax.AxiomXMLStreamReaderExtensionFactory;
import org.apache.axiom.om.impl.stream.stax.MTOMXMLStreamWriterImpl;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

public aspect AxiomContainerSupport {
    private static final Log log = LogFactory.getLog(AxiomContainerSupport.class);
    
    private static final OMXMLStreamReaderConfiguration defaultReaderConfiguration = new OMXMLStreamReaderConfiguration();
    
    public final void AxiomContainer.discarded() {
        coreSetState(DISCARDED);
    }

    public final OMXMLParserWrapper AxiomContainer.getBuilder() {
        BuilderImpl builder = (BuilderImpl)coreGetBuilder();
        if (builder == null) {
            return null;
        } else {
            OMXMLParserWrapper facade = (OMXMLParserWrapper)builder.getFacade();
            if (facade == null) {
                facade = new OMXMLParserWrapperImpl(builder, null);
            }
            return facade;
        }
    }

    public final XMLStreamReader AxiomContainer.getXMLStreamReader() {
        return getXMLStreamReader(true);
    }
    
    public final XMLStreamReader AxiomContainer.getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    public final XMLStreamReader AxiomContainer.getXMLStreamReader(boolean cache) {
        return getXMLStreamReader(cache, defaultReaderConfiguration);
    }
    
    public XMLStreamReader AxiomContainer.getXMLStreamReader(boolean cache, OMXMLStreamReaderConfiguration configuration) {
        return defaultGetXMLStreamReader(cache, configuration);
    }
    
    public final XMLStreamReader AxiomContainer.defaultGetXMLStreamReader(boolean cache, OMXMLStreamReaderConfiguration configuration) {
        StAXPivot pivot = new StAXPivot(AxiomXMLStreamReaderExtensionFactory.INSTANCE);
        XmlHandler handler = pivot;
        if (configuration.isNamespaceURIInterning()) {
            handler = new NamespaceURIInterningFilterHandler(handler);
        }
        CoreElement contextElement = getContextElement();
        if (contextElement != null) {
            if (configuration.isPreserveNamespaceContext()) {
                handler = new NamespaceContextPreservationFilterHandler(handler, contextElement);
            } else {
                for (Iterator<OMNamespace> it = ((OMElement)contextElement).getNamespacesInScope(); it.hasNext(); ) {
                    OMNamespace ns = it.next();
                    pivot.setPrefix(ns.getPrefix(), ns.getNamespaceURI());
                }
                handler = new XsiTypeFilterHandler(handler, contextElement);
            }
        }
        try {
            pivot.setReader(coreGetReader(handler, cache, true));
        } catch (StreamException ex) {
            throw new OMException(ex);
        }
        return pivot;
    }
    
    public final AxiomChildNode AxiomContainer.prepareNewChild(OMNode omNode) {
        AxiomChildNode child;
        // Careful here: if the child was created by another Axiom implementation, it doesn't
        // necessarily implement AxiomChildNode
        if (omNode.getOMFactory().getMetaFactory().equals(getOMFactory().getMetaFactory())) {
            child = (AxiomChildNode)omNode;
        } else {
            child = (AxiomChildNode)((OMFactoryEx)getOMFactory()).importNode(omNode);
        }
        checkChild(omNode);
        return child;
    }

    public final void AxiomContainer.addChild(OMNode omNode) {
        try {
            coreAppendChild(prepareNewChild(omNode));
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }
    
    public OMNode AxiomContainer.getFirstOMChild() {
        try {
            return (OMNode)coreGetFirstChild();
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }
    
    public void AxiomContainer.removeChildren() {
        try {
            coreRemoveChildren(AxiomSemantics.INSTANCE);
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }
    
    public Iterator<OMNode> AxiomContainer.getChildren() {
        return coreGetNodes(Axis.CHILDREN, AxiomChildNode.class, Mappers.<OMNode>identity(), AxiomSemantics.INSTANCE);
    }

    public Iterator<OMElement> AxiomContainer.getChildrenWithLocalName(String localName) {
        return coreGetElements(Axis.CHILDREN, AxiomElement.class,
                ElementMatcher.BY_LOCAL_NAME, null, localName,
                Mappers.<OMElement>identity(), AxiomSemantics.INSTANCE);
    }

    public Iterator<OMElement> AxiomContainer.getChildrenWithNamespaceURI(String uri) {
        return coreGetElements(Axis.CHILDREN, AxiomElement.class,
                ElementMatcher.BY_NAMESPACE_URI, uri, null,
                Mappers.<OMElement>identity(), AxiomSemantics.INSTANCE);
    }

    public Iterator<OMElement> AxiomContainer.getChildrenWithName(QName name) {
        return coreGetElements(Axis.CHILDREN, AxiomElement.class,
                ElementMatcher.BY_QNAME, name.getNamespaceURI(), name.getLocalPart(),
                Mappers.<OMElement>identity(), AxiomSemantics.INSTANCE);
    }
    
    public final OMElement AxiomContainer.getFirstChildWithName(QName name) throws OMException {
        try {
            CoreChildNode child = coreGetFirstChild();
            while (child != null) {
                if (child instanceof AxiomElement) {
                    AxiomElement element = (AxiomElement)child;
                    if (name.getLocalPart().equals(element.coreGetLocalName()) && name.getNamespaceURI().equals(element.coreGetNamespaceURI())) {
                        return element;
                    }
                }
                child = child.coreGetNextSibling();
            }
            return null;
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }

    public final SAXSource AxiomContainer.getSAXSource(boolean cache) {
        return new SAXSource(new XMLReaderImpl(this, cache), new InputSource());
    }

    public final SAXResult AxiomContainer.getSAXResult() {
        XmlHandlerContentHandler handler = new XmlHandlerContentHandler(new SAXResultContentHandler(this), true);
        SAXResult result = new SAXResult();
        result.setHandler(handler);
        result.setLexicalHandler(handler);
        return result;
    }

    private XmlHandler AxiomContainer.createSerializer(MTOMXMLStreamWriter writer, boolean useExistingNamespaceContext) {
        StAXSerializer serializer = new StAXSerializer(writer);
        XmlHandler handler = new XmlDeclarationRewriterHandler(serializer, writer.getOutputFormat());
        CoreElement contextElement = getContextElement();
        if (contextElement != null) {
            handler = new XsiTypeFilterHandler(handler, contextElement);
        }
        return new NamespaceRepairingFilterHandler(handler,
                useExistingNamespaceContext ? new XMLStreamWriterNamespaceContextProvider(writer) : null,
                true);
    }
    
    public abstract CoreElement AxiomContainer.getContextElement();
    
    public final void AxiomContainer.serialize(XMLStreamWriter xmlWriter, boolean cache) throws XMLStreamException {
        // If the input xmlWriter is not an MTOMXMLStreamWriter, then wrapper it
        MTOMXMLStreamWriter writer = xmlWriter instanceof MTOMXMLStreamWriter ?
                (MTOMXMLStreamWriter) xmlWriter : 
                    new MTOMXMLStreamWriterImpl(xmlWriter);
        try {
            internalSerialize(createSerializer(writer, true), cache);
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        } catch (StreamException ex) {
            throw AxiomExceptionTranslator.toXMLStreamException(ex);
        }
        writer.flush();
    }

    private void AxiomContainer.serialize(MTOMXMLStreamWriter writer, boolean cache) throws XMLStreamException {
        try {
            try {
                internalSerialize(createSerializer(writer, false), cache);
            } catch (CoreModelException ex) {
                throw AxiomExceptionTranslator.translate(ex);
            } catch (StreamException ex) {
                throw AxiomExceptionTranslator.toXMLStreamException(ex);
            }
        } finally {
            writer.close();
        }
    }
    
    private void AxiomContainer.serialize(Writer writer, boolean cache) throws XMLStreamException {
        serialize(writer, new OMOutputFormat(), cache);
    }

    private void AxiomContainer.serialize(OutputStream output, OMOutputFormat format, boolean cache) throws XMLStreamException {
        serialize(new MTOMXMLStreamWriterImpl(output, format, cache), cache);
    }

    private void AxiomContainer.serialize(Writer writer, OMOutputFormat format, boolean cache) throws XMLStreamException {
        XmlHandler handler = new XmlDeclarationRewriterHandler(new SerializerXmlHandler(writer), format);
        CoreElement contextElement = getContextElement();
        if (contextElement != null) {
            handler = new XsiTypeFilterHandler(handler, contextElement);
        }
        try {
            internalSerialize(new NamespaceRepairingFilterHandler(handler, null, true), cache);
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        } catch (StreamException ex) {
            throw AxiomExceptionTranslator.toXMLStreamException(ex);
        }
        try {
            // TODO: the Abdera code depends on this
            writer.flush();
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

    public final void AxiomContainer.serialize(OutputStream output) throws XMLStreamException {
        serialize(output, new OMOutputFormat());
    }

    public final void AxiomContainer.serializeAndConsume(OutputStream output) throws XMLStreamException {
        serializeAndConsume(output, new OMOutputFormat());
    }

    public final void AxiomContainer.serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        serialize(output, format, true);
    }

    public final void AxiomContainer.serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        serialize(output, format, false);
    }

    public final void AxiomContainer.serialize(Writer writer) throws XMLStreamException {
        serialize(writer, true);
    }

    public final void AxiomContainer.serializeAndConsume(Writer writer) throws XMLStreamException {
        serialize(writer, false);
    }

    public final void AxiomContainer.serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        serialize(writer, format, true);
    }

    public final void AxiomContainer.serializeAndConsume(Writer writer, OMOutputFormat format) throws XMLStreamException {
        serialize(writer, format, false);
    }

    public final void AxiomContainer.close(boolean build) {
        Builder builder = coreGetBuilder();
        if (build) {
            this.build();
        }
        
        if (builder != null) {
            builder.close();
        }
    }
}
