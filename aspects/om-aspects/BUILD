#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

package(default_visibility = ["//visibility:public"])

load('//buildutils:aspectj.bzl', 'aj_library')

aj_library(
    name = "om-aspects",
    srcs = glob([
        "src/main/java/**/*.java",
        "src/main/java/**/*.aj",
    ]),
    deps = [
        # TODO: too many dependencies because ajc doesn't understand header JARs
        "@commons_logging//jar",
        "//aspects/core-aspects:interface",
        "//aspects/shared-aspects",
        "//axiom-api",
        "//axiom-api:blob",
        "//axiom-api:ext-activation",
        "//axiom-api:ext-io",
        "//axiom-api:ext-stax",
        "//axiom-api:mime",
        "//axiom-api:stax",
        "//axiom-api:util",
        "//axiom-api:util-activation",
        "//axiom-api:util-io",
        "//axiom-api:util-base64",
        "//axiom-api:util-namespace",
        "//axiom-api:util-sax",
        "//axiom-api:util-stax",
        "//axiom-api:util-stax-wrapper",
        "//components/base64-utils",
        "//components/core-streams",
        "//components/xml-utils",
    ],
)
