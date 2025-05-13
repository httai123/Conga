package com.viettel.vds.cdp.translator.utils;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class IdSetter {

    private final Set<String> ids;
    private final boolean uniqueId;

    public IdSetter(boolean uniqueId, Set<String> ids) {
        this.uniqueId = uniqueId;
        this.ids = Optional.ofNullable(ids).orElse(new HashSet<>());
    }

    private String hash() {
        long time = new Date().getTime();
        return "id_" + time;
    }

    private synchronized String createUniqueID() {
        String id = hash();
        while (isDup(id)) {
            id = hash();
        }
        ids.add(id);
        return id;
    }

    public String createId() {
        return uniqueId ? createUniqueID() : hash();
    }

    public boolean isDup(String id) {
        return this.ids.contains(id);
    }
}
