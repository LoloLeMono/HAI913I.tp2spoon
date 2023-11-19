package hai913i.tp2.utils;

import java.util.HashSet;
import java.util.Set;

public class Cluster
{
	Set<String> classes;
    Cluster parent;

    public Cluster(String c) {
        this.classes = new HashSet<>();
        this.classes.add(c);
        this.parent = null;
    }

    public Cluster(Cluster c1, Cluster c2) {
        this.classes = new HashSet<>();
        this.classes.addAll(c1.classes);
        this.classes.addAll(c2.classes);
        this.parent = null;
        c1.parent = this;
        c2.parent = this;
    }

    public boolean isLeaf() {
        return parent == null;
    }
}
