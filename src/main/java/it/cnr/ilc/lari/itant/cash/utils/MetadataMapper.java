package it.cnr.ilc.lari.itant.cash.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataMapper {
    private static final Logger log = LoggerFactory.getLogger(MetadataMapper.class);
    // This class is used to map metadata going in/out of the database

    public static Object lister(Object value) {
        if ( value instanceof Map ) {
            log.debug("\"listing\" object: " + value);
            return listerMap((Map<String, Object>) value);
        }
        return value;
    }

    public static Map<String, Object> listerMap(Map<String, Object> metadata) {
        Map<String, Object> toret = new HashMap<>();
        for ( String key : metadata.keySet() ) {
            Object v = metadata.get(key);
            if ( !(v instanceof List) ) {
                if ( v instanceof Map ) v = listerMap((Map<String, Object>) v);
                // make a list of 1 and put that in its place
                toret.put(key, List.of(v));
            } else {
                List<Object> l = new ArrayList<Object>();
                Iterator i = ((List) v).iterator();
                while ( i.hasNext() ) {
                    Object li = i.next();
                    if ( li instanceof Map )
                        l.add(listerMap((Map<String, Object>)li));
                    else
                        l.add(li);
                }
                toret.put(key, l);
            }
        }
        return toret;
    }

    public static Object delister(Object value) {
        if ( value instanceof Map ) {
            log.debug("Delisting: " + value);
            return delisterMap((Map<String, Object>) value);
        }
        return value;
    }

    public static Map<String, Object> delisterMap(Map<String, Object> metadata) {
        log.debug("Delisting map: " + metadata + " of class " + metadata.getClass());
        Map<String, Object> toret = new HashMap<>();
        for ( String key : metadata.keySet() ) {
            Object v = metadata.get(key);
            log.debug("Processing key: " + key + " value: " + v + " of class " + v.getClass());
            if ( v instanceof List ) {
                log.debug("Delisting list: " + v + " of class " + v.getClass());
                List l = ((List)v);
                // if the list is of size 1, put the first element of the list directly
                if ( l.size() == 1 ) {
                    // take the first element of the list and put that in its place
                    Object dv = l.get(0);
                    if (dv instanceof Map)
                        toret.put(key, delisterMap((Map<String, Object>) l.get(0)));
                    else toret.put(key, dv);
                } else {
                    List lv = new ArrayList<Object>();
                    Iterator i = l.iterator();
                    while ( i.hasNext() ) {
                        Object li = i.next();
                        log.debug("Delisting item: " + li);
                        if ( li instanceof Map )
                            lv.add(delisterMap((Map<String, Object>)li));
                        else
                            lv.add(li);
                    }
                    toret.put(key, lv);
                }
            } else {
                if (v instanceof Map)
                    toret.put(key, delisterMap((Map<String, Object>)v));
                else
                    toret.put(key, v);
            }
        }
        return toret;
    }
}
