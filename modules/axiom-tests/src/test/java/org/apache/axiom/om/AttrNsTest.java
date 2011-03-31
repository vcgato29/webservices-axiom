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

package org.apache.axiom.om;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;

import java.io.StringReader;

public class AttrNsTest extends XMLTestCase {
    public void testAttributesWithNamespaceSerialization() throws Exception {
        String xmlString =
                "<root xmlns='http://custom.com'><node cust:id='123' xmlns:cust='http://custom.com' /></root>";

        // copied code from the generated stub class toOM method
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(new StringReader(xmlString));
        org.apache.axiom.om.OMElement documentElement = builder
                .getDocumentElement();

        ((org.apache.axiom.om.impl.OMNodeEx) documentElement).setParent(null);
        // end copied code

        // now print the object after it has been processed
        System.out.println("after - '" + documentElement.toString() + "'");
        Diff diff = compareXML(xmlString, documentElement.toString());
        assertXMLEqual(diff, true);
    }
}
