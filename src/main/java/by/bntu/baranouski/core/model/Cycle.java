package by.bntu.baranouski.core.model;

import java.util.ArrayList;
import java.util.Collection;

public class Cycle extends ArrayList<RouteNode> {
    public Cycle() {
        super();
    }

    public Cycle(Collection<? extends RouteNode> c) {
        super(c);
    }
}