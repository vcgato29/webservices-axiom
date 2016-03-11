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

import org.apache.axiom.core.CoreChildNode;
import org.apache.axiom.core.CoreModelException;
import org.apache.axiom.om.NodeUnavailableException;
import org.apache.axiom.om.impl.common.AxiomExceptionTranslator;
import org.apache.axiom.om.impl.intf.AxiomCoreParentNode;
import org.apache.axiom.om.impl.intf.AxiomSerializable;
import org.apache.axiom.om.impl.intf.AxiomSourcedElement;

public aspect AxiomCoreParentNodeSupport {
    public final boolean AxiomCoreParentNode.isComplete() {
        try {
            switch (getState()) {
                case COMPACT:
                    return true;
                case COMPLETE:
                    CoreChildNode child = coreGetFirstChild();
                    while (child != null) {
                        if (!(child instanceof AxiomSourcedElement || ((AxiomSerializable)child).isComplete())) {
                            return false;
                        }
                        child = child.coreGetNextSibling();
                    }
                    return true;
                default:
                    return false;
            }
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }
    
    public final void AxiomCoreParentNode.build() {
        try {
            switch (getState()) {
                case DISCARDING:
                case DISCARDED:
                    throw new NodeUnavailableException();
                case COMPLETE:
                case INCOMPLETE:
                    // First do the children that have already been created; only they need recursion.
                    CoreChildNode child = coreGetFirstChildIfAvailable();
                    while (child != null) {
                        // Historically, Axiom skipped OMSourcedElements in the tree. Quote: "The
                        // OMSourcedElement has its own isolated builder/reader during the
                        // expansion process."
                        if (!(child instanceof AxiomSourcedElement)) {
                            ((AxiomSerializable)child).build();
                        }
                        child = child.coreGetNextSibling();
                    }
                    // Now build the parent; no need to recurse because all descendants will also be
                    // complete.
                    coreBuild();
            }
        } catch (CoreModelException ex) {
            throw AxiomExceptionTranslator.translate(ex);
        }
    }
}
