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
