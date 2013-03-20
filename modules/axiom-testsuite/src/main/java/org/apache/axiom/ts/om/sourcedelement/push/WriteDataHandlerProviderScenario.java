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
package org.apache.axiom.ts.om.sourcedelement.push;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.ext.stax.datahandler.DataHandlerWriter;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.testutils.suite.MatrixTestCase;
import org.apache.axiom.util.stax.XMLStreamWriterUtils;
import org.junit.Assert;

/**
 * Tests that {@link DataHandlerWriter#writeDataHandler(DataHandlerProvider, String, boolean)}
 * creates an {@link OMText} backed by a {@link DataHandlerProvider}.
 */
public class WriteDataHandlerProviderScenario implements PushOMDataSourceScenario {
    private final DataHandler dh = new DataHandler("test", "text/plain");
    private final DataHandlerProvider dhp = new DataHandlerProvider() {
        public boolean isLoaded() {
            return true;
        }
        
        public DataHandler getDataHandler() throws IOException {
            return dh;
        }
    };

    public void addTestParameters(MatrixTestCase testCase) {
        testCase.addTestParameter("scenario", "writeDataHandlerProvider");
    }

    public Map getNamespaceContext() {
        return Collections.EMPTY_MAP;
    }

    public void serialize(XMLStreamWriter writer, Map testContext) throws XMLStreamException {
        writer.writeStartElement(null, "root", null);
        try {
            XMLStreamWriterUtils.writeDataHandler(writer, dhp, null, true);
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
        writer.writeEndElement();
    }

    public void validate(OMElement element, Map testContext) {
        OMText child = (OMText)element.getFirstOMChild();
        Assert.assertTrue(child.isBinary());
        Assert.assertSame(dh, child.getDataHandler());
    }
}
