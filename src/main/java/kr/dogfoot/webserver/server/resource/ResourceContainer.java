package kr.dogfoot.webserver.server.resource;

import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.look.LookState;

public abstract class ResourceContainer extends Resource {
    private static final int Default_Child_Size = 10;
    private Resource[] resources;
    private int resourceCount;

    protected ResourceContainer() {
        resources = new Resource[Default_Child_Size];
        resourceCount = 0;
    }

    @Override
    public boolean look(LookState ls, LookResult lr) {
        lr.appendFilter(filters());

        if (ls.isLastPathItem()) {
            lr.resource(this);
            return true;
        } else {
            Resource found = look(ls.getNextPathItem());
            if (found != null) {
                return found.look(ls, lr);
            }
        }
        return false;
    }

    protected Resource look(String path) {
        for (Resource r : resources) {
            if (r != null && r.isMatched(path)) {
                return r;
            }
        }
        return null;
    }

    public void addResource(Resource resource) {
        if (resources.length <= resourceCount) {
            Resource[] newArray = new Resource[resources.length * 2];
            System.arraycopy(resources, 0, newArray, 0, resources.length);
            resources = newArray;
        }

        resources[resourceCount++] = resource;
    }

    public boolean includedDirectory(String path) {
        for (Resource r : resources) {
            if (r != null
                    && r.type() == ResourceType.Directory
                    && r.path().equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    public ResourceDirectory getDirectory(String path) {
        for (Resource r : resources) {
            if (r != null
                    && r.type() == ResourceType.Directory
                    && r.path().equalsIgnoreCase(path)) {
                return (ResourceDirectory) r;
            }
        }
        return null;
    }
}
