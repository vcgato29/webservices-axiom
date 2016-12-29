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

package org.apache.axiom.util.stax.xop;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerWriter;
import org.apache.axiom.util.stax.XMLStreamWriterUtils;

/**
 * {@link XMLStreamWriter} wrapper that encodes XOP. It implements the extension
 * defined by {@link DataHandlerWriter}. The {@link DataHandler}
 * objects for the parts referenced by <tt>xop:Include</tt> element information items produced by
 * an instance of this class can be retrieved using the {@link #getDataHandler(String)} method.
 */
public class XOPEncodingStreamWriter extends XOPEncodingStreamWrapper
                                     implements XMLStreamWriter, DataHandlerWriter {
    private final XMLStreamWriter parent;
    
    /**
     * Constructor.
     * 
     * @param parent
     *            the XML stream to write the encoded infoset to
     * @param contentIDGenerator
     *            used to generate content IDs for the binary content encoded as
     *            <tt>xop:Include</tt> element information items
     * @param optimizationPolicy
     *            the policy to apply to decide which binary content to optimize
     */
    public XOPEncodingStreamWriter(XMLStreamWriter parent, ContentIDGenerator contentIDGenerator,
            OptimizationPolicy optimizationPolicy) {
        super(contentIDGenerator, optimizationPolicy);
        this.parent = parent;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (DataHandlerWriter.PROPERTY.equals(name)) {
            return this;
        } else {
            return parent.getProperty(name);
        }
    }

    private void writeXOPInclude(String contentID) throws XMLStreamException {
        String writerPrefix = parent.getPrefix(XOPConstants.NAMESPACE_URI);
        if (writerPrefix != null) {
            parent.writeStartElement(XOPConstants.NAMESPACE_URI, "Include");
        } else {
            parent.writeStartElement(XOPConstants.DEFAULT_PREFIX, XOPConstants.INCLUDE,
                    XOPConstants.NAMESPACE_URI);
            parent.setPrefix(XOPConstants.DEFAULT_PREFIX, XOPConstants.NAMESPACE_URI);
            parent.writeNamespace(XOPConstants.DEFAULT_PREFIX, XOPConstants.NAMESPACE_URI);
        }
        parent.writeAttribute(XOPConstants.HREF, XOPUtils.getURLForContentID(contentID));
        parent.writeEndElement();
    }
    
    @Override
    public void writeDataHandler(DataHandler dataHandler, String contentID, boolean optimize)
            throws IOException, XMLStreamException {
        contentID = processDataHandler(dataHandler, contentID, optimize);
        if (contentID != null) {
            writeXOPInclude(contentID);
        } else {
            XMLStreamWriterUtils.writeBase64(parent, dataHandler);
        }
    }

    @Override
    public void writeDataHandler(DataHandlerProvider dataHandlerProvider, String contentID,
            boolean optimize) throws IOException, XMLStreamException {
        contentID = processDataHandler(dataHandlerProvider, contentID, optimize);
        if (contentID != null) {
            writeXOPInclude(contentID);
        } else {
            XMLStreamWriterUtils.writeBase64(parent, dataHandlerProvider.getDataHandler());
        }
    }

    @Override
    public void close() throws XMLStreamException {
        parent.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        parent.flush();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return parent.getNamespaceContext();
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return parent.getPrefix(uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        parent.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        parent.setNamespaceContext(context);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        parent.setPrefix(prefix, uri);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        parent.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value)
            throws XMLStreamException {
        parent.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        parent.writeAttribute(localName, value);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        parent.writeCData(data);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        parent.writeCharacters(text, start, len);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        parent.writeCharacters(text);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        parent.writeComment(data);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        parent.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        parent.writeDTD(dtd);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        parent.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        parent.writeEmptyElement(localName);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        parent.writeEndDocument();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        parent.writeEndElement();
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        parent.writeEntityRef(name);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        parent.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        parent.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        parent.writeProcessingInstruction(target);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        parent.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        parent.writeStartDocument(encoding, version);
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        parent.writeStartDocument(version);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        parent.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        parent.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        parent.writeStartElement(localName);
    }
}
