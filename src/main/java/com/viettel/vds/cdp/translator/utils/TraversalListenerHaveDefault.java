package com.viettel.vds.cdp.translator.utils;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

public interface TraversalListenerHaveDefault<V, E>
        extends TraversalListener<V, E> {
    default void connectedComponentFinished(
            ConnectedComponentTraversalEvent var1
    ) {
    }

    default void connectedComponentStarted(
            ConnectedComponentTraversalEvent var1
    ) {
    }

    default void edgeTraversed(EdgeTraversalEvent<E> var1) {
    }

    default void vertexTraversed(VertexTraversalEvent<V> var1) {
    }

    default void vertexFinished(VertexTraversalEvent<V> var1) {
    }
}
