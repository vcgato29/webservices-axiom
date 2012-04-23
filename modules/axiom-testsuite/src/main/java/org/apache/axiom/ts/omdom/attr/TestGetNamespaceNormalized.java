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
package org.apache.axiom.ts.omdom.attr;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamedInformationItem;
import org.apache.axiom.om.dom.DOMMetaFactory;
import org.apache.axiom.ts.AxiomTestCase;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

/**
 * Tests that the return value of {@link OMNamedInformationItem#getNamespace()} is correctly
 * normalized for attributes created using {@link Document#createAttributeNS(String, String)}.
 */
public class TestGetNamespaceNormalized extends AxiomTestCase {
    public TestGetNamespaceNormalized(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        Document doc = ((DOMMetaFactory)metaFactory).newDocumentBuilderFactory().newDocumentBuilder().newDocument();
        Attr attr = doc.createAttributeNS(null, "attr");
        assertNull(((OMAttribute)attr).getNamespace());
    }
}
