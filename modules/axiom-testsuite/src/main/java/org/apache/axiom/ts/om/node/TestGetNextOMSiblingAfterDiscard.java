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
package org.apache.axiom.ts.om.node;

import java.io.StringReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.ts.AxiomTestCase;

public class TestGetNextOMSiblingAfterDiscard extends AxiomTestCase {
    public TestGetNextOMSiblingAfterDiscard(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        OMFactory factory = metaFactory.getOMFactory();
        OMElement element = OMXMLBuilderFactory.createOMBuilder(factory, new StringReader(
                "<element><!--comment--><a/><!--comment--></element>")).getDocumentElement();
        OMNode child = element.getFirstOMChild();
        element.discard();
        try {
            child.getNextOMSibling();
            fail("Expected OMException");
        } catch (Exception ex) {
            // TODO: we should get an OMException here; right now we get an IllegalStateException
            // Expected
        }
    }
}
