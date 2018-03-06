package org.xebialabs.community.googlecloud;

public class SelfLinkTest {

    public static void main(String[] args) throws Exception {
        String selfLink = "https://www.googleapis.com/compute/beta/projects/just-terminus-194507/zones/europe-west1-b/instances/my-xl-group-3xvc";
        SelfLink sl = new SelfLink(selfLink);
        System.out.println(sl.getProject());
        System.out.println(sl.getZone());
        System.out.println(sl.getName());
    }
}
