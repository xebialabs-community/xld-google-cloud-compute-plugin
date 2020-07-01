/**
 * Copyright 2020 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xebialabs.community.googlecloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfLink {
    private String project;
    private String zone;
    private String name;
    public static final String REGEXP = "/compute/beta/projects/([^/]+)/zones/([^/]+)/instances/([^/]+)";

    public SelfLink(String selfLink) throws URISyntaxException {
        String path = new URI(selfLink).getPath().trim();
        Pattern pattern = Pattern.compile(REGEXP);
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            project = matcher.group(1);
            zone = matcher.group(2);
            name = matcher.group(3);
        }
        else {
            throw new RuntimeException("Cannot decode "+selfLink);
        }

    }

    public String getProject() {
        return project;
    }

    public String getZone() {
        return zone;
    }

    public String getName() {
        return name;
    }
}
