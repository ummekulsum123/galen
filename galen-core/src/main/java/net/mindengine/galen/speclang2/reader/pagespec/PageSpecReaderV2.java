/*******************************************************************************
* Copyright 2015 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.speclang2.reader.pagespec;

import net.mindengine.galen.browser.Browser;
import net.mindengine.galen.parser.IndentationStructureParser;
import net.mindengine.galen.parser.StructNode;
import net.mindengine.galen.parser.SyntaxException;
import net.mindengine.galen.specs.reader.page.PageSpec;
import net.mindengine.galen.utils.GalenUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PageSpecReaderV2 {

    public PageSpec read(String path, Browser browser) throws IOException {
        String contextPath = GalenUtils.getParentForFile(path);
        return read(GalenUtils.findFileOrResourceAsStream(path), path, contextPath, browser);
    }

    public PageSpec read(InputStream inputStream, String source, String contextPath, Browser browser) throws IOException {
        IndentationStructureParser structParser = new IndentationStructureParser();
        List<StructNode> structs = structParser.parse(inputStream, source);

        PageSpec pageSpec = new PageSpec();

        PageSpecHandler pageSpecHandler = new PageSpecHandler(pageSpec, browser);

        List<StructNode> allProcessedChildNodes = new LogicProcessor(pageSpecHandler).process(structs);

        for (StructNode structNode : allProcessedChildNodes) {
            processNode(structNode, pageSpecHandler, contextPath);
        }

        return pageSpecHandler.buildPageSpec();
    }

    private void processNode(StructNode node, PageSpecHandler pageSpecHandler, String contextPath) throws IOException {
        if (PageSectionProcessor.isSectionDefinition(node.getName())) {
            new PageSectionProcessor(pageSpecHandler).process(node, contextPath);
        } else {
            throw new SyntaxException(node, "Unknown statement: " + node.getName());
        }
    }

}