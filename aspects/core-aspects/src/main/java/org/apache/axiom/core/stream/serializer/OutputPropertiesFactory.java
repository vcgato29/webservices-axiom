/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package org.apache.axiom.core.stream.serializer;

/**
 * This class is a factory to generate a set of default properties
 * of key/value pairs that are used to create a serializer through the
 * factory {@link SerializerFactory SerilizerFactory}. 
 * The properties generated by this factory
 * may be modified to non-default values before the SerializerFactory is used to
 * create a Serializer.
 * <p>
 * The given output types supported are "xml", "text", and "html". 
 * These type strings can be obtained from the 
 * {@link Method Method} class in this package.
 * <p>
 * Other constants defined in this class are the non-standard property keys
 * that can be used to set non-standard property values on a java.util.Properties object
 * that is used to create or configure a serializer. Here are the non-standard keys:
 * <ul>
 * <li> <b>S_KEY_INDENT_AMOUNT </b> -
 * The non-standard property key to use to set the indentation amount.
 * The "indent" key needs to have a value of "yes", and this
 * properties value is a the number of whitespaces to indent by per
 * indentation level.
 * 
 * <li> <b>S_KEY_CONTENT_HANDLER </b> -
 * This non-standard property key is used to set the name of the fully qualified 
 * Java class that implements the ContentHandler interface. 
 * The output of the serializer will be SAX events sent to this an
 * object of this class.
 * 
 * <li> <b>S_KEY_ENTITIES </b> -
 * This non-standard property key is used to specify the name of the property file
 * that specifies character to entity reference mappings. A line in such a
 * file is has the name of the entity and the numeric (base 10) value
 * of the corresponding character, like this one: <br> quot=34 <br>
 * 
 * <li> <b>S_USE_URL_ESCAPING </b> -
 * This non-standard property key is used to set a value of "yes" if the href values for HTML serialization should
 *  use %xx escaping.
 * 
 * <li> <b>S_OMIT_META_TAG </b> -
 * This non-standard property key is used to set a value of "yes" if the META tag should be omitted where it would
 *  otherwise be supplied.
 * </ul>
 * 
 * @see SerializerFactory
 * @see Method
 * @see Serializer
 */
public final class OutputPropertiesFactory
{
    /** S_BUILTIN_EXTENSIONS_URL is a mnemonic for the XML Namespace 
     *(http://xml.apache.org/xalan) predefined to signify Xalan's
     * built-in XSLT Extensions. When used in stylesheets, this is often 
     * bound to the "xalan:" prefix.
     */
    private static final String 
      S_BUILTIN_EXTENSIONS_URL = "http://xml.apache.org/xalan"; 

    /**
     * The old built-in extension url. It is still supported for
     * backward compatibility.
     */
    private static final String 
      S_BUILTIN_OLD_EXTENSIONS_URL = "http://xml.apache.org/xslt"; 
      
    //************************************************************
    //*  PUBLIC CONSTANTS
    //************************************************************
    /** 
     * This is not a public API.
     * This is the built-in extensions namespace, 
     * reexpressed in {namespaceURI} syntax
     * suitable for prepending to a localname to produce a "universal
     * name".
     */
    public static final String S_BUILTIN_EXTENSIONS_UNIVERSAL =
        "{" + S_BUILTIN_EXTENSIONS_URL + "}";

    // Some special Xalan keys.

    /** 
     * The non-standard property key to use to set the
     * number of whitepaces to indent by, per indentation level,
     * if indent="yes".
     */
    public static final String S_KEY_INDENT_AMOUNT =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "indent-amount";
        
    /** 
     * The non-standard property key to use to set the
     * characters to write out as at the end of a line,
     * rather than the default ones from the runtime.
     */
    public static final String S_KEY_LINE_SEPARATOR =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "line-separator";        

    /** This non-standard property key is used to set the name of the fully qualified 
     * Java class that implements the ContentHandler interface. 
     * Fully qualified name of class with a default constructor that
     *  implements the ContentHandler interface, where the result tree events
     *  will be sent to.
     */

    public static final String S_KEY_CONTENT_HANDLER =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "content-handler";

    /**
     * This non-standard property key is used to specify the name of the property file
     * that specifies character to entity reference mappings.
     */
    public static final String S_KEY_ENTITIES =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "entities";

    /** 
     * This non-standard property key is used to set a value of "yes" if the href values for HTML serialization should
     *  use %xx escaping. */
    public static final String S_USE_URL_ESCAPING =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "use-url-escaping";

    /** 
     * This non-standard property key is used to set a value of "yes" if the META tag should be omitted where it would
     *  otherwise be supplied.
     */
    public static final String S_OMIT_META_TAG =
        S_BUILTIN_EXTENSIONS_UNIVERSAL + "omit-meta-tag";

    /**
     * The old built-in extension namespace, this is not a public API.
     */
    public static final String S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL =
        "{" + S_BUILTIN_OLD_EXTENSIONS_URL + "}";

    /**
     * This is not a public API, it is only public because it is used
     * by outside of this package,
     * it is the length of the old built-in extension namespace.
     */
    public static final int S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN =
        S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL.length();

}
